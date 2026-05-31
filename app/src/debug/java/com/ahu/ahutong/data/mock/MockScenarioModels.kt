package com.ahu.ahutong.data.mock

import com.ahu.ahutong.data.crawler.model.adwnh.AllCampus
import com.ahu.ahutong.data.crawler.model.adwnh.AllLostFoundType
import com.ahu.ahutong.data.crawler.model.adwnh.LostFoundItem
import com.ahu.ahutong.data.crawler.model.jwxt.FreeRoom
import com.ahu.ahutong.data.crawler.model.jwxt.GetBuildingsResponseItem
import com.ahu.ahutong.data.crawler.model.ycard.CardInfo
import com.ahu.ahutong.data.model.BathRoom
import com.ahu.ahutong.data.model.BathroomTelInfo
import com.ahu.ahutong.data.model.Card
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.Exam
import com.ahu.ahutong.data.model.GpaRankInfo
import com.ahu.ahutong.data.model.Grade

data class MockScenarioOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String
)

data class MockScenario(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String,
    val behavior: MockBehavior,
    val academic: MockAcademicProfile,
    val campus: MockCampusProfile,
    val payment: MockPaymentProfile,
    val discovery: MockDiscoveryProfile
) {
    fun option(): MockScenarioOption =
        MockScenarioOption(
            id = id,
            title = title,
            subtitle = subtitle,
            badge = badge
        )
}

data class MockBehavior(
    val networkMode: MockNetworkMode = MockNetworkMode.Success,
    val latencyMs: Long = 0L,
    val errorMessage: String = "Mock 场景模拟接口失败",
    val emptyDataMessage: String = "Mock 场景模拟暂无数据",
    val paymentMode: MockPaymentMode = MockPaymentMode.Success,
    val loginState: MockLoginState = MockLoginState.Valid
) {
    val shouldFail: Boolean
        get() = networkMode == MockNetworkMode.Error || networkMode == MockNetworkMode.Timeout

    val shouldReturnEmpty: Boolean
        get() = networkMode == MockNetworkMode.Empty
}

enum class MockNetworkMode {
    Success,
    Empty,
    Error,
    Timeout
}

enum class MockPaymentMode {
    Success,
    InsufficientBalance,
    PasswordError,
    GatewayError
}

enum class MockLoginState {
    Valid,
    Expired,
    Guest
}

data class MockAcademicProfile(
    val currentSchedule: List<Course>,
    val nextSchedule: List<Course>,
    val grade: Grade,
    val gpaRankInfo: GpaRankInfo,
    val exams: List<Exam>
)

data class MockCampusProfile(
    val campuses: AllCampus,
    val classroomBuildings: Map<Int, List<GetBuildingsResponseItem>>,
    val classroomRooms: Map<Int, List<FreeRoom>>,
    val bathrooms: List<BathRoom>,
    val bathroomAccounts: Map<String, BathroomTelInfo>,
    val calendarJpegBase64: String
)

data class MockPaymentProfile(
    val cardMoney: Card,
    val cardInfo: CardInfo,
    val cardOrderPrefix: String,
    val bathroomOrderPrefix: String,
    val successMessage: String,
    val failureMessage: String
)

data class MockDiscoveryProfile(
    val lostFoundCampuses: AllCampus,
    val lostFoundTypes: AllLostFoundType,
    val lostFoundItems: List<LostFoundItem>
)
