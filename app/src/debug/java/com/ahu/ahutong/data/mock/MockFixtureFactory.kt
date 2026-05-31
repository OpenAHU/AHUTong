package com.ahu.ahutong.data.mock

import com.ahu.ahutong.data.crawler.model.adwnh.AllCampus
import com.ahu.ahutong.data.crawler.model.adwnh.AllLostFoundType
import com.ahu.ahutong.data.crawler.model.adwnh.CampusItem
import com.ahu.ahutong.data.crawler.model.adwnh.LostFoundImage
import com.ahu.ahutong.data.crawler.model.adwnh.LostFoundItem
import com.ahu.ahutong.data.crawler.model.adwnh.LostFoundType
import com.ahu.ahutong.data.crawler.model.adwnh.LostFoundTypeItem
import com.ahu.ahutong.data.crawler.model.adwnh.LostFoundUser
import com.ahu.ahutong.data.crawler.model.jwxt.FreeBuilding
import com.ahu.ahutong.data.crawler.model.jwxt.FreeCampus
import com.ahu.ahutong.data.crawler.model.jwxt.FreeRoom
import com.ahu.ahutong.data.crawler.model.jwxt.FreeRoomType
import com.ahu.ahutong.data.crawler.model.jwxt.GetBuildingsResponseItem
import com.ahu.ahutong.data.crawler.model.ycard.Accinfo
import com.ahu.ahutong.data.crawler.model.ycard.CardInfo
import com.ahu.ahutong.data.crawler.model.ycard.Card as YCard
import com.ahu.ahutong.data.crawler.model.ycard.Data as YCardData
import com.ahu.ahutong.data.model.BathRoom
import com.ahu.ahutong.data.model.BathroomTelInfo
import com.ahu.ahutong.data.model.BizTypeAssoc
import com.ahu.ahutong.data.model.Card
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.Data
import com.ahu.ahutong.data.model.Exam
import com.ahu.ahutong.data.model.GpaRankInfo
import com.ahu.ahutong.data.model.GpaSemesterSub
import com.ahu.ahutong.data.model.Grade
import com.ahu.ahutong.data.model.MapData
import com.ahu.ahutong.data.model.ShowData
import com.ahu.ahutong.data.model.StudentAssoc
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object MockFixtureFactory {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun academic(
        variant: AcademicVariant,
        today: LocalDate = LocalDate.now()
    ): MockAcademicProfile =
        when (variant) {
            AcademicVariant.Standard -> standardAcademic(today)
            AcademicVariant.ExamWeek -> examWeekAcademic(today)
            AcademicVariant.NewStudent -> newStudentAcademic(today)
            AcademicVariant.Empty -> emptyAcademic()
            AcademicVariant.HighPerformer -> highPerformerAcademic(today)
            AcademicVariant.Retake -> retakeAcademic(today)
        }

    fun campus(
        variant: CampusVariant,
        today: LocalDate = LocalDate.now()
    ): MockCampusProfile =
        when (variant) {
            CampusVariant.Standard -> standardCampus(today)
            CampusVariant.EmptyClassrooms -> standardCampus(today).copy(
                classroomRooms = standardCampus(today).classroomBuildings.keys.associateWith { emptyList() }
            )
            CampusVariant.NightMaintenance -> standardCampus(today).copy(
                bathrooms = listOf(
                    bathroom("竹园/龙河", "维护中 23:00 后暂停开放"),
                    bathroom("桔园/蕙园", "维护中 23:00 后暂停开放"),
                    bathroom("研究生公寓", "维护中")
                )
            )
            CampusVariant.LongheFocused -> longheFocusedCampus(today)
        }

    fun payment(
        variant: PaymentVariant
    ): MockPaymentProfile =
        when (variant) {
            PaymentVariant.Standard -> paymentProfile(
                balance = 126.35,
                transitionBalance = 20.0,
                cardOrderPrefix = "MOCK-CARD",
                bathroomOrderPrefix = "MOCK-BATH",
                successMessage = "Mock 支付成功",
                failureMessage = "Mock 支付失败"
            )
            PaymentVariant.LowBalance -> paymentProfile(
                balance = 2.40,
                transitionBalance = 0.0,
                cardOrderPrefix = "MOCK-LOW-CARD",
                bathroomOrderPrefix = "MOCK-LOW-BATH",
                successMessage = "Mock 低余额支付成功",
                failureMessage = "余额不足，请先充值"
            )
            PaymentVariant.RichBalance -> paymentProfile(
                balance = 888.88,
                transitionBalance = 100.0,
                cardOrderPrefix = "MOCK-RICH-CARD",
                bathroomOrderPrefix = "MOCK-RICH-BATH",
                successMessage = "Mock 高余额支付成功",
                failureMessage = "Mock 支付失败"
            )
            PaymentVariant.GatewayIssue -> paymentProfile(
                balance = 126.35,
                transitionBalance = 20.0,
                cardOrderPrefix = "MOCK-GATEWAY-CARD",
                bathroomOrderPrefix = "MOCK-GATEWAY-BATH",
                successMessage = "Mock 支付成功",
                failureMessage = "模拟支付网关异常"
            )
        }

    fun discovery(
        variant: DiscoveryVariant
    ): MockDiscoveryProfile =
        when (variant) {
            DiscoveryVariant.Standard -> discoveryProfile(
                lostCount = 14,
                foundCount = 10,
                activeType = "mixed"
            )
            DiscoveryVariant.ActiveLostFound -> discoveryProfile(
                lostCount = 36,
                foundCount = 28,
                activeType = "busy"
            )
            DiscoveryVariant.Empty -> discoveryProfile(
                lostCount = 0,
                foundCount = 0,
                activeType = "empty"
            )
            DiscoveryVariant.OwnerPosts -> discoveryProfile(
                lostCount = 8,
                foundCount = 8,
                activeType = "owner"
            )
        }

    private fun standardAcademic(today: LocalDate): MockAcademicProfile =
        MockAcademicProfile(
            currentSchedule = listOf(
                course("MOCK-001", "移动应用开发", "张老师", "博学南楼 A301", weekday = 1, start = 1, length = 2, weeks = 1..16),
                course("MOCK-002", "数据库系统", "李老师", "文典阁 205", weekday = 1, start = 6, length = 3, weeks = 1..16),
                course("MOCK-003", "计算机网络", "王老师", "笃行北楼 B402", weekday = 2, start = 3, length = 2, weeks = 1..12),
                course("MOCK-004", "操作系统", "陈老师", "博学南楼 A210", weekday = 3, start = 1, length = 2, weeks = 1..16),
                course("MOCK-005", "软件工程实践", "刘老师", "实验中心 503", weekday = 4, start = 8, length = 3, weeks = 3..15),
                course("MOCK-006", "大学体育", "赵老师", "磬苑操场", weekday = 5, start = 3, length = 2, weeks = 1..16),
                course("MOCK-007", "形势与政策", "辅导员", "线上", weekday = 7, start = 11, length = 2, weeks = listOf(4, 8, 12, 16))
            ),
            nextSchedule = listOf(
                course("MOCK-N001", "Android 性能优化", "周老师", "博学南楼 A305", weekday = 2, start = 1, length = 3, weeks = 1..16),
                course("MOCK-N002", "人工智能导论", "吴老师", "文典阁 310", weekday = 4, start = 6, length = 2, weeks = 1..16),
                course("MOCK-N003", "创新创业实践", "学院导师", "众创空间", weekday = 6, start = 3, length = 4, weeks = 5..12)
            ),
            grade = grade(
                totalCredit = "26.0",
                totalGradePoint = "96.75",
                totalGradePointAverage = "3.72",
                terms = listOf(
                    term("2023-2024", "2", 202320242, listOf(
                        gradeItem("COMP2301", "数据结构", "4.0", "4.0", "96", "专业必修", 202320242),
                        gradeItem("MATH2202", "概率论与数理统计", "3.0", "3.7", "91", "学科基础", 202320242),
                        gradeItem("ENGL2002", "大学英语 IV", "2.0", "3.3", "86", "公共必修", 202320242)
                    )),
                    term("2024-2025", "1", 202420251, listOf(
                        gradeItem("COMP3301", "数据库系统", "3.0", "3.8", "93", "专业必修", 202420251),
                        gradeItem("COMP3302", "计算机网络", "3.0", "3.5", "88", "专业必修", 202420251),
                        gradeItem("COMP3303", "软件工程", "2.5", "4.0", "优秀", "专业必修", 202420251),
                        gradeItem("PE3001", "大学体育", "1.0", "3.0", "良好", "公共必修", 202420251)
                    )),
                    term("2024-2025", "2", 202420252, listOf(
                        gradeItem("COMP3401", "移动应用开发", "3.0", "4.0", "97", "专业选修", 202420252),
                        gradeItem("COMP3402", "操作系统", "3.5", "3.6", "90", "专业必修", 202420252),
                        gradeItem("COMP3403", "软件工程实践", "2.0", "3.9", "95", "实践教学", 202420252)
                    ))
                )
            ),
            gpaRankInfo = gpaRank(
                gpa = 3.72,
                rank = 12,
                headCount = 178,
                semesterRanks = listOf(
                    semesterRank(202320242, 3.70, 16, 9.0),
                    semesterRank(202420251, 3.64, 18, 9.5),
                    semesterRank(202420252, 3.84, 8, 8.5)
                )
            ),
            exams = listOf(
                exam("操作系统", "博学南楼 A210", examTime(today, LocalTime.now().minusMinutes(20), LocalTime.now().plusMinutes(80)), "18", false),
                exam("计算机网络", "笃行北楼 B402", examTime(today.plusDays(2), LocalTime.of(9, 0), LocalTime.of(11, 0)), "32", false),
                exam("数据库系统", "文典阁 205", examTime(today.plusDays(5), LocalTime.of(14, 30), LocalTime.of(16, 30)), "07", false),
                exam("软件工程", "博学南楼 A101", examTime(today.minusDays(3), LocalTime.of(8, 0), LocalTime.of(10, 0)), "21", true)
            )
        )

    private fun examWeekAcademic(today: LocalDate): MockAcademicProfile {
        val base = standardAcademic(today)
        return base.copy(
            currentSchedule = base.currentSchedule.take(3).mapIndexed { index, course ->
                Course().apply {
                    setCourseId("EXAM-WEEK-${index + 1}")
                    setName("${course.name} 答疑")
                    setTeacher(course.teacher)
                    setLocation(course.location)
                    setWeekday((index + 1).toString())
                    setStartTime("9")
                    setLength("2")
                    setStartWeek("17")
                    setEndWeek("17")
                    setWeekIndexes(listOf(17))
                    setExtra("考试周答疑")
                }
            },
            exams = listOf(
                exam("高等数学 A", "博学南楼 A101", examTime(today, LocalTime.of(8, 30), LocalTime.of(10, 30)), "01", false),
                exam("大学英语 IV", "文典阁 201", examTime(today.plusDays(1), LocalTime.of(14, 0), LocalTime.of(16, 0)), "26", false),
                exam("计算机组成原理", "笃行北楼 B301", examTime(today.plusDays(2), LocalTime.of(9, 0), LocalTime.of(11, 0)), "14", false),
                exam("数据库系统", "博学南楼 A305", examTime(today.plusDays(3), LocalTime.of(14, 30), LocalTime.of(16, 30)), "39", false),
                exam("操作系统", "实验中心 402", examTime(today.plusDays(5), LocalTime.of(9, 0), LocalTime.of(11, 0)), "08", false)
            )
        )
    }

    private fun newStudentAcademic(today: LocalDate): MockAcademicProfile =
        MockAcademicProfile(
            currentSchedule = listOf(
                course("FRESH-001", "大学计算机基础", "许老师", "龙河教学楼 101", weekday = 1, start = 1, length = 2, weeks = 1..16),
                course("FRESH-002", "高等数学 A1", "赵老师", "龙河教学楼 203", weekday = 2, start = 3, length = 2, weeks = 1..16),
                course("FRESH-003", "大学英语 I", "孙老师", "龙河教学楼 305", weekday = 3, start = 6, length = 2, weeks = 1..16),
                course("FRESH-004", "军事理论", "辅导员", "线上", weekday = 5, start = 11, length = 2, weeks = listOf(2, 4, 6, 8))
            ),
            nextSchedule = listOf(
                course("FRESH-N001", "程序设计基础", "黄老师", "龙河实验楼 402", weekday = 1, start = 3, length = 3, weeks = 1..16),
                course("FRESH-N002", "线性代数", "郑老师", "龙河教学楼 204", weekday = 4, start = 1, length = 2, weeks = 1..16)
            ),
            grade = grade(
                totalCredit = "0.0",
                totalGradePoint = "0.0",
                totalGradePointAverage = "0.0",
                terms = listOf(
                    term("2025-2026", "1", 202520261, emptyList())
                )
            ),
            gpaRankInfo = gpaRank(
                gpa = 0.0,
                rank = 0,
                headCount = 186,
                semesterRanks = emptyList(),
                updated = "暂无排名"
            ),
            exams = listOf(
                exam("大学计算机基础", "龙河教学楼 101", examTime(today.plusDays(22), LocalTime.of(9, 0), LocalTime.of(11, 0)), "待公布", false)
            )
        )

    private fun emptyAcademic(): MockAcademicProfile =
        MockAcademicProfile(
            currentSchedule = emptyList(),
            nextSchedule = emptyList(),
            grade = grade("0.0", "0.0", "0.0", emptyList()),
            gpaRankInfo = gpaRank(0.0, 0, 0, emptyList(), "暂无数据"),
            exams = emptyList()
        )

    private fun highPerformerAcademic(today: LocalDate): MockAcademicProfile =
        standardAcademic(today).copy(
            grade = grade(
                totalCredit = "31.0",
                totalGradePoint = "122.40",
                totalGradePointAverage = "3.95",
                terms = listOf(
                    term("2024-2025", "1", 202420251, listOf(
                        gradeItem("COMP3301", "数据库系统", "3.0", "4.0", "99", "专业必修", 202420251),
                        gradeItem("COMP3302", "计算机网络", "3.0", "4.0", "98", "专业必修", 202420251),
                        gradeItem("COMP3303", "软件工程", "2.5", "4.0", "优秀", "专业必修", 202420251),
                        gradeItem("COMP3304", "算法设计", "3.0", "3.9", "96", "专业选修", 202420251)
                    )),
                    term("2024-2025", "2", 202420252, listOf(
                        gradeItem("COMP3401", "移动应用开发", "3.0", "4.0", "100", "专业选修", 202420252),
                        gradeItem("COMP3402", "操作系统", "3.5", "3.9", "97", "专业必修", 202420252),
                        gradeItem("COMP3403", "软件工程实践", "2.0", "4.0", "优秀", "实践教学", 202420252)
                    ))
                )
            ),
            gpaRankInfo = gpaRank(
                gpa = 3.95,
                rank = 1,
                headCount = 178,
                semesterRanks = listOf(
                    semesterRank(202420251, 3.96, 1, 11.5),
                    semesterRank(202420252, 3.94, 2, 8.5)
                )
            )
        )

    private fun retakeAcademic(today: LocalDate): MockAcademicProfile =
        standardAcademic(today).copy(
            grade = grade(
                totalCredit = "24.0",
                totalGradePoint = "68.50",
                totalGradePointAverage = "2.85",
                terms = listOf(
                    term("2023-2024", "2", 202320242, listOf(
                        gradeItem("MATH2202", "概率论与数理统计", "3.0", "1.3", "62", "学科基础", 202320242),
                        gradeItem("COMP2301", "数据结构", "4.0", "2.0", "70", "专业必修", 202320242),
                        gradeItem("PHYS2001", "大学物理", "3.0", "0.0", "不及格", "学科基础", 202320242)
                    )),
                    term("2024-2025", "1", 202420251, listOf(
                        gradeItem("PHYS2001-R", "大学物理（重修）", "3.0", "2.7", "78", "重修", 202420251),
                        gradeItem("COMP3301", "数据库系统", "3.0", "3.0", "82", "专业必修", 202420251),
                        gradeItem("COMP3302", "计算机网络", "3.0", "2.3", "74", "专业必修", 202420251)
                    ))
                )
            ),
            gpaRankInfo = gpaRank(
                gpa = 2.85,
                rank = 122,
                headCount = 178,
                semesterRanks = listOf(
                    semesterRank(202320242, 1.58, 168, 10.0),
                    semesterRank(202420251, 2.67, 130, 9.0)
                )
            )
        )

    private fun standardCampus(today: LocalDate): MockCampusProfile {
        val campus = campusList()
        val buildings = mapOf(
            1 to listOf(
                building(101, "QY-BXNL", "博学南楼"),
                building(102, "QY-DXBL", "笃行北楼"),
                building(103, "QY-WDG", "文典阁"),
                building(104, "QY-SYZX", "实验中心")
            ),
            2 to listOf(
                building(201, "LH-JXL", "龙河教学楼"),
                building(202, "LH-SY", "龙河实验楼"),
                building(203, "LH-TSG", "龙河图书馆")
            )
        )
        return MockCampusProfile(
            campuses = campus,
            classroomBuildings = buildings,
            classroomRooms = buildings.mapValues { (campusId, list) ->
                list.flatMapIndexed { index, item ->
                    listOf(
                        freeRoom(campusId, item, index * 100 + 1, 2, "201", 72, "多媒体教室", today),
                        freeRoom(campusId, item, index * 100 + 2, 3, "305", 48, "普通教室", today),
                        freeRoom(campusId, item, index * 100 + 3, 5, "512", 96, "阶梯教室", today)
                    )
                }
            },
            bathrooms = listOf(
                bathroom("竹园/龙河", "开放中 06:30-23:00"),
                bathroom("桔园/蕙园", "开放中 06:30-23:00"),
                bathroom("研究生公寓", "维护中")
            ),
            bathroomAccounts = mapOf(
                "竹园/龙河" to bathroomAccount("竹园/龙河", "13800000000", projectId = 409, cash = 18.60, gift = 2.00),
                "桔园/蕙园" to bathroomAccount("桔园/蕙园", "13800000000", projectId = 430, cash = 12.30, gift = 1.50)
            ),
            calendarJpegBase64 = tinyJpegBase64
        )
    }

    private fun longheFocusedCampus(today: LocalDate): MockCampusProfile {
        val base = standardCampus(today)
        return base.copy(
            classroomBuildings = mapOf(
                1 to emptyList(),
                2 to listOf(
                    building(201, "LH-JXL", "龙河教学楼"),
                    building(202, "LH-SY", "龙河实验楼"),
                    building(203, "LH-TSG", "龙河图书馆"),
                    building(204, "LH-WL", "龙河物理楼")
                )
            )
        )
    }

    private fun paymentProfile(
        balance: Double,
        transitionBalance: Double,
        cardOrderPrefix: String,
        bathroomOrderPrefix: String,
        successMessage: String,
        failureMessage: String
    ): MockPaymentProfile =
        MockPaymentProfile(
            cardMoney = Card().apply {
                this.balance = balance
                this.transitionBalance = transitionBalance
            },
            cardInfo = cardInfo(balance),
            cardOrderPrefix = cardOrderPrefix,
            bathroomOrderPrefix = bathroomOrderPrefix,
            successMessage = successMessage,
            failureMessage = failureMessage
        )

    private fun discoveryProfile(
        lostCount: Int,
        foundCount: Int,
        activeType: String
    ): MockDiscoveryProfile {
        val campuses = campusList()
        val types = lostFoundTypes()
        val lostItems = (1..lostCount).map { index ->
            lostFoundItem(
                id = "mock-lost-$activeType-$index",
                state = 1,
                title = lostTitle(index),
                typeId = ((index % 4) + 1).toString(),
                campusId = if (index % 3 == 0) "2" else "1",
                location = lostLocation(index),
                detail = lostDetail(index),
                typeName = typeName((index % 4) + 1),
                ownerMode = activeType == "owner",
                createdOffsetHours = index.toLong()
            )
        }
        val foundItems = (1..foundCount).map { index ->
            lostFoundItem(
                id = "mock-find-$activeType-$index",
                state = 2,
                title = foundTitle(index),
                typeId = ((index % 4) + 1).toString(),
                campusId = if (index % 2 == 0) "2" else "1",
                location = foundLocation(index),
                detail = foundDetail(index),
                typeName = typeName((index % 4) + 1),
                ownerMode = activeType == "owner",
                createdOffsetHours = index.toLong() + 2L
            )
        }
        return MockDiscoveryProfile(
            lostFoundCampuses = campuses,
            lostFoundTypes = types,
            lostFoundItems = lostItems + foundItems
        )
    }

    private fun course(
        id: String,
        name: String,
        teacher: String,
        location: String,
        weekday: Int,
        start: Int,
        length: Int,
        weeks: IntRange
    ): Course = course(id, name, teacher, location, weekday, start, length, weeks.toList())

    private fun course(
        id: String,
        name: String,
        teacher: String,
        location: String,
        weekday: Int,
        start: Int,
        length: Int,
        weeks: List<Int>
    ): Course = Course().apply {
        setCourseId(id)
        setName(name)
        setTeacher(teacher)
        setLocation(location)
        setWeekday(weekday.toString())
        setStartTime(start.toString())
        setLength(length.toString())
        setStartWeek((weeks.minOrNull() ?: 1).toString())
        setEndWeek((weeks.maxOrNull() ?: 16).toString())
        setWeekIndexes(weeks)
        setExtra(if (weeks.size < 16) "第${weeks.joinToString("、")}周" else "")
    }

    private fun grade(
        totalCredit: String,
        totalGradePoint: String,
        totalGradePointAverage: String,
        terms: List<Grade.TermGradeListBean>
    ): Grade = Grade().apply {
        this.totalCredit = totalCredit
        this.totalGradePoint = totalGradePoint
        this.totalGradePointAverage = totalGradePointAverage
        termGradeList = terms
    }

    private fun term(
        schoolYear: String,
        term: String,
        semesterId: Int,
        items: List<Grade.TermGradeListBean.GradeListBean>
    ): Grade.TermGradeListBean {
        val totalCredit = items.sumOf { it.credit.toDoubleOrNull() ?: 0.0 }
        val weighted = items.sumOf {
            (it.credit.toDoubleOrNull() ?: 0.0) * (it.gradePoint.toDoubleOrNull() ?: 0.0)
        }
        return Grade.TermGradeListBean().apply {
            this.schoolYear = schoolYear
            this.term = term
            gradeList = items.onEach { it.semesterId = semesterId }
            termTotalCredit = "%.1f".format(totalCredit)
            termGradePoint = "%.2f".format(weighted)
            termGradePointAverage = if (totalCredit > 0.0) "%.2f".format(weighted / totalCredit) else "0.0"
        }
    }

    private fun gradeItem(
        courseNum: String,
        course: String,
        credit: String,
        gradePoint: String,
        grade: String,
        courseNature: String,
        semesterId: Int
    ): Grade.TermGradeListBean.GradeListBean = Grade.TermGradeListBean.GradeListBean().apply {
        this.courseNum = courseNum
        this.course = course
        this.credit = credit
        this.gradePoint = gradePoint
        this.grade = grade
        this.courseNature = courseNature
        this.semesterId = semesterId
    }

    private fun gpaRank(
        gpa: Double,
        rank: Int,
        headCount: Int,
        semesterRanks: List<GpaSemesterSub>,
        updated: String = "2026-05-31 09:00"
    ): GpaRankInfo = GpaRankInfo(
        id = 10086,
        gpa = gpa,
        majorRank = rank,
        majorHeadCount = headCount,
        inPlanCredits = semesterRanks.sumOf { it.inPlanCredits },
        outPlanCredits = 0.0,
        totalCredits = semesterRanks.sumOf { it.totalCredits },
        updatedDateTimeStr = updated,
        studentAssoc = StudentAssoc(id = 10086),
        bizTypeAssoc = BizTypeAssoc(id = 1),
        gpaSemesterSubs = semesterRanks
    )

    private fun semesterRank(
        semesterId: Int,
        gpa: Double,
        rank: Int,
        credits: Double
    ): GpaSemesterSub = GpaSemesterSub(
        gpa = gpa,
        semesterId = semesterId,
        majorRank = rank,
        inPlanCredits = credits,
        outPlanCredits = 0.0,
        totalCredits = credits
    )

    private fun exam(
        course: String,
        location: String,
        time: String,
        seat: String,
        finished: Boolean
    ): Exam = Exam().apply {
        this.course = course
        this.location = location
        this.time = time
        seatNum = seat
        this.finished = finished
    }

    private fun examTime(
        date: LocalDate,
        start: LocalTime,
        end: LocalTime
    ): String = "${date.format(dateFormatter)} ${start.format(timeFormatter)}~${end.format(timeFormatter)}"

    private fun campusList(): AllCampus =
        AllCampus(
            code = 0,
            msg = "success",
            `object` = listOf(
                CampusItem(id = "1", campusName = "磬苑校区", campusOrder = 1, createTime = "2024-09-01", createUser = "mock"),
                CampusItem(id = "2", campusName = "龙河校区", campusOrder = 2, createTime = "2024-09-01", createUser = "mock")
            )
        )

    private fun lostFoundTypes(): AllLostFoundType =
        AllLostFoundType(
            code = 0,
            msg = "success",
            `object` = listOf(
                lostFoundTypeItem("1", "校园卡"),
                lostFoundTypeItem("2", "电子设备"),
                lostFoundTypeItem("3", "证件资料"),
                lostFoundTypeItem("4", "生活用品")
            )
        )

    private fun lostFoundTypeItem(id: String, name: String): LostFoundTypeItem =
        LostFoundTypeItem(
            typeId = id,
            typeName = name,
            typeState = "1",
            createtime = "2026-05-31 09:00:00",
            createuser = "mock",
            updatetime = null,
            updateuser = null,
            remark = null
        )

    private fun lostFoundItem(
        id: String,
        state: Int,
        title: String,
        typeId: String,
        campusId: String,
        location: String,
        detail: String,
        typeName: String,
        ownerMode: Boolean,
        createdOffsetHours: Long
    ): LostFoundItem = LostFoundItem(
        id = id,
        title = "$title - $detail",
        phone = if (ownerMode) "13800000000" else "1390000${(1000..9999).random()}",
        linkman = if (ownerMode) "Mock 本人" else "Mock 同学",
        createuser = if (ownerMode) "U20260001" else "U2026${(1000..9999).random()}",
        createtime = LocalDateTime.now().minusHours(createdOffsetHours).format(dateTimeFormatter),
        state = state,
        audituser = "mock-auditor",
        auditresult = "1",
        typeid = typeId,
        campusid = campusId,
        num1 = location,
        num2 = null,
        campusName = if (campusId == "2") "龙河校区" else "磬苑校区",
        audituserName = "系统审核",
        imgs = listOf(
            LostFoundImage(
                imgId = "$id-img",
                imgPath = "https://mock.ahu.edu.cn/images/$id.jpg",
                createtime = LocalDateTime.now().minusHours(createdOffsetHours).format(dateTimeFormatter),
                lostid = id
            )
        ),
        pubuser = LostFoundUser(
            idNumber = if (ownerMode) "U20260001" else "U2026${(1000..9999).random()}",
            unitUid = "CS",
            unitName = "计算机科学与技术学院",
            userName = if (ownerMode) "Mock 本人" else "Mock 同学",
            mobile = "13800000000",
            headimgurl = null
        ),
        lostType = LostFoundType(
            typeId = typeId,
            typeName = typeName,
            typeState = "1",
            createtime = "2026-05-31 09:00:00",
            createuser = "mock",
            updatetime = null,
            updateuser = null,
            remark = null
        )
    )

    private fun typeName(id: Int): String =
        when (id) {
            1 -> "校园卡"
            2 -> "电子设备"
            3 -> "证件资料"
            else -> "生活用品"
        }

    private fun lostTitle(index: Int): String =
        when (index % 8) {
            0 -> "在博学南楼捡到校园卡"
            1 -> "文典阁三楼捡到 U 盘"
            2 -> "实验中心捡到蓝牙耳机"
            3 -> "磬苑食堂捡到钥匙"
            4 -> "笃行北楼捡到学生证"
            5 -> "操场看台捡到水杯"
            6 -> "校车站捡到雨伞"
            else -> "图书馆捡到笔记本"
        }

    private fun foundTitle(index: Int): String =
        when (index % 8) {
            0 -> "寻找校园卡和钥匙"
            1 -> "寻找黑色 U 盘"
            2 -> "寻找白色耳机"
            3 -> "寻找课程教材"
            4 -> "寻找身份证件"
            5 -> "寻找保温杯"
            6 -> "寻找折叠伞"
            else -> "寻找课堂笔记"
        }

    private fun lostLocation(index: Int): String =
        when (index % 6) {
            0 -> "博学南楼一楼大厅"
            1 -> "文典阁 3F 自习区"
            2 -> "实验中心 5F"
            3 -> "磬苑食堂二楼"
            4 -> "笃行北楼 B402"
            else -> "龙河校区篮球场"
        }

    private fun foundLocation(index: Int): String =
        when (index % 6) {
            0 -> "磬苑食堂附近"
            1 -> "龙河教学楼 204"
            2 -> "文典阁入口"
            3 -> "博学南楼 A301"
            4 -> "校车站"
            else -> "实验中心机房"
        }

    private fun lostDetail(index: Int): String =
        when (index % 5) {
            0 -> "已交给楼栋值班室"
            1 -> "请描述外观后领取"
            2 -> "物品保存在学院办公室"
            3 -> "可在晚自习后联系"
            else -> "请失主带有效证件联系"
        }

    private fun foundDetail(index: Int): String =
        when (index % 5) {
            0 -> "蓝色挂绳，钥匙上有小标签"
            1 -> "内有课程资料"
            2 -> "外壳有轻微划痕"
            3 -> "书内夹有课堂笔记"
            else -> "最后一次看到是在自习区"
        }

    private fun building(id: Int, code: String, name: String): GetBuildingsResponseItem =
        GetBuildingsResponseItem(
            code = code,
            enabled = true,
            id = id,
            nameEn = code,
            nameZh = name
        )

    private fun freeRoom(
        campusId: Int,
        buildingItem: GetBuildingsResponseItem,
        id: Int,
        floor: Int,
        roomName: String,
        seats: Int,
        typeName: String,
        today: LocalDate
    ): FreeRoom {
        val campus = FreeCampus(
            code = if (campusId == 2) "LH" else "QY",
            id = campusId,
            nameEn = if (campusId == 2) "Longhe" else "Qingyuan",
            nameZh = if (campusId == 2) "龙河校区" else "磬苑校区"
        )
        val building = FreeBuilding(
            campus = campus,
            code = buildingItem.code,
            id = buildingItem.id,
            nameEn = buildingItem.nameEn,
            nameZh = buildingItem.nameZh
        )
        return FreeRoom(
            building = building,
            code = "${buildingItem.code}-$roomName",
            date = today.toString(),
            floor = floor,
            id = buildingItem.id * 1000 + id,
            mngtDepartAssoc = 0,
            nameEn = roomName,
            nameZh = "$roomName 教室",
            remark = "Mock：可容纳 $seats 人",
            roomType = FreeRoomType(
                code = "NORMAL",
                id = 1,
                nameEn = typeName,
                nameZh = typeName
            ),
            seats = seats,
            seatsForLesson = seats,
            units = "1-13",
            virtual = false,
            week = 1,
            weekNum = 1,
            weekday = today.dayOfWeek.value
        )
    }

    private fun bathroom(name: String, status: String): BathRoom =
        BathRoom().apply {
            bathroom = name
            openStatus = status
        }

    private fun bathroomAccount(
        bathroom: String,
        phone: String,
        projectId: Int,
        cash: Double,
        gift: Double
    ): BathroomTelInfo =
        BathroomTelInfo(
            msg = "success",
            code = 0,
            message = null,
            map = MapData(
                showData = ShowData(
                    phone = phone,
                    cashAmount = "%.2f".format(cash),
                    giftAmount = "%.2f".format(gift)
                ),
                data = Data(
                    projectId = projectId,
                    projectName = bathroom,
                    accountId = 20260531,
                    telPhone = phone,
                    identifier = "mock-$phone-$projectId",
                    sex = "未知",
                    name = "Mock 用户",
                    statusId = 1,
                    accountMoney = (cash * 100).toInt(),
                    accountGivenMoney = (gift * 100).toInt(),
                    alias = "安大通模拟浴室账户",
                    tags = "mock",
                    isCard = 0,
                    cardStatusId = 1,
                    isUseCode = 1,
                    cardPhysicalId = null,
                    tsmAbstract = "Mock bathroom account",
                    myCustomInfo = null,
                    message = null
                )
            )
        )

    private fun cardInfo(balance: Double): CardInfo =
        CardInfo(
            code = 200,
            msg = "success",
            success = true,
            data = YCardData(
                account = "mock-account",
                errmsg = "",
                retcode = "0",
                sno = "U20260001",
                card = listOf(
                    YCard(
                        acc_status = 1,
                        accinfo = listOf(
                            Accinfo(
                                autotrans_amt = 0,
                                autotrans_flag = 0,
                                autotrans_limite = 0,
                                balance = (balance * 100).toInt(),
                                daycostamt = 1580,
                                daycostlimit = 5000,
                                name = "主钱包",
                                nonpwdlimit = 30,
                                singlelimit = 100,
                                type = "01"
                            )
                        ),
                        account = "mock-account",
                        acctId = "mock-acct",
                        auth_code_background = "",
                        auth_code_font_color = "",
                        autotrans_amt = 0,
                        autotrans_flag = 0,
                        autotrans_limite = 0,
                        bankacc = "",
                        barflag = 1,
                        card_background = "",
                        card_font_color = "",
                        card_logo = "",
                        card_name = "安徽大学校园卡",
                        card_name_en = "AHU Campus Card",
                        cardname = "校园卡",
                        cardtype = "本科生卡",
                        cert = "340000********1234",
                        createdate = "2024-09-01",
                        custId = "mock-cust",
                        custMemberId = "mock-member",
                        daycostlimit = 5000,
                        db_balance = (balance * 100).toInt(),
                        debitamt = 0,
                        department_name = "计算机科学与技术学院",
                        elec_accamt = 0,
                        expdate = "2028-07-01",
                        flag = "1",
                        freezeflag = 0,
                        idflag = 1,
                        lostflag = 0,
                        mscard = 0,
                        name = "Mock 用户",
                        nonpwdlimit = 30,
                        phone = "13800000000",
                        scbkbs = 0,
                        schcode = "10357",
                        singlelimit = 100,
                        sno = "U20260001",
                        unsettle_amount = 0,
                        voucher = "",
                        voucherStatus = 0
                    )
                )
            )
        )

    const val tinyJpegBase64 =
        "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////2wBDAf//////////////////////////////////////////////////////////////////////////////////////wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAX/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIQAxAAAAH/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEBAAEFAqf/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oACAEDAQE/ASP/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oACAECAQE/ASP/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEBAAY/Ar//xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oACAEBAAE/IV//2gAMAwEAAgADAAAAEP/EFBQRAQAAAAAAAAAAAAAAAAAAARD/2gAIAQMBAT8QH//EFBQRAQAAAAAAAAAAAAAAAAAAARD/2gAIAQIBAT8QH//EFBABAQAAAAAAAAAAAAAAAAAAARD/2gAIAQEAAT8QH//Z"
}

enum class AcademicVariant {
    Standard,
    ExamWeek,
    NewStudent,
    Empty,
    HighPerformer,
    Retake
}

enum class CampusVariant {
    Standard,
    EmptyClassrooms,
    NightMaintenance,
    LongheFocused
}

enum class PaymentVariant {
    Standard,
    LowBalance,
    RichBalance,
    GatewayIssue
}

enum class DiscoveryVariant {
    Standard,
    ActiveLostFound,
    Empty,
    OwnerPosts
}
