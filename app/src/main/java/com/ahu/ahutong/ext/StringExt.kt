package com.ahu.ahutong.ext

import com.google.gson.Gson
import java.lang.reflect.Type

/**
 * @Author: SinkDev
 * @Date: 2021/7/31-下午8:58
 * @Email: 468766131@qq.com
 */
fun <T> String.fromJson(classOfT: Class<T>?): T? {
    return try {
        Gson().fromJson(this, classOfT)
    } catch (e: Exception) {
        null
    }
}

fun <T> String.fromJson(typeOfT: Type?): T? {
    return try {
        Gson().fromJson(this, typeOfT)
    } catch (e: Exception) {
        null
    }
}
