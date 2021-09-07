package com.ahu.ahutong.ext

import org.jsoup.Connection


fun Connection.require(): Connection.Response{
    val response = execute()
    if (response.statusCode() == 503) {
        throw IllegalStateException("请求过于频繁，请稍后刷新。")
    }
    return response
}