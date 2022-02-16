package com.ahu.ahutong.ext

import com.ahu.ahutong.data.AHUResponse


fun <T> createSuccessResponse(message: String, data: T): AHUResponse<T> {
    val ahuResponse = AHUResponse<T>()
    ahuResponse.msg = message
    ahuResponse.code = 0
    ahuResponse.data = data
    return ahuResponse
}

fun <T> createFailureResponse(message: String): AHUResponse<T> {
    val ahuResponse = AHUResponse<T>()
    ahuResponse.msg = message
    ahuResponse.code = -1
    ahuResponse.data = null
    return ahuResponse
}