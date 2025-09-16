package com.ahu.ahutong.data.api

import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.base.BaseDataSource
import com.ahu.ahutong.data.crawler.model.ycard.CardInfo
import com.ahu.ahutong.data.model.BathRoom
import com.ahu.ahutong.data.model.BathroomTelInfo
import com.ahu.ahutong.data.model.Card
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.Grade

/**
 * @Author: SinkDev
 * @Date: 2021/7/31-下午8:46
 * @Email: 468766131@qq.com
 */
//class APIDataSource : BaseDataSource {
//    override suspend fun getSchedule(
//        schoolYear: String,
//        schoolTerm: String
//    ): AHUResponse<List<Course>> {
//        return AHUService.API.getSchedule(schoolYear, schoolTerm)
//    }
//
//    override suspend fun getSchedule(): AHUResponse<List<Course>> {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun getGrade(): AHUResponse<Grade> {
//        return AHUService.API.getGrade()
//    }
//
//    override suspend fun getCardMoney(): AHUResponse<Card> {
//        return AHUService.API.getCardMoney()
//    }
//
//    override suspend fun getBathRooms(): AHUResponse<List<BathRoom>> {
//        return AHUService.API.getBathRooms()
//    }
//
//    override suspend fun getBathroomTelInfo(
//        bathroom: String,
//        tel: String
//    ): AHUResponse<BathroomTelInfo> {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun getCardInfo(): AHUResponse<CardInfo> {
//        TODO("Not yet implemented")
//    }
//}
