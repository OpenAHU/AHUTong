package com.ahu.ahutong.data.gray

import android.content.Context
import android.provider.Settings
import com.ahu.ahutong.BuildConfig
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.server.AhuTong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

enum class GrayOverride(
    val storageValue: String,
    val label: String
) {
    FollowRollout("follow", "跟随灰度"),
    ForceEnabled("enabled", "强制开启"),
    ForceDisabled("disabled", "强制关闭");

    companion object {
        fun fromStorage(value: String?): GrayOverride =
            values().firstOrNull { it.storageValue == value } ?: FollowRollout
    }
}

data class GrayFeatureState(
    val feature: GrayFeature,
    val override: GrayOverride,
    val bucket: Int,
    val rolloutPercentage: Int,
    val rolloutEnabled: Boolean,
    val enabled: Boolean,
    val source: String
) {
    val reason: String
        get() = when (override) {
            GrayOverride.FollowRollout -> {
                if (source == REMOTE_SOURCE) {
                    if (enabled) "服务端开启" else "服务端关闭"
                } else {
                    if (rolloutEnabled) "本地兜底命中" else "本地兜底未命中"
                }
            }
            GrayOverride.ForceEnabled -> "Debug 强制开启"
            GrayOverride.ForceDisabled -> "Debug 强制关闭"
        }

    companion object {
        const val LOCAL_SOURCE = "local"
        const val REMOTE_SOURCE = "remote"
        const val DEBUG_SOURCE = "debug"
    }
}

/*
 * 当前服务端灰度逻辑：
 * - 客户端请求 GET /api/gray/check，参数为 feature、subject、versionCode、versionName。
 * - subject 是当前学号或 Android ID 的 SHA-256 摘要，不上传原始用户标识。
 * - 服务端配置文件位于 /home/ubuntu/AHUTong/server/update_server/gray_config.json。
 * - 服务端每次请求实时读取配置，先判断 forceDisabledSubjects，再判断 forceEnabledSubjects，
 *   否则在 enabled=true 时按 bucket < rolloutPercentage 命中灰度。
 * - 接口失败、字段缺失或网络超时时，客户端退回 GrayFeature.rolloutPercentage 本地稳定分桶。
 */
object GrayReleaseManager {
    suspend fun states(context: Context): List<GrayFeatureState> =
        GrayFeatures.all.map { state(it, context) }

    suspend fun state(feature: GrayFeature, context: Context): GrayFeatureState {
        val override = GrayOverride.fromStorage(AHUCache.getGrayOverride(feature.key))
        if (override != GrayOverride.FollowRollout) {
            return debugOverrideState(feature, context, override)
        }

        return remoteState(feature, context).getOrElse {
            localState(feature, context, override)
        }
    }

    fun localStates(context: Context): List<GrayFeatureState> =
        GrayFeatures.all.map { localState(it, context) }

    fun localState(
        feature: GrayFeature,
        context: Context,
        override: GrayOverride = GrayOverride.fromStorage(AHUCache.getGrayOverride(feature.key))
    ): GrayFeatureState {
        if (override != GrayOverride.FollowRollout) {
            return debugOverrideState(feature, context, override)
        }

        val bucket = GrayRollout.bucket(
            featureKey = feature.key,
            subjectKey = subjectKey(context)
        )
        val rolloutEnabled = GrayRollout.isEnabled(feature.rolloutPercentage, bucket)

        return GrayFeatureState(
            feature = feature,
            override = override,
            bucket = bucket,
            rolloutPercentage = feature.rolloutPercentage,
            rolloutEnabled = rolloutEnabled,
            enabled = rolloutEnabled,
            source = GrayFeatureState.LOCAL_SOURCE
        )
    }

    suspend fun isEnabled(feature: GrayFeature, context: Context): Boolean =
        state(feature, context).enabled

    fun setOverride(feature: GrayFeature, override: GrayOverride) {
        if (override == GrayOverride.FollowRollout) {
            AHUCache.clearGrayOverride(feature.key)
        } else {
            AHUCache.setGrayOverride(feature.key, override.storageValue)
        }
    }

    private suspend fun remoteState(
        feature: GrayFeature,
        context: Context
    ): Result<GrayFeatureState> = withContext(Dispatchers.IO) {
        runCatching {
            val subject = subjectKey(context)
            val decision = AhuTong.GRAY_API.getGrayFeatureDecision(
                feature = feature.key,
                subject = subject,
                versionCode = BuildConfig.VERSION_CODE,
                versionName = BuildConfig.VERSION_NAME
            )
            val enabled = decision.enabled
                ?: throw IllegalStateException("Gray decision missing enabled")
            val rolloutPercentage = decision.rolloutPercentage
                ?.coerceIn(0, 100)
                ?: feature.rolloutPercentage
            val bucket = decision.bucket
                ?.coerceIn(0, 99)
                ?: GrayRollout.bucket(feature.key, subject)

            GrayFeatureState(
                feature = feature,
                override = GrayOverride.FollowRollout,
                bucket = bucket,
                rolloutPercentage = rolloutPercentage,
                rolloutEnabled = GrayRollout.isEnabled(rolloutPercentage, bucket),
                enabled = enabled,
                source = GrayFeatureState.REMOTE_SOURCE
            )
        }
    }

    private fun debugOverrideState(
        feature: GrayFeature,
        context: Context,
        override: GrayOverride
    ): GrayFeatureState {
        val bucket = GrayRollout.bucket(feature.key, subjectKey(context))
        return GrayFeatureState(
            feature = feature,
            override = override,
            bucket = bucket,
            rolloutPercentage = feature.rolloutPercentage,
            rolloutEnabled = GrayRollout.isEnabled(feature.rolloutPercentage, bucket),
            enabled = override == GrayOverride.ForceEnabled,
            source = GrayFeatureState.DEBUG_SOURCE
        )
    }

    private fun subjectKey(context: Context): String {
        AHUCache.getCurrentUser()?.xh?.takeIf { it.isNotBlank() }?.let { userId ->
            return sha256("user:$userId")
        }

        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        return androidId?.takeIf { it.isNotBlank() }?.let { sha256("device:$it") }
            ?: sha256("guest")
    }

    private fun sha256(value: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }
}
