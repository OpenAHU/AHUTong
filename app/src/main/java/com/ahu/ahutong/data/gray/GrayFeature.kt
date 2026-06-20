package com.ahu.ahutong.data.gray

data class GrayFeature(
    val key: String,
    val title: String,
    val description: String,
    val rolloutPercentage: Int
) {
    init {
        require(rolloutPercentage in 0..100) {
            "rolloutPercentage must be in 0..100"
        }
    }
}

/*
 * 灰度注册说明：
 * 1. 新增客户端灰度时，在 GrayFeatures 中注册 GrayFeature，并加入 all 列表。
 * 2. key 必须与服务端 gray_config.json 中 features.<key> 保持一致。
 * 3. rolloutPercentage 只作为服务端请求失败时的本地兜底比例；正常线上开关和比例以服务端为准。
 */
object GrayFeatures {
    val HomeEdit = GrayFeature(
        key = "home_edit",
        title = "首页编辑",
        description = "允许长按首页进入编辑态，并在小工具页显示编辑入口。",
        rolloutPercentage = 0
    )

    val all: List<GrayFeature> = listOf(
        HomeEdit
    )
}
