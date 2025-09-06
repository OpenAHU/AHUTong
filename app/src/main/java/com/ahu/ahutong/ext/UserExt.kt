package com.ahu.ahutong.ext

import com.ahu.ahutong.data.model.User
import java.util.*

fun User.getSchoolYears(): Array<String> {
    // 求出用户的入学时间
    val startYear = this.xh
        .substring(2, 4).toInt() + 2000
    val calendar = Calendar.getInstance()
    // 获取截至年份
    val thisYear = calendar.get(Calendar.YEAR) +
        if (calendar.get(Calendar.MONTH) < Calendar.SEPTEMBER) -1 else 0
    return (thisYear downTo startYear).toList()
        .map { "$it-${it + 1}" }
        .toTypedArray()
}
