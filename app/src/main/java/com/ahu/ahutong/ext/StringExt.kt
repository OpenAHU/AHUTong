package com.ahu.ahutong.ext

import com.google.gson.Gson
import java.lang.reflect.Type

/**
 * @Author: SinkDev
 * @Date: 2021/7/31-下午8:58
 * @Email: 468766131@qq.com
 */

fun String.isCampus(): Boolean{
    return this == "1"|| this == "2"
}

fun String.isWeekday(): Boolean{
    return this in listOf("1", "2", "3", "4", "5", "6", "7")
}

fun String.isEmptyRoomTime(): Boolean{
    return this in listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
}

fun String.isTerm(): Boolean{
    return this == "1"|| this == "2"
}

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
