package com.ahu.ahutong.data.crawler

import android.util.Log
import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.base.BaseDataSource
import com.ahu.ahutong.data.crawler.api.adwmh.AdwmhApi
import com.ahu.ahutong.data.crawler.api.jwxt.JwxtApi
import com.ahu.ahutong.data.crawler.api.ycard.YcardApi
import com.ahu.ahutong.data.crawler.model.adwnh.AllCampus
import com.ahu.ahutong.data.crawler.model.adwnh.AllLostFoundType
import com.ahu.ahutong.data.crawler.model.adwnh.LostFoundPublishRequest
import com.ahu.ahutong.data.crawler.model.adwnh.LostFoundResponse
import com.ahu.ahutong.data.crawler.model.jwxt.CourseTable
import com.ahu.ahutong.data.crawler.model.jwxt.CurrentSemester
import com.ahu.ahutong.data.crawler.model.ycard.CardInfo
import com.ahu.ahutong.data.crawler.model.ycard.RequestBody
import com.ahu.ahutong.data.crawler.utils.GpaRankHtmlParser
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.BathRoom
import com.ahu.ahutong.data.model.BathroomTelInfo
import com.ahu.ahutong.data.model.Card
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.Exam
import com.ahu.ahutong.data.model.GpaRankInfo
import com.ahu.ahutong.data.model.Grade
import com.ahu.ahutong.data.server.AhuTong
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.FormBody
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Response
import kotlin.text.Regex


class CrawlerDataSource : BaseDataSource {

    val TAG = this::class.java.simpleName

    override suspend fun getSchedule(
        schoolYear: String,
        schoolTerm: String
    ): AHUResponse<List<Course>> {
        return AHUResponse<List<Course>>()
    }

    override suspend fun getSchedule(): AHUResponse<List<Course>> {
        val currentSemesterJson = getCurrentSemester()
        val courseTable = JwxtApi.API.getCourse(currentSemesterJson.id, currentSemesterJson.id)

        AHUCache.saveSchoolTerm(currentSemesterJson.name)

        return AHUResponse<List<Course>>().apply {
            data = courseTable.toCourseList()
            code = 0
            msg = ""
        }
    }

    override suspend fun getNextSchedule(): AHUResponse<List<Course>> {
        val currentSemesterJson = getCurrentSemester()
        val nextCourseTable = JwxtApi.API.getCourse(currentSemesterJson.id + 20, currentSemesterJson.id)

        return AHUResponse<List<Course>>().apply {
            data = nextCourseTable.toCourseList()
            code = 0
            msg = ""
        }
    }

    private suspend fun getCurrentSemester(): CurrentSemester {
        val basicInfo = JwxtApi.API.fetchCourseTableBasicInfo()
        val doc = Jsoup.parse(basicInfo.body()!!.string())

        val element = doc.select("script")
            .map { it.data() }
            .firstOrNull { it.contains("var semesters = JSON.parse") && it.contains("var currentSemester") }


        if (element == null) {
            throw IllegalStateException("Cannot find current semester script")
        }
        val currentSemesterPattern = Regex("var\\s+currentSemester\\s*=\\s*(\\{.*?\\});")
        val currentSemester = currentSemesterPattern.find(element)
            ?: throw IllegalStateException("Cannot parse current semester")


        val gson = Gson()

        val currentSemesterJson = gson.fromJson(
            currentSemester!!.groups[1]!!.value.replace("\\\"", "\""),
            CurrentSemester::class.java
        )

        return currentSemesterJson
    }

    private fun CourseTable.toCourseList(): List<Course> {
        val courseList = ArrayList<Course>()
        studentTableVms.firstOrNull()?.activities.orEmpty().forEach {
            val sortedWeekIndexes = it.weekIndexes.sorted()
            if (sortedWeekIndexes.isEmpty()) {
                return@forEach
            }

            val course = Course()
            course.name = it.courseName
            course.setStartWeek(sortedWeekIndexes.first().toString())
            course.setLength((it.endUnit - it.startUnit + 1).toString())
            course.setWeekday(it.weekday.toString())
            course.setEndWeek(sortedWeekIndexes.last().toString())
            course.setStartTime(it.startUnit.toString())
            course.location = it.room ?: "未知"
            course.teacher = it.teacherNames.joinToString(", ")
            course.weekIndexes = sortedWeekIndexes
            course.courseId = it.lessonId.toString()

            Log.e(TAG, "getSchedule: $course")
            courseList.add(course)
        }
        return courseList
    }

    override suspend fun getGrade(): AHUResponse<Grade> {
        var id = AHUCache.getJwxtStudentId()
        id = id ?: getStudentId().also { AHUCache.setJwxtStudentId(it) }

        val data = JwxtApi.API.getGrade(id)
        val map = hashMapOf<String, Grade.TermGradeListBean>()

        data.semesterId2studentGrades?.values?.forEach { gradeList ->
            val newGradeList = mutableListOf<Grade.TermGradeListBean.GradeListBean>()
            var termName: String? = null

            gradeList.forEach { it ->

                termName = termName ?: it.semesterName
                val grade = Grade.TermGradeListBean.GradeListBean()
                grade.course = it.courseName
                grade.credit = it.credits.toString()
                grade.grade = it.gaGrade
                grade.gradePoint = it.gp.toString()
                grade.courseNature = it.courseType
                grade.courseNum = it.courseCode
                grade.semesterId = it.semesterId!!
                grade.gradeDetail = it.gradeDetail
                newGradeList.add(grade)
            }

            termName?.let {

                val names = termName.split("-")
                if (names.size < 3) { //2023-2024-1
                    return@forEach
                }

                val termGradeList = Grade.TermGradeListBean()
                termGradeList.gradeList = newGradeList
                termGradeList.term = names[2]
                termGradeList.schoolYear = "${names[0]}-${names[1]}"
                termGradeList.termGradePoint = newGradeList.sumOf { it ->
                    it.grade?.toDoubleOrNull() ?: 0.0
                }.toString()
                termGradeList.termTotalCredit = newGradeList.sumOf { it ->
                    it.credit?.toDoubleOrNull() ?: 0.0
                }.toString()
                val totalGradePointWeighted = newGradeList.sumOf {
                    (it.gradePoint?.toDoubleOrNull() ?: 0.0) * (it.credit?.toDoubleOrNull() ?: 0.0)
                }

                termGradeList.termGradePointAverage =
                    if (termGradeList.termTotalCredit.toDouble() > 0) {
                        "%.2f".format(totalGradePointWeighted / termGradeList.termTotalCredit.toDouble())
                    } else {
                        "0.0"
                    }

                map[it] = termGradeList
            }

        }

        val response = AHUResponse<Grade>()
        val termGradeList = map.values.toList()
        val grade = Grade()

        grade.totalCredit = termGradeList.sumOf {
            it.termTotalCredit?.toDoubleOrNull() ?: 0.0
        }.toString()

        grade.totalGradePoint = termGradeList.sumOf {
            val avg = it.termGradePointAverage?.toDoubleOrNull() ?: 0.0
            val credit = it.termTotalCredit?.toDoubleOrNull() ?: 0.0
            avg * credit
        }.toString()

        val weightedGradePointSum = termGradeList.sumOf {
            val avg = it.termGradePointAverage?.toDoubleOrNull() ?: 0.0
            val credit = it.termTotalCredit?.toDoubleOrNull() ?: 0.0
            avg * credit
        }


        grade.totalGradePointAverage = if (grade.totalCredit.toDouble() > 0) {
            "%.2f".format(weightedGradePointSum / grade.totalCredit.toDouble())
        } else {
            "0.0"
        }

        grade.termGradeList = termGradeList


        response.data = grade
        response.code = 0


        return response
    }

    override suspend fun getGpaRankFromHtml(): AHUResponse<GpaRankInfo> {
        val response = AHUResponse<GpaRankInfo>()
        try {
            val htmlResponse = JwxtApi.API.getGrade()
            if(!htmlResponse.isSuccessful||htmlResponse.body() == null){
                response.code = -1
                response.msg = "获取成绩页面失败"
                return response
            }

            val html = htmlResponse.body()!!.string()
            val jsObject = GpaRankHtmlParser.extractModelObject(html)

            val json = convertJsToJson(jsObject)
            val gpaRankInfo = Gson().fromJson(json, GpaRankInfo::class.java)


            response.code = 0
            response.msg = "success"
            response.data = gpaRankInfo
            return response

        }catch (e: Exception){
            e.printStackTrace()
            response.code = -1
            response.msg = "解析失败：${e.message}"
            return response
        }
    }

    /**
     * 将 JS 对象字符串 转换为 标准 JSON 字符串
     */
    private fun convertJsToJson(js: String): String {
        return js
            .replace(Regex("'"), "\"")                // 单引号 → 双引号
    }

    override suspend fun getAllCampus(): AHUResponse<AllCampus> {
        val response = AHUResponse<AllCampus>()
        try {
            // 直接请求 JSON 接口
            val campusList = AdwmhApi.API.getAllcampus()

            // 封装返回
            response.code = 0
            response.msg = "success"
            response.data = campusList
            return response

        } catch (e: Exception) {
            e.printStackTrace()
            response.code = -1
            response.msg = "解析校区列表失败：${e.message}"
            return response
        }
    }

    override suspend fun getAllLostFoundType(): AHUResponse<AllLostFoundType> {
        val response = AHUResponse<AllLostFoundType>()
        try {
            // 直接请求 JSON 接口
            val typeList = AdwmhApi.API.getAlllostfoundtype()
            // 封装返回
            response.code = 0
            response.msg = "success"
            response.data = typeList
            return response

        } catch (e: Exception) {
            e.printStackTrace()
            response.code = -1
            response.msg = "解析失败：${e.message}"
            return response
        }
    }

    override suspend fun getLostFoundList(
        pageNo: Int,
        pageSize: Int,
        state: Int
    ): AHUResponse<LostFoundResponse> {
        val response = AHUResponse<LostFoundResponse>()
        try {
            // 直接请求 JSON 接口
            val List = AdwmhApi.API.getLostFoundList(
                pageNo,
                pageSize,
                state
            )
            // 封装返回
            response.code = 0
            response.msg = "success"
            response.data = List
            return response

        } catch (e: Exception) {
            e.printStackTrace()
            response.code = -1
            response.msg = "解析失败：${e.message}"
            return response
        }
    }
    override suspend fun publishLostFound(
        request: LostFoundPublishRequest
    ): AHUResponse<Any> {
        return AdwmhApi.API.publishLostFound(request)
    }
    override suspend fun deleteLostFound(
        id: String
    ): AHUResponse<Any> {
        return AdwmhApi.API.deleteLostFound(id)
    }

    override suspend fun getCardMoney(): AHUResponse<Card> {
        val card = Card()
        card.balance = AdwmhApi.API.getBalance().`object`
        val result = AHUResponse<Card>();
        result.data = card
        result.code = 0
        return result
    }

    override suspend fun getBathRooms(): AHUResponse<List<BathRoom>> {
        return AHUResponse<List<BathRoom>>()
    }

    override suspend fun getExamInfo(
        studentID: String,
        studentName: String
    ): AHUResponse<List<Exam>> {
        return try {
            val res = JwxtApi.API.fetchExamArrangePage()
            if (!res.isSuccessful || res.body() == null) {
                AHUResponse<List<Exam>>().apply {
                    code = -1
                    msg = "请求失败"
                    data = emptyList()
                }
            } else {
                val html = res.body()!!.string()

                // Try new HTML table format first (post-redesign: server-rendered <tr> elements)
                val tableExams = parseExamTableHtml(html)
                if (tableExams.isNotEmpty()) {
                    AHUResponse<List<Exam>>().apply {
                        code = 0
                        data = tableExams
                        msg = ""
                    }
                } else {
                    // Fallback: old format with studentExamInfoVms JS variable
                    val regex = Regex("(?s)studentExamInfoVms\\s*=\\s*(\\[.*?]);")
                    val match = regex.find(html)
                    if (match == null) {
                        AHUResponse<List<Exam>>().apply {
                            code = 0
                            msg = "未发现考试信息"
                            data = emptyList()
                        }
                    } else {
                        val jsonStr = match.groupValues[1]
                        val fixedJson = jsonStr.replace("'", "\"")
                        val jsonArray = JsonParser.parseString(fixedJson).asJsonArray
                        val list = mutableListOf<Exam>()
                        jsonArray.forEach { elem ->
                            val obj = elem.asJsonObject
                            val courseObj = obj.getAsJsonObject("course")
                            val examTypeObj = obj.getAsJsonObject("examType")
                            val courseName = courseObj?.get("nameZh")?.asString ?: ""
                            val examTypeName = examTypeObj?.get("nameZh")?.asString ?: ""
                            val courseDisplay = if (examTypeName.isNotEmpty()) "$courseName($examTypeName)" else courseName
                            val time = obj.get("examTime")?.asString ?: ""
                            val seatVal = obj.get("seatNo")
                            val seatNum = when {
                                seatVal == null || seatVal.isJsonNull -> ""
                                seatVal.isJsonPrimitive && seatVal.asJsonPrimitive.isNumber -> seatVal.asNumber.toString()
                                else -> seatVal.asString
                            }
                            val campus = obj.getAsJsonObject("requiredCampus")?.get("nameZh")?.asString ?: ""
                            val room = obj.get("room")?.asString ?: ""
                            val location = if (campus.isNotEmpty() && room.isNotEmpty()) "$campus-$room" else campus + room
                            val finished = obj.get("finished")?.asBoolean ?: false
                            val exam = Exam().apply {
                                setCourse(courseDisplay)
                                setTime(time)
                                setSeatNum(seatNum)
                                setLocation(location)
                                setFinished(finished)
                            }
                            list.add(exam)
                        }
                        AHUResponse<List<Exam>>().apply {
                            code = 0
                            data = list
                            msg = ""
                        }
                    }
                }
            }
        } catch (e: Exception) {
            AHUResponse<List<Exam>>().apply {
                code = -1
                msg = "解析失败: ${e.message}"
                data = emptyList()
            }
        }
    }

    /**
     * Parse exam info from the new server-rendered HTML table format.
     * The page renders exams as <tr> elements with seat data in a JS variable studentExamList.
     */
    private fun parseExamTableHtml(html: String): List<Exam> {
        // 1. Parse studentExamList for seat number mapping (exam id -> seat number)
        val seatMap = mutableMapOf<String, String>()
        val seatListRegex = Regex("(?s)var\\s+studentExamList\\s*=\\s*(\\[.+?\\]);")
        seatListRegex.find(html)?.let { match ->
            val jsonStr = match.groupValues[1].replace("'", "\"")
            try {
                val arr = JsonParser.parseString(jsonStr).asJsonArray
                arr.forEach {
                    val obj = it.asJsonObject
                    val id = obj.get("id")?.asString ?: obj.get("id")?.asLong?.toString() ?: ""
                    val seat = obj.get("seatNo")?.asString ?: obj.get("seatNo")?.asLong?.toString() ?: ""
                    if (id.isNotEmpty()) seatMap[id] = seat
                }
            } catch (_: Exception) { }
        }

        // 2. Parse HTML table rows
        val doc = Jsoup.parse(html)
        val rows = doc.select("tr[data-finished]")
        if (rows.isEmpty()) return emptyList()

        return rows.map { row ->
            val finished = row.attr("data-finished") == "true"

            // Time from <div class="time ...">
            val time = row.select("div.time").first()?.text()?.trim() ?: ""

            // Course name from bold <span>
            val course = row.select("span[style*=font-weight]").firstOrNull { el ->
                el.attr("style").contains("bold")
            }?.text()?.trim() ?: ""

            // Exam type from <span class="tag-span typeX">
            val examType = row.select("span.tag-span").first()?.text()?.trim() ?: ""

            // Seat exam ID from <span id="seat-NNN">
            val seatId = row.select("span[id^=seat-]").first()?.id()?.removePrefix("seat-") ?: ""
            val seatNum = seatMap[seatId] ?: ""

            // Location: campus, building, room from spans in first <td>
            val firstTd = row.select("td").first()
            val locationSpans = firstTd?.select("span")?.filter {
                !it.id().startsWith("seat-") && it.text().trim().isNotEmpty()
            } ?: emptyList()
            val location = locationSpans.joinToString("-") { it.text().trim() }

            val courseDisplay = if (examType.isNotEmpty()) "$course($examType)" else course

            Exam().apply {
                setCourse(courseDisplay)
                setTime(time)
                setSeatNum(seatNum)
                setLocation(location)
                setFinished(finished)
            }
        }
    }

    override suspend fun getBathroomTelInfo(
        bathroom: String,
        tel: String
    ): AHUResponse<BathroomTelInfo> {

        val response = AHUResponse<BathroomTelInfo>()

        var feeitemid: String? = null

        when (bathroom) {
            "竹园/龙河" -> {
                feeitemid = "409"
            }

            "桔园/蕙园" -> {
                feeitemid = "430"
            }

            else -> {
                response.code = -1
                response.msg = "目前没有这个浴室啊"
                response.data = null
                return response
            }
        }


        val formBody = FormBody.Builder()
            .add("feeitemid", feeitemid)
            .add("type", "IEC")
            .add("level", "1")
            .add("telPhone", tel)
            .build()


        val res = YcardApi.API.getFeeItemThirdData(formBody)

        if (res.isSuccessful) {
            val responseBody = res.body()
            val responseJson = responseBody?.string()

            val bathroomInfo = Gson().fromJson(responseJson, BathroomTelInfo::class.java)

            bathroomInfo?.let {
                response.code = 0
                response.data = it
                response.msg = "success"
                return response
            }
            response.code = -1
            response.msg = "数据返回错误"

        } else {
            response.code = -1
            response.msg = "请求接口失败"
        }

        return response
    }

    override suspend fun getCardInfo(): AHUResponse<CardInfo> {

        val response = AHUResponse<CardInfo>()

        response.data = YcardApi.API.loadCardRecharge()
        response.code = 0

        return response
    }

    override suspend fun getOrderThirdData(request: RequestBody): AHUResponse<Response<ResponseBody>> {
        val response = AHUResponse<Response<ResponseBody>>()
        response.data = YcardApi.API.getOrderThirdData(request.toFormBody())
        response.code = 0;
        return response
    }

    override suspend fun pay(request: RequestBody): AHUResponse<Response<ResponseBody>> {
        val response = AHUResponse<Response<ResponseBody>>()
        response.data = YcardApi.API.pay(request.toFormBody())
        response.code = 0;
        return response
    }

    override suspend fun getSchoolCalendar(): AHUResponse<Response<ResponseBody>> {
        val response = AHUResponse<Response<ResponseBody>>()
        response.data = AhuTong.API.downloadFile("xiaoli.jpg");
        response.code = 0;
        return response
    }


    suspend fun getStudentId(): String {
        val lastURL = JwxtApi.API.getGrade().raw().request.url.toString()
        val data = lastURL.split("/")
        return data.last()
    }
}
