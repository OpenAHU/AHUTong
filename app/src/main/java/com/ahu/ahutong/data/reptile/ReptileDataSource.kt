package com.ahu.ahutong.data.reptile

import android.widget.Toast
import arch.sink.utils.Utils
import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.base.BaseDataSource
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.*
import com.ahu.ahutong.data.reptile.store.DefaultCookieStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @Author: SinkDev
 * @Date: 2021/8/13-下午2:45
 * @Email: 468766131@qq.com
 */
class ReptileDataSource(user: ReptileUser): BaseDataSource {
    init {
        //初始化
        ReptileManager.getInstance()
            .setCookieStore(DefaultCookieStore())
            .setCurrentUser(user.username, user.password)

    }
    override suspend fun getSchedule(
        schoolYear: String,
        schoolTerm: String
    ): AHUResponse<List<Course>> {
       return Reptile.getSchedule(schoolYear, schoolTerm)
    }

    override suspend fun getExamInfo(
        schoolYear: String,
        schoolTerm: String
    ): AHUResponse<List<Exam>> {
        return Reptile.getExam(schoolYear, schoolTerm)
    }

    override suspend fun getEmptyRoom(
        campus: String,
        weekday: String,
        weekNum: String,
        time: String
    ): AHUResponse<List<Room>> {
       return Reptile.getEmptyRoom(campus, weekday, weekNum, time)
    }

    override suspend fun getGrade(): AHUResponse<Grade> {
       return Reptile.getGrade()
    }

    override suspend fun getNews(): AHUResponse<List<News>> {
        //TODO
        withContext(Dispatchers.Main){
            Toast.makeText(Utils.getApp(), "本地爬虫暂不支持此项功能！", Toast.LENGTH_SHORT).show()
        }
        return AHUResponse()
    }

    override suspend fun getCardMoney(): AHUResponse<String> {
       return Reptile.getCardMoney()
    }
}