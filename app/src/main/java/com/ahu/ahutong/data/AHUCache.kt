package com.ahu.ahutong.data

import com.ahu.ahutong.data.model.Course
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


    fun saveSchedule(schoolYear: String, schoolTerm: String, schdule: List<Course>){
        val data = Gson().toJson(schdule)
        kv.encode("${schoolYear}-${schoolTerm}.schedule", data)
    }

    fun getSchedule(schoolYear: String, schoolTerm: String): List<Course>{
        val data = kv.decodeString("${schoolYear}-${schoolTerm}.schedule") ?: ""
        return Gson().fromJson(data,  object: TypeToken<List<Course>>() {}.type)
    }



}