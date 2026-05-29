package com.ahu.ahutong.data.crawler.model.adwnh

data class LostFoundPublishRequest(
    val imgs: List<Any> = emptyList(),
    val linkman: String,
    val phone: String,
    val typeid: String,
    val num1: String? = null,
    val campusid: String,
    val title: String,
    val state: String,
    val auditresult: Int = 1
)