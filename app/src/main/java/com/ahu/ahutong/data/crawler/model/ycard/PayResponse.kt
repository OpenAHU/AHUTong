package com.ahu.ahutong.data.crawler.model.ycard

data class PayResponse(
    val code: Int,
    val `data`: String,
    val msg: String,
    val success: Boolean
)