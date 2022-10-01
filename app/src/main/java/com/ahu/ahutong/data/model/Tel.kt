package com.ahu.ahutong.data.model

import java.io.Serializable

data class Tel(
    val name: String,
    val tel: String? = null, // 磬苑校区
    val tel2: String? = tel // 龙河校区
) : Serializable
