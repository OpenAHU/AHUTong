package com.ahu.ahutong.data.crawler.model.jwxt

data class CurrentTeachWeek(
    val currentSemester: String,
    val dayIndex: Int,
    val isInSemester: Boolean,
    val weekIndex: Int
)