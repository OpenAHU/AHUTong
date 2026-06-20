package com.ahu.ahutong.data.server.model

data class GrayFeatureDecision(
    val feature: String? = null,
    val enabled: Boolean? = null,
    val rolloutPercentage: Int? = null,
    val bucket: Int? = null,
    val source: String? = null,
    val reason: String? = null
)
