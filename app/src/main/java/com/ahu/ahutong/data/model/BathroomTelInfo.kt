package com.ahu.ahutong.data.model

import com.google.gson.annotations.SerializedName

data class BathroomTelInfo(
    val msg: String,
    val code: Int,
    val map: MapData?,
    val message:String?
)

data class MapData(
    val showData: ShowData?,
    val data: Data?
)

data class ShowData(
    @SerializedName("手机号")
    val phone: String,

    @SerializedName("现金金额（单位：元）")
    val cashAmount: String,

    @SerializedName("赠送金额（单位：元）")
    val giftAmount: String
)

data class Data(
    val projectId: Int,
    val projectName: String,
    val accountId: Int,
    val telPhone: String,
    val identifier: String?,
    val sex: String,
    val name: String?,
    val statusId: Int,
    val accountMoney: Int,
    val accountGivenMoney: Int,
    val alias: String?,
    val tags: String?,
    val isCard: Int,
    val cardStatusId: Int,
    val isUseCode: Int,
    val cardPhysicalId: String?,
    val tsmAbstract: String,
    var myCustomInfo:String?,
    var message:String?
)