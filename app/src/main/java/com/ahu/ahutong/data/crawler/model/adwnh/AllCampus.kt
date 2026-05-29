package com.ahu.ahutong.data.crawler.model.adwnh

import java.io.Serializable

/**
 * 校区列表接口根实体
 */
data class AllCampus(
    val code: Int,
    val msg: String,
    val `object`: List<CampusItem> = emptyList()
) : Serializable

/**
 * 校区条目
 */
data class CampusItem(
    val id: String = "",
    val campusName: String = "",
    val campusOrder: Int = 0,
    val createTime: String = "",
    val createUser: String = "",
    val updateTime: String? = null,
    val updater: String? = null
) : Serializable