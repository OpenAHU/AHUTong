package com.ahu.ahutong.data.server.model

import androidx.annotation.Keep

@Keep
data class ApkUpdateInfo(
    val update: Boolean = false,
    val force: Boolean,
    val versionCode: Int,
    val versionName: String,
    val changelog: String,
    val url: String? = null,
    val sha256: String? = null,
    val signature: String? = null,
    val alg: String? = null,
    val note: String? = null
)
