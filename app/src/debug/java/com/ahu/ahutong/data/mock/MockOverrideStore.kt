package com.ahu.ahutong.data.mock

import android.content.Context
import com.ahu.ahutong.AHUApplication

data class MockEditableEndpointOption(
    val key: String,
    val title: String,
    val subtitle: String
)

enum class MockEditableEndpoint(
    val key: String,
    val title: String,
    val subtitle: String
) {
    CurrentSchedule(
        key = "current_schedule",
        title = "当前学期课表",
        subtitle = "getSchedule() / getSchedule(schoolYear, schoolTerm)"
    ),
    NextSchedule(
        key = "next_schedule",
        title = "下学期课表",
        subtitle = "getNextSchedule()"
    ),
    Grade(
        key = "grade",
        title = "成绩数据",
        subtitle = "getGrade()"
    ),
    GpaRank(
        key = "gpa_rank",
        title = "绩点排名",
        subtitle = "getGpaRankFromHtml()"
    ),
    Exams(
        key = "exams",
        title = "考试安排",
        subtitle = "getExamInfo(studentID, studentName)"
    ),
    CardMoney(
        key = "card_money",
        title = "一卡通余额",
        subtitle = "getCardMoney()"
    ),
    CardInfo(
        key = "card_info",
        title = "一卡通充值信息",
        subtitle = "getCardInfo()"
    ),
    Bathrooms(
        key = "bathrooms",
        title = "浴室开放状态",
        subtitle = "getBathRooms()"
    ),
    BathroomAccounts(
        key = "bathroom_accounts",
        title = "浴室账户",
        subtitle = "getBathroomTelInfo(bathroom, tel)，Map 的 key 为浴室名称"
    ),
    LostFoundCampuses(
        key = "lost_found_campuses",
        title = "失物招领校区",
        subtitle = "getAllCampus()"
    ),
    LostFoundTypes(
        key = "lost_found_types",
        title = "失物招领类型",
        subtitle = "getAllLostFoundType()"
    ),
    LostFoundItems(
        key = "lost_found_items",
        title = "失物招领列表",
        subtitle = "getLostFoundList(pageNo, pageSize, state)，分页由 MockDataSource 计算"
    ),
    ClassroomBuildings(
        key = "classroom_buildings",
        title = "空教室教学楼",
        subtitle = "Map<Int, List<GetBuildingsResponseItem>>，key 为校区 id"
    ),
    ClassroomRooms(
        key = "classroom_rooms",
        title = "空教室结果",
        subtitle = "Map<Int, List<FreeRoom>>，key 为校区 id"
    ),
    SchoolCalendar(
        key = "school_calendar",
        title = "校历图片 Base64",
        subtitle = "getSchoolCalendar()，填写 JPEG Base64"
    ),
    Behavior(
        key = "behavior",
        title = "接口行为",
        subtitle = "MockBehavior，可切换成功、空数据、接口错误、支付错误等"
    );

    fun option(): MockEditableEndpointOption =
        MockEditableEndpointOption(
            key = key,
            title = title,
            subtitle = subtitle
        )

    companion object {
        fun fromKey(key: String): MockEditableEndpoint =
            entries.firstOrNull { it.key == key } ?: CurrentSchedule
    }
}

object MockOverrideStore {
    private const val PREFS_NAME = "ahutong_mock_overrides"

    fun get(endpoint: MockEditableEndpoint): String? =
        prefs().getString(endpoint.key, null)

    fun set(endpoint: MockEditableEndpoint, value: String) {
        prefs()
            .edit()
            .putString(endpoint.key, value)
            .apply()
    }

    fun clear(endpoint: MockEditableEndpoint) {
        prefs()
            .edit()
            .remove(endpoint.key)
            .apply()
    }

    fun clearAll() {
        prefs()
            .edit()
            .clear()
            .apply()
    }

    fun overriddenKeys(): Set<String> =
        prefs().all.keys

    private fun prefs() =
        AHUApplication.getApp().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
