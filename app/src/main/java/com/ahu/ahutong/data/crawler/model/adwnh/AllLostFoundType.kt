package com.ahu.ahutong.data.crawler.model.adwnh

data class AllLostFoundType(
    val code: Int,
    val msg: String,
    val `object`: List<LostFoundTypeItem>
)

data class LostFoundTypeItem(
    val typeId: String,
    val typeName: String,
    val typeState: String,
    val createtime: String,
    val createuser: String?,
    val updatetime: String?,
    val updateuser: String?,
    val remark: String?
)