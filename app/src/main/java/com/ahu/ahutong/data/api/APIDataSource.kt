package com.ahu.ahutong.data.api

import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.base.BaseDataSource
import com.ahu.ahutong.data.model.*

/**
 * @Author: SinkDev
 * @Date: 2021/7/31-下午8:46
 * @Email: 468766131@qq.com
 */
class APIDataSource: BaseDataSource {
    override suspend fun getSchedule(
        schoolYear: String,
        schoolTerm: String
    ): AHUResponse<List<Course>> {
        return AHUService.API.getSchedule(schoolYear, schoolTerm)
    }

    override suspend fun getExamInfo(
        schoolYear: String,
        schoolTerm: String
    ): AHUResponse<List<Exam>> {
        return AHUService.API.getExamInfo(schoolYear, schoolTerm)
    }

    override suspend fun getEmptyRoom(
        campus: String,
        weekday: String,
        weekNum: String,
        time: String
    ): AHUResponse<List<Room>> {
       return AHUService.API.getEmptyRoom(campus, weekday, weekNum, time)
    }

    override suspend fun getGrade(): AHUResponse<Grade> {
        return AHUService.API.getGrade()
    }


    override suspend fun getCardMoney(): AHUResponse<Card> {
        return AHUService.API.getCardMoney()
    }
}