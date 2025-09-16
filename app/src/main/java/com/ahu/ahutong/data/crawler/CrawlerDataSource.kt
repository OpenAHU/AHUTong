package com.ahu.ahutong.data.crawler

import android.util.Log
import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.base.BaseDataSource
import com.ahu.ahutong.data.crawler.api.adwmh.AdwmhApi
import com.ahu.ahutong.data.crawler.api.jwxt.JwxtApi
import com.ahu.ahutong.data.crawler.api.ycard.YcardApi
import com.ahu.ahutong.data.crawler.model.jwxt.CurrentSemester
import com.ahu.ahutong.data.crawler.model.ycard.CardInfo
import com.ahu.ahutong.data.crawler.model.ycard.Request
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.BathRoom
import com.ahu.ahutong.data.model.BathroomTelInfo
import com.ahu.ahutong.data.model.Card
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.Grade
import com.google.gson.Gson
import okhttp3.FormBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Response
import kotlin.math.log
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
        val basicInfo = JwxtApi.API.fetchCourseTableBasicInfo()
        val doc = Jsoup.parse(basicInfo.body()!!.string())

        val element = doc.select("script")
            .map { it.data() }
            .firstOrNull { it.contains("var semesters = JSON.parse") && it.contains("var currentSemester") }


        if (element == null) {

        }
        val semestersPattern: Regex? =
            Regex(
                "var\\s+semesters\\s*=\\s*JSON\\.parse\\(\\s*'(.*?)'\\s*\\);",
                RegexOption.DOT_MATCHES_ALL
            )
        val currentSemesterPattern: Regex? =
            Regex("var\\s+currentSemester\\s*=\\s*(\\{.*?\\});")

        if (semestersPattern == null) {
            return AHUResponse<List<Course>>()
        }

        if (currentSemesterPattern == null) {
            return AHUResponse<List<Course>>()
        }

        val currentSemester = currentSemesterPattern!!.find(element.toString())




        val gson = Gson()

        val currentSemesterJson = gson.fromJson(
            currentSemester!!.groups[1]!!.value.replace("\\\"", "\""),
            CurrentSemester::class.java
        )

        val courseTable = JwxtApi.API.getCourse(currentSemesterJson.id, currentSemesterJson.id)

        AHUCache.saveSchoolTerm(currentSemesterJson.name)

        val courseList = ArrayList<Course>()

        courseTable.studentTableVms[0].activities.forEach {

            val sortedWeekIndexes = it.weekIndexes.sorted()

            val course = Course()
            course.name = it.courseName
            course.setStartWeek(sortedWeekIndexes.get(0).toString())
            course.setLength((it.endUnit - it.startUnit + 1).toString())
            course.setWeekday(it.weekday.toString())
            course.setEndWeek(sortedWeekIndexes.get(sortedWeekIndexes.size - 1).toString())
            course.setStartTime(it.startUnit.toString())
            course.location = it.room ?: "未知"
            course.teacher = it.teacherNames.toString()
            course.weekIndexes = sortedWeekIndexes
            course.courseId = it.lessonId.toString()

            Log.e(TAG, "getSchedule: $course")
            courseList.add(course)
        }

        val response = AHUResponse<List<Course>>()
        response.data = courseList
        response.code = 0
        response.msg = ""

        return response


    }

    override suspend fun getGrade(): AHUResponse<Grade> {
        var id = AHUCache.getJwxtStudentId()
        id = id ?: getStudentId().also { AHUCache.setJwxtStudentId(it) }

        val data = JwxtApi.API.getGrade(id)
        val map = hashMapOf<String, Grade.TermGradeListBean>()

        data.semesterId2studentGrades.values.forEach { gradeList ->
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

                termGradeList.termGradePointAverage = if (termGradeList.termTotalCredit.toDouble() > 0) {
                    "%.2f".format(totalGradePointWeighted / termGradeList.termTotalCredit.toDouble())
                } else {
                    "0.0"
                }

                map[it] = termGradeList
            }

        }

        val response = AHUResponse<Grade>()
        val termGradeList =  map.values.toList()
        val grade = Grade()

        grade.totalCredit = termGradeList.sumOf {
            it.termTotalCredit?.toDoubleOrNull() ?: 0.0
        }.toString()

        grade.totalGradePoint =termGradeList.sumOf {
            val avg = it.termGradePointAverage?.toDoubleOrNull() ?: 0.0
            val credit = it.termTotalCredit?.toDoubleOrNull() ?: 0.0
            avg * credit
        }.toString()

        val weightedGradePointSum = termGradeList.sumOf {
            val avg = it.termGradePointAverage?.toDoubleOrNull() ?: 0.0
            val credit = it.termTotalCredit?.toDoubleOrNull() ?: 0.0
            avg * credit
        }


        grade.totalGradePointAverage =if (grade.totalCredit.toDouble() > 0) {
            "%.2f".format(weightedGradePointSum / grade.totalCredit.toDouble())
        } else {
            "0.0"
        }

        grade.termGradeList = termGradeList


        response.data = grade
        response.code = 0


        return response
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

    override suspend fun getBathroomTelInfo(
        bathroom: String,
        tel: String
    ): AHUResponse<BathroomTelInfo> {

        val response = AHUResponse<BathroomTelInfo>()

        var feeitemid: String? = null
        var type: String? = "IEC"
        var level: String? = "1"
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

    override suspend fun getOrderThirdData(request : Request): AHUResponse<Response<ResponseBody>> {
        val response = AHUResponse<Response<ResponseBody>>()
        response.data = YcardApi.API.getOrderThirdData(request.toFormBody())
        response.code = 0;
        return response
    }

    override suspend fun pay(request: Request): AHUResponse<Response<ResponseBody>> {
        val response = AHUResponse<Response<ResponseBody>>()
        response.data = YcardApi.API.pay(request.toFormBody())
        response.code = 0;
        return response
    }


    suspend fun getStudentId(): String {
        val lastURL = JwxtApi.API.getGrade().raw().request.url.toString()
        val data = lastURL.split("/")
        return data.last()
    }
}