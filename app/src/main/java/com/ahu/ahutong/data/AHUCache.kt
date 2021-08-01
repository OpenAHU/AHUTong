package com.ahu.ahutong.data

import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV
import java.util.*

/**
 * @Author SinkDev
 * @Date 2021/7/27-16:49
 * @Email 468766131@qq.com
 */
object AHUCache {
    private val kv: MMKV = MMKV.mmkvWithID("ahu")

    /**
     * 保存本地User对象
     * @param user User
     */
    fun saveCurrentUser(user: User){
        val data = Gson().toJson(user)
        kv.encode("current_user", data)
    }
    /**
     * 清除本地登陆状态
     */
    fun clearCurrentUser(){
        kv.encode("current_user", "")
    }
    /**
     * 获取本地User对象
     * @return User?
     */
    fun getCurrentUser(): User?{
        val data = kv.decodeString("current_user")
        if (data.isNullOrEmpty()){
            return null
        }
        return Gson().fromJson(data, User::class.java)
    }
    /**
     * 是否登录
     * @return Boolean
     */
    fun isLogin(): Boolean{
        return if (getCurrentUser() == null) false else true
    }

    /**
     * 保存课程表
     * @param schoolYear String
     * @param schoolTerm String
     * @param schdule List<Course>
     */
    fun saveSchedule(schoolYear: String, schoolTerm: String, schdule: List<Course>){
        val data = Gson().toJson(schdule)
        kv.encode("${schoolYear}-${schoolTerm}.schedule", data)
    }

    /**
     * 获取课程表
     * @param schoolYear String
     * @param schoolTerm String
     * @return List<Course>
     */
    fun getSchedule(schoolYear: String, schoolTerm: String): List<Course>{
        val data = kv.decodeString("${schoolYear}-${schoolTerm}.schedule") ?: ""
        return Gson().fromJson(data,  object: TypeToken<List<Course>>() {}.type)
    }



}