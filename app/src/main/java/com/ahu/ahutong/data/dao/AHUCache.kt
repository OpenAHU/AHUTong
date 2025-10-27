package com.ahu.ahutong.data.dao

import arch.sink.utils.Utils
import com.ahu.ahutong.data.model.*
import com.ahu.ahutong.ext.fromJson
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
        MMKV.initialize(Utils.getApp())
    }

    private val kv: MMKV = MMKV.mmkvWithID("ahu")

    /**
     * 清除全部数据
     */
    fun clearAll() {
        kv.clearAll()
    }

    /**
     * 保存本地User对象
     * @param user User
     */
    fun saveCurrentUser(user: User) {
        val data = Gson().toJson(user)
        kv.encode("current_user", data)
    }

    /**
     * 清除本地登陆状态
     */
    fun clearCurrentUser() {
        kv.encode("current_user", "")
    }

    /**
     * 获取本地User对象
     * @return User?
     */
    fun getCurrentUser(): User? {
        val data = kv.decodeString("current_user") ?: ""
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
        kv.encode("password_wisdom", password)
    }

    /**
     * 获取智慧安大密码
     * @return String?
     */
    fun getWisdomPassword(): String? {
        return kv.decodeString("password_wisdom")
    }

    /**
     * 保存课程表
     * @param schoolYear String
     * @param schoolTerm String
     * @param schdule List<Course>
     */
    fun saveSchedule(schoolYear: String, schoolTerm: String, schedule: List<Course>) {
        val data = Gson().toJson(schedule)
        kv.putString("$schoolYear-$schoolTerm.schedule", data)
    }

    fun saveSchedule(schoolTerm: String,schedule: List<Course>) {
        val data = Gson().toJson(schedule)
        kv.putString("$schoolTerm.schedule", data) // 2025-2026-1
    }

    /**
     * 获取课程表
     * @param schoolYear String
     * @param schoolTerm String
     * @return List<Course>
     */
    fun getSchedule(schoolYear: String, schoolTerm: String): List<Course>? {
        val data = kv.getString("$schoolYear-$schoolTerm.schedule", "") ?: ""
        return data.fromJson(object : TypeToken<List<Course>>() {}.type)
    }

    fun getSchedule(schoolTerm: String): List<Course>? {
        val data = kv.getString("$schoolTerm.schedule", "") ?: ""
        return data.fromJson(object : TypeToken<List<Course>>() {}.type)
    }

    /**
     * 保存成绩
     * @param grade Grade
     */
    fun saveGrade(grade: Grade) {
        val data = Gson().toJson(grade)
        kv.encode("grade", data)
    }

    /**
     * 获取成绩
     * @return Grade
     */
    fun getGrade(): Grade? {
        val data = kv.decodeString("grade") ?: ""
        return data.fromJson(Grade::class.java)
    }

    /**
     * 保存考试信息
     * @param exams List<Exam>
     */
    fun saveExamInfo(exams: List<Exam>) {
        val data = Gson().toJson(exams)
        kv.encode("exams", data)
    }

    /**
     * 获取考试信息
     * @return List<Exam>?
     */
    fun getExamInfo(): List<Exam>? {
        val data = kv.decodeString("exams") ?: ""
        return data.fromJson(object : TypeToken<List<Exam>>() {}.type)
    }

    /**
     * 获取开学时间
     * @param schoolYear String yyyy-yyyy
     * @param schoolTerm String 1 or 2
     * @return String? yyyy-MM-dd
     */
    fun getSchoolTermStartTime(schoolYear: String, schoolTerm: String): String? {
        return kv.decodeString("startTime-$schoolYear-$schoolTerm")
    }

    /**
     * 保存开学时间
     * @param schoolYear String yyyy-yyyy
     * @param schoolTerm String 1 or 2
     * @param startTime String yyyy-MM-dd
     */
    fun saveSchoolTermStartTime(schoolYear: String, schoolTerm: String, startTime: String) {
        kv.encode("startTime-$schoolYear-$schoolTerm", startTime)
    }

    /**
     * 获取默认的学年
     * @return String?
     */
    fun getSchoolYear(): String? {
        return kv.decodeString("defaultSchoolYear")
    }

    /**
     * 保存默认的学年
     * @param schoolYear String
     */
    fun saveSchoolYear(schoolYear: String) {
        kv.encode("defaultSchoolYear", schoolYear)
    }

    /**
     * 获取默认学期
     * @return String?
     */
    fun getSchoolTerm(): String? {
        return kv.getString("defaultSchoolTerm", null)
    }

    /**
     * 保存默认的学期
     * @param schoolTerm String
     */
    fun saveSchoolTerm(schoolTerm: String) {
        kv.putString("defaultSchoolTerm", schoolTerm)
    }

    /**
     * 是否显示非本周课程
     * @return Boolean
     */
    fun isShowAllCourse(): Boolean {
        return kv.getBoolean("isShowAllCourse", false)
    }

    /**
     * 保存是否显示非本周课程
     * @param isCourse Boolean
     */
    fun saveIsShowAllCourse(isCourse: Boolean) {
        kv.putBoolean("isShowAllCourse", isCourse)
    }

    fun isShowWidgetTip(): Boolean {
        return kv.getBoolean("is_show_widget_dialog", true)
    }

    fun ignoreWidgetTip() {
        kv.putBoolean("is_show_widget_dialog", false)
    }

    fun logout() {
        clearCurrentUser()
        saveWisdomPassword("")
    }


    fun savePhone(phone:String){
        kv.putString("phone",phone)
    }

    fun getPhone() : String?{
        return kv.getString("phone",null)
    }


    fun setJwxtStudentId(id: String){
        kv.putString("jwxt_stu_id",id)
    }

    fun getJwxtStudentId() : String?{
        return kv.getString("jwxt_stu_id",null)
    }


    fun saveString(key: String ,value : String){
        kv.putString("key",value)
    }


    fun isAgreementAccepted(): Boolean{
        return kv.getBoolean("agreementAccepted",false)
    }

    fun setAgreementAccepted(){
        kv.putBoolean("agreementAccepted",true)
    }

    /**
     * 获取房间选择信息
     * @return RoomSelectionInfo?
     */
    fun getRoomSelection(): RoomSelectionInfo? {
        val data = kv.decodeString("room_selection_info") ?: ""
        return data.fromJson(RoomSelectionInfo::class.java)
    }

    /**
     * 保存电费累计充值信息
     * @param info ElectricityChargeInfo
     */
    fun saveElectricityChargeInfo(info: ElectricityChargeInfo) {
        val data = Gson().toJson(info)
        kv.encode("electricity_charge_acl", data)
    }

    /**
     * 获取电费累计充值信息
     * @return ElectricityChargeInfo?
     */
    fun getElectricityChargeInfo(): ElectricityChargeInfo? {
        val data = kv.decodeString("electricity_charge_acl") ?: ""
        if (data.isEmpty()) {
            return null
        }
        return data.fromJson(ElectricityChargeInfo::class.java)
    }

    /**
     * 清除电费累计充值信息
     */
    fun clearElectricityChargeInfo() {
        kv.removeValueForKey("electricity_charge_acl")
    }

    /**
     * 保存房间选择信息
     * @param info RoomSelectionInfo
     */
    fun saveRoomSelection(info: RoomSelectionInfo) {
        val data = Gson().toJson(info)
        kv.encode("room_selection_info", data)
    }
}
