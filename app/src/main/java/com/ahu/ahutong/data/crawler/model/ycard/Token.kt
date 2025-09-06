package com.ahu.ahutong.data.crawler.model.ycard

data class Token(
    val access_token: String,
    val client_id: String,
    val expires_in: Int,
    val flag: String,
    val id: Int,
    val is_first_login: Boolean,
    val jti: String,
    val loginFrom: String,
    val logintype: String,
    val mobile: String,
    val name: String,
    val refresh_token: String,
    val scope: String,
    val sno: String,
    val tenant_id: String,
    val token_type: String,
    val uuid: String
)