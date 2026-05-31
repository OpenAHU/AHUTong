package com.ahu.ahutong.data.mock

import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.base.BaseDataSource
import com.ahu.ahutong.data.crawler.model.adwnh.AllCampus
import com.ahu.ahutong.data.crawler.model.adwnh.AllLostFoundType
import com.ahu.ahutong.data.crawler.model.adwnh.LostFoundPublishRequest
import com.ahu.ahutong.data.crawler.model.adwnh.LostFoundResponse
import com.ahu.ahutong.data.crawler.model.jwxt.FreeRoom
import com.ahu.ahutong.data.crawler.model.jwxt.GetBuildingsResponseItem
import com.ahu.ahutong.data.crawler.model.ycard.CardInfo
import com.ahu.ahutong.data.crawler.model.ycard.RequestBody
import com.ahu.ahutong.data.model.BathRoom
import com.ahu.ahutong.data.model.BathroomTelInfo
import com.ahu.ahutong.data.model.Card
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.Exam
import com.ahu.ahutong.data.model.GpaRankInfo
import com.ahu.ahutong.data.model.Grade
import okhttp3.ResponseBody
import retrofit2.Response

class MockDataSource : BaseDataSource {
    override suspend fun getSchedule(
        schoolYear: String,
        schoolTerm: String
    ): AHUResponse<List<Course>> = unavailable()

    override suspend fun getSchedule(): AHUResponse<List<Course>> = unavailable()

    override suspend fun getNextSchedule(): AHUResponse<List<Course>> = unavailable()

    override suspend fun getGrade(): AHUResponse<Grade> = unavailable()

    override suspend fun getGpaRankFromHtml(): AHUResponse<GpaRankInfo> = unavailable()

    override suspend fun getAllCampus(): AHUResponse<AllCampus> = unavailable()

    override suspend fun getAllLostFoundType(): AHUResponse<AllLostFoundType> = unavailable()

    override suspend fun getLostFoundList(
        pageNo: Int,
        pageSize: Int,
        state: Int
    ): AHUResponse<LostFoundResponse> = unavailable()

    override suspend fun publishLostFound(
        request: LostFoundPublishRequest
    ): AHUResponse<Any> = unavailable()

    override suspend fun deleteLostFound(id: String): AHUResponse<Any> = unavailable()

    override suspend fun getCardMoney(): AHUResponse<Card> = unavailable()

    override suspend fun getBathRooms(): AHUResponse<List<BathRoom>> = unavailable()

    override suspend fun getExamInfo(
        studentID: String,
        studentName: String
    ): AHUResponse<List<Exam>> = unavailable()

    override suspend fun getBathroomTelInfo(
        bathroom: String,
        tel: String
    ): AHUResponse<BathroomTelInfo> = unavailable()

    override suspend fun getCardInfo(): AHUResponse<CardInfo> = unavailable()

    override suspend fun getOrderThirdData(
        request: RequestBody
    ): AHUResponse<Response<ResponseBody>> = unavailable()

    override suspend fun pay(
        request: RequestBody
    ): AHUResponse<Response<ResponseBody>> = unavailable()

    override suspend fun getSchoolCalendar(): AHUResponse<Response<ResponseBody>> = unavailable()

    private fun <T> unavailable(): AHUResponse<T> =
        AHUResponse<T>().apply {
            code = -1
            msg = "Mock data source is debug-only."
            data = null
        }
}

object MockCampusData {
    fun buildings(campusId: Int): List<GetBuildingsResponseItem> = emptyList()

    fun freeRooms(
        campusId: Int,
        buildingIds: List<Int>
    ): List<FreeRoom> = emptyList()
}
