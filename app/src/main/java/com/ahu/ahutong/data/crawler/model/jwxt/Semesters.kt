package com.ahu.ahutong.data.crawler.model.jwxt

class Semesters : ArrayList<SemestersItem>()

data class SemestersItem(
    val approvedYear: String,
    val calendarAssoc: Int,
    val code: String,
    val countInTerm: Boolean,
    val enabled: Boolean,
    val endDate: String,
    val fileInfoAssoc: Any,
    val id: Int,
    val includeMonths: List<Int>,
    val name: String,
    val nameEn: String,
    val nameZh: String,
    val schoolYear: String,
    val season: String,
    val startDate: String,
    val transient: Boolean,
    val weekStartOnSunday: Boolean
)