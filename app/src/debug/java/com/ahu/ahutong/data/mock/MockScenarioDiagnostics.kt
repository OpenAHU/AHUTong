package com.ahu.ahutong.data.mock

import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.Exam
import com.ahu.ahutong.data.model.Grade

object MockScenarioDiagnostics {
    fun report(scenario: MockScenario): List<String> =
        buildList {
            add(header(scenario))
            addAll(behaviorReport(scenario.behavior))
            addAll(academicReport(scenario.academic))
            addAll(campusReport(scenario.campus))
            addAll(paymentReport(scenario.payment, scenario.behavior))
            addAll(discoveryReport(scenario.discovery))
            addAll(riskReport(scenario))
        }

    private fun header(scenario: MockScenario): String =
        "场景：${scenario.title} / ${scenario.badge}"

    private fun behaviorReport(behavior: MockBehavior): List<String> =
        listOf(
            "网络模式：${behavior.networkMode.label()}",
            "支付模式：${behavior.paymentMode.label()}",
            "登录态：${behavior.loginState.label()}",
            "接口失败文案：${behavior.errorMessage}"
        )

    private fun academicReport(profile: MockAcademicProfile): List<String> {
        val scheduleSummary = scheduleSummary(profile.currentSchedule)
        val nextScheduleSummary = scheduleSummary(profile.nextSchedule)
        val gradeSummary = gradeSummary(profile.grade)
        val examSummary = examSummary(profile.exams)
        return listOf(
            "当前课表：${scheduleSummary.totalCourses} 门，覆盖 ${scheduleSummary.weekdaysCovered} 天，${scheduleSummary.minWeek}-${scheduleSummary.maxWeek} 周",
            "下学期课表：${nextScheduleSummary.totalCourses} 门，覆盖 ${nextScheduleSummary.weekdaysCovered} 天",
            "成绩：${gradeSummary.termCount} 个学期，${gradeSummary.courseCount} 门课程，GPA ${profile.grade.totalGradePointAverage ?: "暂无"}",
            "成绩边界：优秀 ${gradeSummary.excellentCount}，不及格 ${gradeSummary.failedCount}，重修 ${gradeSummary.retakeCount}",
            "排名：${profile.gpaRankInfo.majorRank}/${profile.gpaRankInfo.majorHeadCount}，更新时间 ${profile.gpaRankInfo.updatedDateTimeStr}",
            "考试：${examSummary.total} 场，进行中 ${examSummary.running}，未开始 ${examSummary.upcoming}，已结束 ${examSummary.finished}"
        )
    }

    private fun campusReport(profile: MockCampusProfile): List<String> {
        val buildingCount = profile.classroomBuildings.values.sumOf { it.size }
        val roomCount = profile.classroomRooms.values.sumOf { it.size }
        val roomSeats = profile.classroomRooms.values.flatten().sumOf { it.seats }
        val bathroomOpen = profile.bathrooms.count { it.openStatus?.contains("开放") == true }
        return listOf(
            "校区：${profile.campuses.`object`.size} 个，教学楼 $buildingCount 栋，空教室 $roomCount 间",
            "空教室容量：总座位 $roomSeats，平均 ${if (roomCount == 0) 0 else roomSeats / roomCount} 座/间",
            "浴室：${profile.bathrooms.size} 个，其中开放 $bathroomOpen 个",
            "浴室账户：${profile.bathroomAccounts.keys.joinToString("、").ifBlank { "无" }}",
            "校历：${profile.calendarJpegBase64.length} 字符 Base64，占位图仅用于 Debug"
        )
    }

    private fun paymentReport(
        profile: MockPaymentProfile,
        behavior: MockBehavior
    ): List<String> =
        listOf(
            "一卡通余额：${profile.cardMoney.balance} 元，过渡余额 ${profile.cardMoney.transitionBalance} 元",
            "一卡通账户：${profile.cardInfo.data.card.firstOrNull()?.cardname ?: "无"} / ${profile.cardInfo.data.card.firstOrNull()?.sno ?: "无学号"}",
            "订单前缀：${profile.cardOrderPrefix} / ${profile.bathroomOrderPrefix}",
            "支付预期：${if (behavior.paymentMode == MockPaymentMode.Success) profile.successMessage else profile.failureMessage}"
        )

    private fun discoveryReport(profile: MockDiscoveryProfile): List<String> {
        val lostCount = profile.lostFoundItems.count { it.state == 1 }
        val foundCount = profile.lostFoundItems.count { it.state == 2 }
        val campusNames = profile.lostFoundCampuses.`object`
            .map { it.campusName }
            .joinToString("、")
        val typeNames = profile.lostFoundTypes.`object`
            .map { it.typeName }
            .joinToString("、")
        return listOf(
            "失物招领：失物 $lostCount 条，寻物 $foundCount 条，总计 ${profile.lostFoundItems.size} 条",
            "招领校区：${campusNames.ifBlank { "无" }}",
            "招领类型：${typeNames.ifBlank { "无" }}",
            "分页压力：${if (profile.lostFoundItems.size > 20) "可触发加载更多" else "单页即可展示"}"
        )
    }

    private fun riskReport(scenario: MockScenario): List<String> {
        val risks = mutableListOf<String>()
        if (scenario.academic.currentSchedule.isEmpty()) {
            risks += "课表空状态"
        }
        if (scenario.academic.grade.termGradeList.isNullOrEmpty()) {
            risks += "成绩空状态"
        }
        if (scenario.academic.exams.isEmpty()) {
            risks += "考试空状态"
        }
        if (scenario.campus.classroomRooms.values.all { it.isEmpty() }) {
            risks += "空教室无结果"
        }
        if (scenario.discovery.lostFoundItems.isEmpty()) {
            risks += "失物招领空列表"
        }
        if (scenario.behavior.shouldFail) {
            risks += "接口错误流"
        }
        if (scenario.behavior.paymentMode != MockPaymentMode.Success) {
            risks += "支付失败流"
        }
        if (scenario.behavior.loginState != MockLoginState.Valid) {
            risks += "登录异常流"
        }
        return listOf("覆盖边界：${risks.joinToString("、").ifBlank { "标准成功流" }}")
    }

    fun validate(scenario: MockScenario): List<String> =
        buildList {
            validateIdentity(scenario)?.let { add(it) }
            validateAcademic(scenario).forEach { add(it) }
            validateCampus(scenario).forEach { add(it) }
            validatePayment(scenario).forEach { add(it) }
            validateDiscovery(scenario).forEach { add(it) }
        }

    private fun validateIdentity(scenario: MockScenario): String? =
        when {
            scenario.id.isBlank() -> "场景 id 为空"
            scenario.title.isBlank() -> "场景标题为空"
            scenario.badge.isBlank() -> "场景标签为空"
            else -> null
        }

    private fun validateAcademic(scenario: MockScenario): List<String> {
        val result = mutableListOf<String>()
        val schedule = scenario.academic.currentSchedule
        val grade = scenario.academic.grade
        val exams = scenario.academic.exams
        if (!scenario.behavior.shouldFail && scenario.behavior.networkMode != MockNetworkMode.Empty) {
            if (schedule.isEmpty()) result += "${scenario.title} 未提供当前课表"
            if (grade.termGradeList.isNullOrEmpty()) result += "${scenario.title} 未提供成绩学期"
            if (exams.isEmpty()) result += "${scenario.title} 未提供考试数据"
        }
        schedule.forEachIndexed { index, course ->
            if (course.name.isNullOrBlank()) result += "${scenario.title} 第 ${index + 1} 门课缺少课程名"
            if (course.weekday !in 1..7) result += "${scenario.title} ${course.name} weekday 越界"
            if (course.startTime !in 1..13) result += "${scenario.title} ${course.name} startTime 越界"
            if (course.length <= 0) result += "${scenario.title} ${course.name} length 非法"
        }
        exams.forEachIndexed { index, exam ->
            if (exam.course.isNullOrBlank()) result += "${scenario.title} 第 ${index + 1} 场考试缺少课程名"
            if (exam.time.isNullOrBlank()) result += "${scenario.title} ${exam.course} 缺少考试时间"
            if (exam.location.isNullOrBlank()) result += "${scenario.title} ${exam.course} 缺少考试地点"
        }
        return result
    }

    private fun validateCampus(scenario: MockScenario): List<String> {
        val result = mutableListOf<String>()
        val campus = scenario.campus
        if (!scenario.behavior.shouldFail && campus.campuses.`object`.isEmpty()) {
            result += "${scenario.title} 未提供校区列表"
        }
        campus.classroomBuildings.forEach { (campusId, buildings) ->
            buildings.forEach { building ->
                if (building.id <= 0) result += "${scenario.title} 校区 $campusId 存在非法教学楼 id"
                if (building.nameZh.isBlank()) result += "${scenario.title} 校区 $campusId 存在无名称教学楼"
            }
        }
        campus.classroomRooms.forEach { (campusId, rooms) ->
            rooms.forEach { room ->
                if (room.building.id <= 0) result += "${scenario.title} 校区 $campusId 存在无教学楼空教室"
                if (room.seats < 0) result += "${scenario.title} ${room.nameZh} 座位数非法"
                if (room.nameZh.isBlank()) result += "${scenario.title} 存在无名称空教室"
            }
        }
        campus.bathrooms.forEach {
            if (it.bathroom.isNullOrBlank()) result += "${scenario.title} 存在无名称浴室"
            if (it.openStatus.isNullOrBlank()) result += "${scenario.title} ${it.bathroom} 缺少开放状态"
        }
        return result
    }

    private fun validatePayment(scenario: MockScenario): List<String> {
        val result = mutableListOf<String>()
        val payment = scenario.payment
        if ((payment.cardMoney.balance ?: 0.0) < 0.0) {
            result += "${scenario.title} 一卡通余额非法"
        }
        if (payment.cardInfo.data.card.isEmpty()) {
            result += "${scenario.title} 缺少一卡通卡片信息"
        }
        if (payment.cardOrderPrefix.isBlank()) {
            result += "${scenario.title} 缺少一卡通订单前缀"
        }
        if (payment.bathroomOrderPrefix.isBlank()) {
            result += "${scenario.title} 缺少浴室订单前缀"
        }
        return result
    }

    private fun validateDiscovery(scenario: MockScenario): List<String> {
        val result = mutableListOf<String>()
        val discovery = scenario.discovery
        discovery.lostFoundItems.forEachIndexed { index, item ->
            if (item.id.isBlank()) result += "${scenario.title} 第 ${index + 1} 条失物招领缺少 id"
            if (item.title.isBlank()) result += "${scenario.title} 第 ${index + 1} 条失物招领缺少标题"
            if (item.state !in setOf(1, 2)) result += "${scenario.title} ${item.id} state 非法"
            if (item.lostType == null) result += "${scenario.title} ${item.id} 缺少类型"
        }
        return result
    }

    private fun scheduleSummary(courses: List<Course>): ScheduleSummary {
        val weekdays = courses.map { it.weekday }.filter { it in 1..7 }.toSet()
        val weeks = courses.flatMap { it.weekIndexes.orEmpty() }
        return ScheduleSummary(
            totalCourses = courses.size,
            weekdaysCovered = weekdays.size,
            minWeek = weeks.minOrNull() ?: 0,
            maxWeek = weeks.maxOrNull() ?: 0
        )
    }

    private fun gradeSummary(grade: Grade): GradeSummary {
        val terms = grade.termGradeList.orEmpty()
        val courses = terms.flatMap { it.gradeList.orEmpty() }
        return GradeSummary(
            termCount = terms.size,
            courseCount = courses.size,
            excellentCount = courses.count {
                val numeric = it.grade?.toDoubleOrNull()
                numeric != null && numeric >= 90.0 || it.grade == "优秀"
            },
            failedCount = courses.count {
                val numeric = it.grade?.toDoubleOrNull()
                numeric != null && numeric < 60.0 || it.grade?.contains("不及格") == true
            },
            retakeCount = courses.count {
                it.courseNature?.contains("重修") == true || it.course?.contains("重修") == true
            }
        )
    }

    private fun examSummary(exams: List<Exam>): ExamSummary {
        val finished = exams.count { it.finished == true }
        val running = exams.count { it.finished != true && it.course?.contains("进行中") == true }
        return ExamSummary(
            total = exams.size,
            running = running,
            upcoming = (exams.size - finished - running).coerceAtLeast(0),
            finished = finished
        )
    }

    private fun MockNetworkMode.label(): String =
        when (this) {
            MockNetworkMode.Success -> "成功"
            MockNetworkMode.Empty -> "空数据"
            MockNetworkMode.Error -> "接口错误"
            MockNetworkMode.Timeout -> "超时"
        }

    private fun MockPaymentMode.label(): String =
        when (this) {
            MockPaymentMode.Success -> "成功"
            MockPaymentMode.InsufficientBalance -> "余额不足"
            MockPaymentMode.PasswordError -> "密码错误"
            MockPaymentMode.GatewayError -> "网关异常"
        }

    private fun MockLoginState.label(): String =
        when (this) {
            MockLoginState.Valid -> "有效"
            MockLoginState.Expired -> "已过期"
            MockLoginState.Guest -> "游客"
        }
}

private data class ScheduleSummary(
    val totalCourses: Int,
    val weekdaysCovered: Int,
    val minWeek: Int,
    val maxWeek: Int
)

private data class GradeSummary(
    val termCount: Int,
    val courseCount: Int,
    val excellentCount: Int,
    val failedCount: Int,
    val retakeCount: Int
)

private data class ExamSummary(
    val total: Int,
    val running: Int,
    val upcoming: Int,
    val finished: Int
)
