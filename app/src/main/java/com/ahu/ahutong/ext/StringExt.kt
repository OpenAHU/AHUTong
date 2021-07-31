package com.ahu.ahutong.ext

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
