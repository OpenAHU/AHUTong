package com.ahu.ahutong.data.api

import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.base.BaseDataSource
import com.ahu.ahutong.data.model.BathRoom
import com.ahu.ahutong.data.model.Card
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.Grade

/**
 * @Author: SinkDev
 * @Date: 2021/7/31-下午8:46
 * @Email: 468766131@qq.com
 */
class APIDataSource : BaseDataSource {
    override suspend fun getSchedule(
        schoolYear: String,
        schoolTerm: String
    ): AHUResponse<List<Course>> {
        return AHUService.API.getSchedule(schoolYear, schoolTerm)
    }

    override suspend fun getGrade(): AHUResponse<Grade> {
        return AHUService.API.getGrade()
    }

    override suspend fun getCardMoney(): AHUResponse<Card> {
        return AHUService.API.getCardMoney()
    }

    override suspend fun getBathRooms(): AHUResponse<List<BathRoom>> {
        return AHUService.API.getBathRooms()
    }
}
