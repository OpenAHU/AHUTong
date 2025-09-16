package com.ahu.ahutong.data.crawler.model.ycard

import com.google.gson.annotations.SerializedName

data class BathroomInfo(
    val msg: String,
    val code: Int,
    val map: MapData
)

data class MapData(
    val showData: ShowData,
    val data: BathroomData
)

data class ShowData(
    @SerializedName("手机号") val phoneNumber: String,
    @SerializedName("现金金额（单位：元）") val cashAmount: String,
    @SerializedName("赠送金额（单位：元）") val giftAmount: String
)

data class BathroomData(
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
    val tsmAbstract: String
)
