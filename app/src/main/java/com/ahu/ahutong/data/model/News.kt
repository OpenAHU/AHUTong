package com.ahu.ahutong.data.model

import com.google.gson.annotations.SerializedName

/**
 * @Author: SinkDev
 * @Date: 2021/8/2-下午12:36
 * @Email: 468766131@qq.com
 */
class News {
    @SerializedName("department")
    var department: String? = null

    @SerializedName("author")
    var author: String? = null

    @SerializedName("title")
    var title: String? = null

    @SerializedName("abstract")
    var abstractX: String? = null

    @SerializedName("releaseTime")
    var releaseTime: String? = null

    @SerializedName("detailUrl")
    var detailUrl: String? = null

    @SerializedName("imageUrlList")
    var imageUrlList: List<String>? = null
}