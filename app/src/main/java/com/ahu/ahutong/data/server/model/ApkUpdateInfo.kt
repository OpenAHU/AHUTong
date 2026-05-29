package com.ahu.ahutong.data.server.model

import androidx.annotation.Keep

@Keep
data class ApkUpdateInfo(
    val update: Boolean? = null,
    val force: Boolean = false,
    val versionCode: Int = 0,
    val versionName: String? = null,
    val changelog: String? = null,
    val url: String? = null,
    val sha256: String? = null,
    val signature: String? = null,
    val alg: String? = null,
    val note: String? = null
)
