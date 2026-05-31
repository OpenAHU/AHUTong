package com.ahu.ahutong.data.mock

import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.base.BaseDataSource
import com.ahu.ahutong.data.crawler.model.adwnh.AllCampus
import com.ahu.ahutong.data.crawler.model.adwnh.AllLostFoundType
import com.ahu.ahutong.data.crawler.model.adwnh.LostFoundItem
import com.ahu.ahutong.data.crawler.model.adwnh.LostFoundPage
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import java.util.Base64
import kotlin.math.ceil

class MockDataSource : BaseDataSource {
    private val gson = Gson()

    override suspend fun getSchedule(
        schoolYear: String,
        schoolTerm: String
    ): AHUResponse<List<Course>> =
        scenarioResponse(
            endpoint = MockEditableEndpoint.CurrentSchedule,
            type = object : TypeToken<List<Course>>() {}.type
        ) { it.academic.currentSchedule }

    override suspend fun getSchedule(): AHUResponse<List<Course>> =
        scenarioResponse(
            endpoint = MockEditableEndpoint.CurrentSchedule,
            type = object : TypeToken<List<Course>>() {}.type
        ) { it.academic.currentSchedule }

    override suspend fun getNextSchedule(): AHUResponse<List<Course>> =
        scenarioResponse(
            endpoint = MockEditableEndpoint.NextSchedule,
            type = object : TypeToken<List<Course>>() {}.type
        ) { it.academic.nextSchedule }

    override suspend fun getGrade(): AHUResponse<Grade> =
        scenarioResponse(MockEditableEndpoint.Grade, Grade::class.java) { it.academic.grade }

    override suspend fun getCardMoney(): AHUResponse<Card> =
        scenarioResponse(MockEditableEndpoint.CardMoney, Card::class.java) { it.payment.cardMoney }

    override suspend fun getBathRooms(): AHUResponse<List<BathRoom>> =
        scenarioResponse(
            endpoint = MockEditableEndpoint.Bathrooms,
            type = object : TypeToken<List<BathRoom>>() {}.type
        ) { it.campus.bathrooms }

    override suspend fun getExamInfo(
        studentID: String,
        studentName: String
    ): AHUResponse<List<Exam>> =
        scenarioResponse(
            endpoint = MockEditableEndpoint.Exams,
            type = object : TypeToken<List<Exam>>() {}.type
        ) { it.academic.exams }

    override suspend fun getBathroomTelInfo(
        bathroom: String,
        tel: String
    ): AHUResponse<BathroomTelInfo> {
        val scenario = MockScenarioController.activeScenario()
        activeBehavior(scenario).failureOrNull<BathroomTelInfo>()?.let { return it }
        val accounts = overrideValue<Map<String, BathroomTelInfo>>(
            endpoint = MockEditableEndpoint.BathroomAccounts,
            type = object : TypeToken<Map<String, BathroomTelInfo>>() {}.type
        ) ?: scenario.campus.bathroomAccounts
        val account = accounts[bathroom]
            ?: return failure("Mock 暂不支持该浴室：$bathroom")
        val map = account.map ?: return success(account)
        val data = map.data ?: return success(account)
        return success(
            account.copy(
                map = map.copy(
                    showData = map.showData?.copy(phone = tel),
                    data = data.copy(
                        telPhone = tel,
                        identifier = "mock-$tel-${data.projectId}"
                    )
                )
            )
        )
    }

    override suspend fun getCardInfo(): AHUResponse<CardInfo> =
        scenarioResponse(MockEditableEndpoint.CardInfo, CardInfo::class.java) { it.payment.cardInfo }

    override suspend fun getOrderThirdData(
        request: RequestBody
    ): AHUResponse<Response<ResponseBody>> {
        val scenario = MockScenarioController.activeScenario()
        val behavior = activeBehavior(scenario)
        behavior.failureOrNull<Response<ResponseBody>>()?.let { return it }
        if (behavior.paymentMode == MockPaymentMode.GatewayError) {
            return success(
                mockResponse(
                    """{"code":500,"success":false,"data":"","msg":"${scenario.payment.failureMessage}"}""",
                    "MOCK-GATEWAY-ORDER"
                )
            )
        }
        val orderId = "${scenario.payment.cardOrderPrefix}-${System.currentTimeMillis()}"
        return success(
            mockResponse(
                """{"code":200,"success":true,"data":{"orderid":"$orderId"},"msg":"success"}""",
                orderId
            )
        )
    }

    override suspend fun pay(
        request: RequestBody
    ): AHUResponse<Response<ResponseBody>> {
        val scenario = MockScenarioController.activeScenario()
        val behavior = activeBehavior(scenario)
        behavior.failureOrNull<Response<ResponseBody>>()?.let { return it }
        val orderId = request.getParam("orderid")?.toString()
            ?: "${scenario.payment.bathroomOrderPrefix}-${System.currentTimeMillis()}"
        val isFinalPay = request.getParam("paystep")?.toString() == "2"
        val body = when {
            isFinalPay && behavior.paymentMode != MockPaymentMode.Success ->
                """{"code":500,"success":false,"data":"","msg":"${scenario.payment.failureMessage}"}"""

            isFinalPay ->
                """{"code":200,"success":true,"data":"$orderId","msg":"${scenario.payment.successMessage}"}"""

            else ->
                """{"code":200,"success":true,"data":{"orderid":"$orderId"},"msg":"success"}"""
        }
        return success(mockResponse(body, orderId))
    }

    override suspend fun getGpaRankFromHtml(): AHUResponse<GpaRankInfo> =
        scenarioResponse(MockEditableEndpoint.GpaRank, GpaRankInfo::class.java) { it.academic.gpaRankInfo }

    override suspend fun getAllCampus(): AHUResponse<AllCampus> =
        scenarioResponse(MockEditableEndpoint.LostFoundCampuses, AllCampus::class.java) {
            it.discovery.lostFoundCampuses
        }

    override suspend fun getAllLostFoundType(): AHUResponse<AllLostFoundType> =
        scenarioResponse(MockEditableEndpoint.LostFoundTypes, AllLostFoundType::class.java) {
            it.discovery.lostFoundTypes
        }

    override suspend fun getLostFoundList(
        pageNo: Int,
        pageSize: Int,
        state: Int
    ): AHUResponse<LostFoundResponse> {
        val scenario = MockScenarioController.activeScenario()
        activeBehavior(scenario).failureOrNull<LostFoundResponse>()?.let { return it }
        val items = overrideValue<List<LostFoundItem>>(
            endpoint = MockEditableEndpoint.LostFoundItems,
            type = object : TypeToken<List<LostFoundItem>>() {}.type
        ) ?: scenario.discovery.lostFoundItems
        val allItems = items.filter { it.state == state }
        val from = ((pageNo - 1) * pageSize).coerceAtLeast(0)
        val pageItems = allItems.drop(from).take(pageSize)
        val totalPages = ceil(allItems.size.toDouble() / pageSize.toDouble()).toInt().coerceAtLeast(1)
        return success(
            LostFoundResponse(
                code = 0,
                msg = "success",
                data = LostFoundPage(
                    pageNum = pageNo,
                    pageSize = pageSize,
                    size = pageItems.size,
                    startRow = if (pageItems.isEmpty()) 0 else from + 1,
                    endRow = from + pageItems.size,
                    total = allItems.size,
                    pages = totalPages,
                    list = pageItems
                )
            )
        )
    }

    override suspend fun getSchoolCalendar(): AHUResponse<Response<ResponseBody>> =
        scenarioResponse { scenario ->
            Response.success(
                Base64.getDecoder()
                    .decode(MockOverrideStore.get(MockEditableEndpoint.SchoolCalendar) ?: scenario.campus.calendarJpegBase64)
                    .toResponseBody("image/jpeg".toMediaType())
            )
        }

    override suspend fun publishLostFound(
        request: LostFoundPublishRequest
    ): AHUResponse<Any> =
        scenarioResponse { Any() }

    override suspend fun deleteLostFound(
        id: String
    ): AHUResponse<Any> =
        scenarioResponse { Any() }

    private fun mockResponse(json: String, orderId: String): Response<ResponseBody> {
        val raw = okhttp3.Response.Builder()
            .request(
                Request.Builder()
                    .url("https://mock.ahu.edu.cn/payment/result?orderid=$orderId")
                    .build()
            )
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .build()
        return Response.success(json.toResponseBody("application/json".toMediaType()), raw)
    }

    private fun <T> scenarioResponse(
        endpoint: MockEditableEndpoint,
        type: java.lang.reflect.Type,
        block: (MockScenario) -> T
    ): AHUResponse<T> {
        val scenario = MockScenarioController.activeScenario()
        activeBehavior(scenario).failureOrNull<T>()?.let { return it }
        return success(overrideValue(endpoint, type) ?: block(scenario))
    }

    private fun <T> scenarioResponse(block: (MockScenario) -> T): AHUResponse<T> {
        val scenario = MockScenarioController.activeScenario()
        activeBehavior(scenario).failureOrNull<T>()?.let { return it }
        return success(block(scenario))
    }

    private fun activeBehavior(scenario: MockScenario): MockBehavior =
        overrideValue(
            endpoint = MockEditableEndpoint.Behavior,
            type = MockBehavior::class.java
        ) ?: scenario.behavior

    private fun <T> overrideValue(
        endpoint: MockEditableEndpoint,
        type: java.lang.reflect.Type
    ): T? =
        MockOverrideStore.get(endpoint)?.let { raw ->
            runCatching { gson.fromJson<T>(raw, type) }.getOrNull()
        }

    private fun <T> overrideValue(
        endpoint: MockEditableEndpoint,
        type: Class<T>
    ): T? =
        MockOverrideStore.get(endpoint)?.let { raw ->
            runCatching { gson.fromJson(raw, type) }.getOrNull()
        }

    private fun <T> MockBehavior.failureOrNull(): AHUResponse<T>? =
        if (shouldFail) failure(errorMessage) else null

    private fun <T> success(data: T, msg: String = "success"): AHUResponse<T> =
        AHUResponse<T>().apply {
            code = 0
            this.msg = msg
            this.data = data
        }

    private fun <T> failure(msg: String): AHUResponse<T> =
        AHUResponse<T>().apply {
            code = -1
            this.msg = msg
            data = null
        }
}

object MockCampusData {
    private val gson = Gson()

    fun buildings(campusId: Int): List<GetBuildingsResponseItem> {
        val override = MockOverrideStore.get(MockEditableEndpoint.ClassroomBuildings)?.let { raw ->
            runCatching {
                gson.fromJson<Map<Int, List<GetBuildingsResponseItem>>>(
                    raw,
                    object : TypeToken<Map<Int, List<GetBuildingsResponseItem>>>() {}.type
                )
            }.getOrNull()
        }
        return (override ?: MockScenarioController.activeScenario().campus.classroomBuildings)[campusId].orEmpty()
    }

    fun freeRooms(
        campusId: Int,
        buildingIds: List<Int>
    ): List<FreeRoom> {
        val override = MockOverrideStore.get(MockEditableEndpoint.ClassroomRooms)?.let { raw ->
            runCatching {
                gson.fromJson<Map<Int, List<FreeRoom>>>(
                    raw,
                    object : TypeToken<Map<Int, List<FreeRoom>>>() {}.type
                )
            }.getOrNull()
        }
        return (override ?: MockScenarioController.activeScenario().campus.classroomRooms)[campusId]
            .orEmpty()
            .filter { buildingIds.isEmpty() || it.building.id in buildingIds }
    }
}
