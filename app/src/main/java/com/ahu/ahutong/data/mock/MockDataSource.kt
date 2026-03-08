package com.ahu.ahutong.data.mock

import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.base.BaseDataSource
import com.ahu.ahutong.data.crawler.model.ycard.CardInfo
import com.ahu.ahutong.data.crawler.model.ycard.RequestBody
import com.ahu.ahutong.data.model.BathRoom
import com.ahu.ahutong.data.model.BathroomTelInfo
import com.ahu.ahutong.data.model.Card
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.Exam
import com.ahu.ahutong.data.model.Grade
import okhttp3.ResponseBody
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MockDataSource : BaseDataSource {
    override suspend fun getSchedule(schoolYear: String, schoolTerm: String): AHUResponse<List<Course>> {
        return AHUResponse<List<Course>>().apply { code = 0; data = emptyList() }
    }

    override suspend fun getSchedule(): AHUResponse<List<Course>> {
        return AHUResponse<List<Course>>().apply { code = 0; data = emptyList() }
    }

    override suspend fun getGrade(): AHUResponse<Grade> {
        return AHUResponse<Grade>().apply { code = 0; data = Grade() }
    }

    override suspend fun getCardMoney(): AHUResponse<Card> {
        return AHUResponse<Card>().apply { code = 0; data = Card() }
    }

    override suspend fun getBathRooms(): AHUResponse<List<BathRoom>> {
        return AHUResponse<List<BathRoom>>().apply { code = 0; data = emptyList() }
    }

    override suspend fun getExamInfo(studentID: String, studentName: String): AHUResponse<List<Exam>> {
        val today = LocalDate.now()
        val fmtDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val fmtTime = DateTimeFormatter.ofPattern("HH:mm")
        fun timeStr(date: LocalDate, start: LocalTime, end: LocalTime): String {
            return "${date.format(fmtDate)} ${start.format(fmtTime)}~${end.format(fmtTime)}"
        }
        val list = mutableListOf<Exam>()
        run {
            val e = Exam()
            e.course = "高等数学（进行中示例）"
            e.location = "教学楼A-101"
            e.time = timeStr(today, LocalTime.now().minusMinutes(30), LocalTime.now().plusMinutes(90))
            e.seatNum = "12"
            e.finished = false
            list.add(e)
        }
        run {
            val e = Exam()
            e.course = "线性代数（未开始-早）"
            e.location = "教学楼B-202"
            e.time = timeStr(today.plusDays(1), LocalTime.of(9, 0), LocalTime.of(11, 0))
            e.seatNum = "A23"
            e.finished = false
            list.add(e)
        }
        run {
            val e = Exam()
            e.course = "计算机基础（未开始-晚）"
            e.location = "机房C-301"
            e.time = timeStr(today.plusDays(2), LocalTime.of(14, 0), LocalTime.of(16, 0))
            e.seatNum = "45"
            e.finished = false
            list.add(e)
        }
        run {
            val e = Exam()
            e.course = "大学英语（已结束）"
            e.location = "综合楼D-402"
            e.time = timeStr(today.minusDays(1), LocalTime.of(8, 0), LocalTime.of(10, 0))
            e.seatNum = "09"
            e.finished = true
            list.add(e)
        }
        run {
            val e = Exam()
            e.course = "物理（时间格式错误示例）"
            e.location = "理科楼E-503"
            e.time = "${today.format(fmtDate)} 09:00-11:00"
            e.seatNum = "Z7"
            e.finished = false
            list.add(e)
        }
        return AHUResponse<List<Exam>>().apply { code = 0; data = list }
    }

    override suspend fun getBathroomTelInfo(bathroom: String, tel: String): AHUResponse<BathroomTelInfo> {
        return AHUResponse<BathroomTelInfo>().apply { code = -1; data = null }
    }

    override suspend fun getCardInfo(): AHUResponse<CardInfo> {
        return AHUResponse<CardInfo>().apply { code = -1; data = null }
    }

    override suspend fun getOrderThirdData(request: RequestBody): AHUResponse<Response<ResponseBody>> {
        return AHUResponse<Response<ResponseBody>>().apply { code = 0; data = null }
    }

    override suspend fun pay(request: RequestBody): AHUResponse<Response<ResponseBody>> {
        return AHUResponse<Response<ResponseBody>>().apply { code = 0; data = null }
    }

    override suspend fun getSchoolCalendar(): AHUResponse<Response<ResponseBody>> {
        TODO("Not yet implemented")
    }
}
