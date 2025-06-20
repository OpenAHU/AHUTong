package com.ahu.ahutong.data.crawler

import android.util.Log
import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.base.BaseDataSource
import com.ahu.ahutong.data.crawler.api.adwmh.AdwmhApi
import com.ahu.ahutong.data.crawler.api.jwxt.JwxtApi
import com.ahu.ahutong.data.crawler.model.jwxt.CurrentSemester
import com.ahu.ahutong.data.crawler.model.jwxt.Semesters
import com.ahu.ahutong.data.model.BathRoom
import com.ahu.ahutong.data.model.Card
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.Grade
import com.ahu.ahutong.ui.screen.main.schedule.CourseCard
import com.google.gson.Gson
import org.json.JSONArray
import org.jsoup.Jsoup
import java.util.regex.Pattern
import kotlin.text.Regex


class CrawlerDataSource : BaseDataSource {
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

        val semesters = semestersPattern!!.find(element.toString())
        val currentSemester = currentSemesterPattern!!.find(element.toString())


        val gson = Gson()

        val semestersJson = gson.fromJson(
            semesters!!.groups[1]!!.value.replace("\\\"", "\""),
            Semesters::class.java
        )

        val currentSemesterJson = gson.fromJson(
            currentSemester!!.groups[1]!!.value.replace("\\\"", "\""),
            CurrentSemester::class.java
        )

        val courseTable = JwxtApi.API.getCourse(currentSemesterJson.id,currentSemesterJson.id)

        val courseList = ArrayList<Course>()

        courseTable.studentTableVms[0].activities.forEach {

            val sortedWeekIndexes = it.weekIndexes.sorted()

            val course = Course()
            course.name = it.courseName
            course.setStartWeek(sortedWeekIndexes.get(0).toString())
            course.setLength((it.endUnit-it.startUnit+1).toString())
            course.setWeekday(it.weekday.toString())
            course.setEndWeek(sortedWeekIndexes.get(sortedWeekIndexes.size-1).toString())
            course.setStartTime(it.startUnit.toString())
            course.location = it.room
            course.teacher = it.teacherNames.toString()
            course.weekIndexes = sortedWeekIndexes
            course.courseId = it.lessonId.toString()

            courseList.add(course)
        }

        val response = AHUResponse<List<Course>>()
        response.data = courseList
        response.code = 0
        response.msg = ""

        return response


    }

    override suspend fun getGrade(): AHUResponse<Grade> {
        return AHUResponse<Grade>()
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
}