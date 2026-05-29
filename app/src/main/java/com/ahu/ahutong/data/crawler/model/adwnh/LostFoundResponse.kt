package com.ahu.ahutong.data.crawler.model.adwnh

import com.google.gson.annotations.SerializedName

data class LostFoundResponse(
    val code: Int,
    val msg: String,

    @SerializedName("object")
    val data: LostFoundPage
)

data class LostFoundPage(
    val pageNum: Int,
    val pageSize: Int,
    val size: Int,
    val startRow: Int,
    val endRow: Int,
    val total: Int,
    val pages: Int,
    val list: List<LostFoundItem>
)

data class LostFoundItem(
    val id: String,
    val title: String,
    val phone: String?,
    val linkman: String?,
    val createuser: String?,
    val createtime: String?,
    val state: Int,
    val audituser: String?,
    val auditresult: String?,
    val typeid: String?,
    val campusid: String?,
    val num1: String?,
    val num2: String?,
    val campusName: String?,
    val audituserName: String?,
    val imgs: List<LostFoundImage>,
    val pubuser: LostFoundUser?,
    val lostType: LostFoundType?
)

data class LostFoundImage(
    val imgId: String,
    val imgPath: String,
    val createtime: String?,
    val lostid: String
)

data class LostFoundUser(
    val idNumber: String?,
    val unitUid: String?,
    val unitName: String?,
    val userName: String?,
    val mobile: String?,
    val headimgurl: String?
)

data class LostFoundType(
    val typeId: String,
    val typeName: String,
    val typeState: String,
    val createtime: String?,
    val createuser: String?,
    val updatetime: String?,
    val updateuser: String?,
    val remark: String?
)