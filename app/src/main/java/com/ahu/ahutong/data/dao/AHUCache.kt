package com.ahu.ahutong.data.dao

import com.ahu.ahutong.AHUApplication
import com.ahu.ahutong.data.crawler.model.adwnh.CampusItem
import com.ahu.ahutong.data.crawler.model.adwnh.LostFoundItem
import com.ahu.ahutong.data.crawler.model.adwnh.LostFoundTypeItem
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.ElectricityChargeInfo
import com.ahu.ahutong.data.model.ElectricityDepositHistoryItem
import com.ahu.ahutong.data.model.Exam
import com.ahu.ahutong.data.model.GpaRankInfo
import com.ahu.ahutong.data.model.Grade
import com.ahu.ahutong.data.model.RoomSelectionInfo
import com.ahu.ahutong.data.model.User
import com.ahu.ahutong.ext.fromJson
import com.ahu.ahutong.sdk.RustSDK
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV

/**
 * @Author SinkDev
 * @Date 2021/7/27-16:49
 * @Email 468766131@qq.com
 */
object AHUCache {

    init {
        MMKV.initialize(AHUApplication.getApp())
    }

    private val kv_init: MMKV = MMKV.mmkvWithID("ahu")
    private val kv: MMKV
        get() {
            val user = getCurrentUser()
            return  if (user != null && !user.xh.isNullOrEmpty()) {
                MMKV.mmkvWithID("ahu_${user.xh}")
            } else {
                MMKV.mmkvWithID("ahu_guest")
            }
        }

    private const val INIT_BOX = "init"

    private fun sanitizeBoxPart(value: String): String {
        return value.replace(Regex("[^A-Za-z0-9_.-]"), "_")
    }

    private fun userBoxName(): String {
        val userId = getCurrentUser()?.xh?.takeIf { it.isNotEmpty() } ?: "guest"
        return "user_${sanitizeBoxPart(userId)}"
    }

    private fun initPutString(key: String, value: String) {
        RustSDK.kvPutStringSafe(INIT_BOX, key, value)
    }

    private fun initGetString(key: String): String? {
        return RustSDK.kvGetStringSafe(INIT_BOX, key)
    }

    private fun initGetStringOrMigrate(key: String, fallback: () -> String?): String? {
        initGetString(key)?.let { return it }
        return fallback()?.also {
            if (it.isNotEmpty()) initPutString(key, it)
        }
    }

    private fun initRemove(key: String) {
        RustSDK.kvRemoveSafe(INIT_BOX, key)
    }

    private fun userPutString(key: String, value: String) {
        RustSDK.kvPutStringSafe(userBoxName(), key, value)
    }

    private fun userGetString(key: String): String? {
        return RustSDK.kvGetStringSafe(userBoxName(), key)
    }

    private fun userGetStringOrMigrate(key: String, fallback: () -> String?): String? {
        userGetString(key)?.let { return it }
        return fallback()?.also {
            if (it.isNotEmpty()) userPutString(key, it)
        }
    }

    private fun userRemove(key: String) {
        RustSDK.kvRemoveSafe(userBoxName(), key)
    }

    /**
     * 清除全部数据
     */
    fun clearAll() {
        val boxName = userBoxName()
        val currentKv = kv
        RustSDK.kvClearBoxSafe(INIT_BOX)
        RustSDK.kvClearBoxSafe(boxName)
        RustSDK.kvClearBoxSafe("user_guest")
        kv_init.clearAll()
        currentKv.clearAll()
        MMKV.mmkvWithID("ahu_guest").clearAll()
    }

    /**
     * 保存本地User对象
     * @param user User
     */
    fun saveCurrentUser(user: User) {
        val data = Gson().toJson(user)
        initPutString("current_user", data)
        kv_init.encode("current_user", data)
    }

    /**
     * 清除本地登陆状态
     */
    fun clearCurrentUser() {
        initPutString("current_user", "")
        kv_init.encode("current_user", "")
    }

    /**
     * 获取本地User对象
     * @return User?
     */
    fun getCurrentUser(): User? {
        val data = initGetStringOrMigrate("current_user") { kv_init.decodeString("current_user") } ?: ""
        return data.fromJson(User::class.java)
    }

    /**
     * 是否登录
     * @return Boolean
     */
    fun isLogin(): Boolean {
        return getCurrentUser() != null
    }

    /**
     * 保存智慧安大密码
     * @param password String
     */
    fun saveWisdomPassword(password: String) {
        initPutString("password_wisdom", password)
        kv_init.encode("password_wisdom", password)
    }

    /**
     * 获取智慧安大密码
     * @return String?
     */
    fun getWisdomPassword(): String? {
        return initGetStringOrMigrate("password_wisdom") { kv_init.decodeString("password_wisdom") }
    }

    /**
     * 保存课程表
     * @param schoolYear String
     * @param schoolTerm String
     * @param schdule List<Course>
     */
    fun saveSchedule(schoolYear: String, schoolTerm: String, schedule: List<Course>) {
        val data = Gson().toJson(schedule)
        userPutString("$schoolYear-$schoolTerm.schedule", data)
        kv.putString("$schoolYear-$schoolTerm.schedule", data)
    }

    fun saveSchedule(schoolTerm: String,schedule: List<Course>) {
        val data = Gson().toJson(schedule)
        userPutString("$schoolTerm.schedule", data)
        kv.putString("$schoolTerm.schedule", data) // 2025-2026-1
    }

    /**
     * 获取课程表
     * @param schoolYear String
     * @param schoolTerm String
     * @return List<Course>
     */
    fun getSchedule(schoolYear: String, schoolTerm: String): List<Course>? {
        val key = "$schoolYear-$schoolTerm.schedule"
        val data = userGetStringOrMigrate(key) { kv.getString(key, "") } ?: ""
        return data.fromJson(object : TypeToken<List<Course>>() {}.type)
    }

    fun getSchedule(schoolTerm: String): List<Course>? {
        val key = "$schoolTerm.schedule"
        val data = userGetStringOrMigrate(key) { kv.getString(key, "") } ?: ""
        return data.fromJson(object : TypeToken<List<Course>>() {}.type)
    }

    fun saveNextSchedule(schedule: List<Course>) {
        val data = Gson().toJson(schedule)
        kv.putString("next.schedule", data)
    }

    fun getNextSchedule(): List<Course>? {
        val data = kv.getString("next.schedule", "") ?: ""
        return data.fromJson(object : TypeToken<List<Course>>() {}.type)
    }

    /**
     * 保存成绩
     * @param grade Grade
     */
    fun saveGrade(grade: Grade) {
        val data = Gson().toJson(grade)
        userPutString("grade", data)
        kv.encode("grade", data)
    }

    /**
     * 获取成绩
     * @return Grade
     */
    fun getGrade(): Grade? {
        val data = userGetStringOrMigrate("grade") { kv.decodeString("grade") } ?: ""
        return data.fromJson(Grade::class.java)
    }

    /**
     * 保存考试信息
     * @param exams List<Exam>
     */
    fun saveExamInfo(exams: List<Exam>) {
        val data = Gson().toJson(exams)
        userPutString("exams", data)
        kv.encode("exams", data)
    }

    /**
     * 获取考试信息
     * @return List<Exam>?
     */
    fun getExamInfo(): List<Exam>? {
        val data = userGetStringOrMigrate("exams") { kv.decodeString("exams") } ?: ""
        return data.fromJson(object : TypeToken<List<Exam>>() {}.type)
    }

    /**
     * 获取开学时间
     * @param schoolYear String yyyy-yyyy
     * @param schoolTerm String 1 or 2
     * @return String? yyyy-MM-dd
     */
    fun getSchoolTermStartTime(schoolYear: String, schoolTerm: String): String? {
        val key = "startTime-$schoolYear-$schoolTerm"
        return userGetStringOrMigrate(key) { kv.decodeString(key) }
    }

    /**
     * 保存开学时间
     * @param schoolYear String yyyy-yyyy
     * @param schoolTerm String 1 or 2
     * @param startTime String yyyy-MM-dd
     */
    fun saveSchoolTermStartTime(schoolYear: String, schoolTerm: String, startTime: String) {
        userPutString("startTime-$schoolYear-$schoolTerm", startTime)
        kv.encode("startTime-$schoolYear-$schoolTerm", startTime)
    }

    /**
     * 获取默认的学年
     * @return String?
     */
    fun getSchoolYear(): String? {
        return userGetStringOrMigrate("defaultSchoolYear") {
            kv.decodeString("defaultSchoolYear")
                ?: initGetStringOrMigrate("defaultSchoolYear") { kv_init.decodeString("defaultSchoolYear") }
        }
    }

    /**
     * 保存默认的学年
     * @param schoolYear String
     */
    fun saveSchoolYear(schoolYear: String) {
        userPutString("defaultSchoolYear", schoolYear)
        kv.encode("defaultSchoolYear", schoolYear)
    }

    /**
     * 获取默认学期
     * @return String?
     */
    fun getSchoolTerm(): String? {
        return userGetStringOrMigrate("defaultSchoolTerm") {
            kv.getString(
                "defaultSchoolTerm",
                initGetStringOrMigrate("defaultSchoolTerm") { kv_init.getString("defaultSchoolTerm", null) }
            )
        }
    }

    /**
     * 保存默认的学期
     * @param schoolTerm String
     */
    fun saveSchoolTerm(schoolTerm: String) {
        userPutString("defaultSchoolTerm", schoolTerm)
        kv.putString("defaultSchoolTerm", schoolTerm)
    }

    /**
     * 是否显示非本周课程
     * @return Boolean
     */
    fun isShowAllCourse(): Boolean {
        userGetString("isShowAllCourse")?.toBooleanStrictOrNull()?.let { return it }
        val value = kv.getBoolean("isShowAllCourse", false)
        if (kv.containsKey("isShowAllCourse")) userPutString("isShowAllCourse", value.toString())
        return value
    }

    /**
     * 保存是否显示非本周课程
     * @param isCourse Boolean
     */
    fun saveIsShowAllCourse(isCourse: Boolean) {
        userPutString("isShowAllCourse", isCourse.toString())
        kv.putBoolean("isShowAllCourse", isCourse)
    }

    fun isShowWidgetTip(): Boolean {
        userGetString("is_show_widget_dialog")?.toBooleanStrictOrNull()?.let { return it }
        val value = kv.getBoolean("is_show_widget_dialog", true)
        if (kv.containsKey("is_show_widget_dialog")) userPutString("is_show_widget_dialog", value.toString())
        return value
    }

    fun ignoreWidgetTip() {
        userPutString("is_show_widget_dialog", false.toString())
        kv.putBoolean("is_show_widget_dialog", false)
    }

    private const val HOME_WIDGET_SLOTS_KEY = "home_widget_slots"
    private const val HOME_WIDGET_SLOT_COUNT = 8

    private fun defaultHomeWidgetSlots(): List<String?> {
        return listOf("bathroom", "electricity") + List(HOME_WIDGET_SLOT_COUNT - 2) { null }
    }

    private fun normalizeHomeWidgetSlots(slots: List<String?>): List<String?> {
        val seen = mutableSetOf<String>()
        return List(HOME_WIDGET_SLOT_COUNT) { index ->
            val id = slots.getOrNull(index)?.takeIf { it.isNotBlank() }
            if (id != null && seen.add(id)) id else null
        }
    }

    fun getHomeWidgetSlots(): List<String?> {
        val data = userGetStringOrMigrate(HOME_WIDGET_SLOTS_KEY) {
            kv.decodeString(HOME_WIDGET_SLOTS_KEY)
        } ?: ""
        if (data.isBlank()) return defaultHomeWidgetSlots()

        return runCatching {
            Gson().fromJson<List<String?>>(
                data,
                object : TypeToken<List<String?>>() {}.type
            )
        }.getOrNull()
            ?.let(::normalizeHomeWidgetSlots)
            ?: defaultHomeWidgetSlots()
    }

    fun saveHomeWidgetSlots(slots: List<String?>) {
        val normalizedSlots = normalizeHomeWidgetSlots(slots)
        val data = Gson().toJson(normalizedSlots)
        userPutString(HOME_WIDGET_SLOTS_KEY, data)
        kv.encode(HOME_WIDGET_SLOTS_KEY, data)
    }

    fun logout() {
        clearCurrentUser()
        saveWisdomPassword("")
        saveRustCookies("")
    }


    fun savePhone(phone:String){
        userPutString("phone", phone)
        kv.putString("phone",phone)
    }

    fun getPhone() : String?{
        return userGetStringOrMigrate("phone") { kv.getString("phone",null) }
    }


    fun setJwxtStudentId(id: String){
        userPutString("jwxt_stu_id", id)
        kv.putString("jwxt_stu_id",id)
    }

    fun getJwxtStudentId() : String?{
        return userGetStringOrMigrate("jwxt_stu_id") {
            kv.getString(
                "jwxt_stu_id",
                initGetStringOrMigrate("jwxt_stu_id") { kv_init.getString("jwxt_stu_id", null) }
            )
        }
    }


    fun saveString(key: String ,value : String){
        userPutString(key, value)
        kv.putString(key,value)
    }

    fun saveRustCookies(cookiesJson: String) {
        if (cookiesJson.isEmpty()) {
            initRemove("rust_cookies_json")
        } else {
            initPutString("rust_cookies_json", cookiesJson)
        }
        kv_init.putString("rust_cookies_json", cookiesJson)
    }

    fun getRustCookies(): String {
        return initGetStringOrMigrate("rust_cookies_json") {
            kv_init.getString("rust_cookies_json", "")
        } ?: ""
    }


    fun isAgreementAccepted(): Boolean{
        userGetString("agreementAccepted")?.toBooleanStrictOrNull()?.let { return it }
        val value = kv.getBoolean("agreementAccepted",false)
        if (kv.containsKey("agreementAccepted")) userPutString("agreementAccepted", value.toString())
        return value
    }

    fun setAgreementAccepted(){
        userPutString("agreementAccepted", true.toString())
        kv.putBoolean("agreementAccepted",true)
    }

    fun isPrivacyAccepted(): Boolean{
        userGetString("privacyAccepted")?.toBooleanStrictOrNull()?.let { return it }
        val value = kv.getBoolean("privacyAccepted",false)
        if (kv.containsKey("privacyAccepted")) userPutString("privacyAccepted", value.toString())
        return value
    }

    fun setPrivacyAccepted(){
        userPutString("privacyAccepted", true.toString())
        kv.putBoolean("privacyAccepted",true)
    }

    fun isBusinessAccepted(): Boolean{
        userGetString("businessAccepted")?.toBooleanStrictOrNull()?.let { return it }
        val value = kv.getBoolean("businessAccepted",false)
        if (kv.containsKey("businessAccepted")) userPutString("businessAccepted", value.toString())
        return value
    }

    fun setBusinessAccepted(){
        userPutString("businessAccepted", true.toString())
        kv.putBoolean("businessAccepted",true)
    }

    /**
     * 获取房间选择信息
     * @return RoomSelectionInfo?
     */
    fun getRoomSelection(): RoomSelectionInfo? {
        val data = userGetStringOrMigrate("room_selection_info") {
            kv.decodeString("room_selection_info")
        } ?: ""
        return data.fromJson(RoomSelectionInfo::class.java)
    }

    fun saveElectricityDepositHistory(history: List<ElectricityDepositHistoryItem>) {
        val data = Gson().toJson(history)
        userPutString("electricity_room_history", data)
        kv.encode("electricity_room_history", data)
    }

    fun getElectricityDepositHistory(): List<ElectricityDepositHistoryItem> {
        val data = userGetStringOrMigrate("electricity_room_history") {
            kv.decodeString("electricity_room_history")
                ?: initGetStringOrMigrate("electricity_room_history") {
                    kv_init.decodeString("electricity_room_history")
                }
        } ?: ""
        if (data.isEmpty()) return emptyList()
        return data.fromJson(object : TypeToken<List<ElectricityDepositHistoryItem>>() {}.type) ?: emptyList()
    }

    /**
     * 保存电费累计充值信息
     * @param info ElectricityChargeInfo
     */
    fun saveElectricityChargeInfo(info: ElectricityChargeInfo) {
        val data = Gson().toJson(info)
        userPutString("electricity_charge_acl", data)
        kv.encode("electricity_charge_acl", data)
    }

    /**
     * 获取电费累计充值信息
     * @return ElectricityChargeInfo?
     */
    fun getElectricityChargeInfo(): ElectricityChargeInfo? {
        val data = userGetStringOrMigrate("electricity_charge_acl") {
            kv.decodeString("electricity_charge_acl")
                ?: initGetStringOrMigrate("electricity_charge_acl") {
                    kv_init.decodeString("electricity_charge_acl")
                }
        } ?: ""
        if (data.isEmpty()) {
            return null
        }
        return data.fromJson(ElectricityChargeInfo::class.java)
    }

    /**
     * 清除电费累计充值信息
     */
    fun clearElectricityChargeInfo() {
        userRemove("electricity_charge_acl")
        kv.removeValueForKey("electricity_charge_acl")
    }

    /**
     * 保存房间选择信息
     * @param info RoomSelectionInfo
     */
    fun saveRoomSelection(info: RoomSelectionInfo) {
        val data = Gson().toJson(info)
        userPutString("room_selection_info", data)
        kv.encode("room_selection_info", data)
    }

    /**
     * 保存一卡通余额
     * @param balance Double
     */
    fun saveCardBalance(balance: Double) {
        userPutString("card_balance", balance.toString())
        kv.encode("card_balance", balance)
    }

    /**
     * 获取一卡通余额
     * @return Double?
     */
    fun getCardBalance(): Double? {
        userGetString("card_balance")?.toDoubleOrNull()?.let { return it }
        if (!kv.containsKey("card_balance")) return null
        return kv.decodeDouble("card_balance").also {
            userPutString("card_balance", it.toString())
        }
    }

    fun getMockData(): Boolean {
        initGetString("mock_data")?.toBooleanStrictOrNull()?.let { return it }
        if (!kv.containsKey("mock_data")) return false
        return kv.decodeBool("mock_data").also {
            initPutString("mock_data", it.toString())
        }
    }

    fun setMockData(enable: Boolean) {
        initPutString("mock_data", enable.toString())
        kv.encode("mock_data", enable)
    }

    fun saveMockCurrentTimeMillis(value: Long) {
        initPutString("mock_current_time_millis", value.toString())
        kv.encode("mock_current_time_millis", value)
    }

    fun getMockCurrentTimeMillis(): Long? {
        initGetString("mock_current_time_millis")?.toLongOrNull()?.let { return it }
        if (!kv.containsKey("mock_current_time_millis")) return null
        return kv.decodeLong("mock_current_time_millis").also {
            initPutString("mock_current_time_millis", it.toString())
        }
    }

    fun clearMockCurrentTimeMillis() {
        initRemove("mock_current_time_millis")
        kv.removeValueForKey("mock_current_time_millis")
    }

    fun getGrayOverride(key: String): String? {
        return initGetString("gray_override_$key")?.takeIf { it.isNotBlank() }
    }

    fun setGrayOverride(key: String, value: String) {
        initPutString("gray_override_$key", value)
    }

    fun clearGrayOverride(key: String) {
        initRemove("gray_override_$key")
    }

    /**
     * 保存 GPA 排名信息
     */
    fun saveGpaRankInfo(gpaRankInfo: GpaRankInfo) {
        val data = Gson().toJson(gpaRankInfo)
        userPutString("gpa_rank_info", data)
        kv.encode("gpa_rank_info", data)
    }
    /**
     * 获取缓存的 GPA 排名信息
     */
    fun getGpaRankInfo(): GpaRankInfo? {
        val data = userGetStringOrMigrate("gpa_rank_info") { kv.decodeString("gpa_rank_info") } ?: ""
        return data.fromJson(GpaRankInfo::class.java)
    }
    /**
     * 清除 GPA 排名缓存
     */
    fun clearGpaRankInfo() {
        userRemove("gpa_rank_info")
        kv.removeValueForKey("gpa_rank_info")
    }
    /**
     * 保存失物招领校区缓存
     */
    fun saveLostFoundCampus(campus: List<CampusItem>) {
        kv.encode(
            "lost_found_campus",
            Gson().toJson(campus)
        )
    }

    /**
     * 获取失物招领校区缓存
     */
    fun getLostFoundCampus(): List<CampusItem> {
        val data =
            kv.decodeString("lost_found_campus") ?: ""

        if (data.isEmpty()) return emptyList()

        return data.fromJson(
            object : TypeToken<List<CampusItem>>() {}.type
        ) ?: emptyList()
    }

    /**
     * 保存失物招领类型缓存
     */
    fun saveLostFoundType(types: List<LostFoundTypeItem>) {
        kv.encode(
            "lost_found_type",
            Gson().toJson(types)
        )
    }

    /**
     * 获取失物招领类型缓存
     */
    fun getLostFoundType(): List<LostFoundTypeItem> {
        val data =
            kv.decodeString("lost_found_type") ?: ""

        if (data.isEmpty()) return emptyList()

        return data.fromJson(
            object : TypeToken<List<LostFoundTypeItem>>() {}.type
        ) ?: emptyList()
    }

    /**
     * 保存失物招领帖子缓存（按状态）
     */
    fun saveLostFoundList(
        state: Int,
        items: List<LostFoundItem>
    ) {
        kv.encode(
            "lost_found_list_$state",
            Gson().toJson(items)
        )
    }

    /**
     * 获取失物招领帖子缓存（按状态）
     */
    fun getLostFoundList(
        state: Int
    ): List<LostFoundItem> {
        val data =
            kv.decodeString(
                "lost_found_list_$state"
            ) ?: ""

        if (data.isEmpty()) return emptyList()

        return data.fromJson(
            object : TypeToken<List<LostFoundItem>>() {}.type
        ) ?: emptyList()
    }

    /**
     * 追加失物招领帖子缓存（分页）
     */
    fun appendLostFoundList(
        state: Int,
        newItems: List<LostFoundItem>
    ) {
        val oldList =
            getLostFoundList(state)

        val merged =
            oldList + newItems

        saveLostFoundList(
            state,
            merged
        )
    }

    /**
     * 清除指定状态帖子缓存
     */
    fun clearLostFoundList(
        state: Int
    ) {
        kv.removeValueForKey(
            "lost_found_list_$state"
        )
    }

    /**
     * 清除全部失物招领缓存
     */
    fun clearLostFoundCache() {
        kv.removeValueForKey("lost_found_campus")
        kv.removeValueForKey("lost_found_type")
        kv.removeValueForKey("lost_found_list_1")
        kv.removeValueForKey("lost_found_list_2")
    }
}
