package com.ahu.ahutong.data.model

import java.io.Serializable

data class GpaRankInfo(
    val id: Long = 0,
    val gpa: Double = 0.0,
    val majorRank: Int = 0,
    val majorHeadCount: Int = 0,
    val inPlanCredits: Double = 0.0,
    val outPlanCredits: Double = 0.0,
    val totalCredits: Double = 0.0,
    val gpaSemesterSubs: List<GpaSemesterSub> = emptyList(),
    val updatedDateTimeStr: String = "",
    val studentAssoc: StudentAssoc? = null,
    val bizTypeAssoc: BizTypeAssoc? = null
) : Serializable

data class GpaSemesterSub(
    val gpa: Double = 0.0,
    val semesterId: Int = 0,
    val majorRank: Int = 0,
    val inPlanCredits: Double = 0.0,
    val outPlanCredits: Double = 0.0,
    val totalCredits: Double = 0.0
) : Serializable

data class StudentAssoc(
    val id: Long = 0
) : Serializable

data class BizTypeAssoc(
    val id: Int = 0
) : Serializable