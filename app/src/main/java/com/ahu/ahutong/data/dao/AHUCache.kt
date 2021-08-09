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
     * 保存课程表
     * @param schoolYear String
     * @param schoolTerm String
     * @param schdule List<Course>
     */
    fun saveSchedule(schoolYear: String, schoolTerm: String, schdule: List<Course>) {
        val data = Gson().toJson(schdule)
        kv.encode("${schoolYear}-${schoolTerm}.schedule", data)
    }

    /**
     * 获取课程表
     * @param schoolYear String
     * @param schoolTerm String
     * @return List<Course>
     */
    fun getSchedule(schoolYear: String, schoolTerm: String): List<Course>? {
        val data = kv.decodeString("${schoolYear}-${schoolTerm}.schedule") ?: ""
        return data.fromJson(object : TypeToken<List<Course>>() {}.type)
    }

    /**
     * 保存新闻
     * @param news List<News>
     */
    fun saveNews(news: List<News>) {
        val data = Gson().toJson(news)
        kv.encode("news", data)
    }

    /**
     * 获取新闻
     * @return List<News>
     */
    fun getNews(): List<News>? {
        val data = kv.decodeString("news") ?: ""
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
        return kv.decodeString("startTime-${schoolYear}-${schoolTerm}")
    }

    /**
     * 保存开学时间
     * @param schoolYear String yyyy-yyyy
     * @param schoolTerm String 1 or 2
     * @param startTime String yyyy-MM-dd
     */
    fun saveSchoolTermStartTime(schoolYear: String, schoolTerm: String, startTime: String){
        kv.encode("startTime-${schoolYear}-${schoolTerm}", startTime)
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
    fun saveSchoolYear(schoolYear: String){
        kv.encode("defaultSchoolYear", schoolYear)
    }

    /**
     * 获取默认学期
     * @return String?
     */
    fun getSchoolTerm(): String?{
        return kv.decodeString("defaultSchoolTerm")
    }

    /**
     * 保存默认的学期
     * @param schoolTerm String
     */
    fun saveSchoolTerm(schoolTerm: String){
        kv.encode("defaultSchoolTerm", schoolTerm)
    }

    /**
     * 是否显示非本周课程
     * @return Boolean
     */
    fun isShowAllCourse(): Boolean {
        return kv.decodeBool("isShowAllCourse", false)
    }


}