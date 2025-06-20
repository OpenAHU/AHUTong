package com.ahu.ahutong.data.base

import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.model.BathRoom
import com.ahu.ahutong.data.model.Card
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.Grade

/**
 * @Author: Sink
 * @Date: 2021/7/31-下午8:40
 * @Email: 468766131@qq.com
 */
interface BaseDataSource {

    /**
     * getSchedule
     * @param schoolYear String 2020-2021
     * @param schoolTerm String 1,2
     * @return AHUResponse<List<Course>>
     */
    suspend fun getSchedule(schoolYear: String, schoolTerm: String): AHUResponse<List<Course>>

    /**
     * getSchedule (auto get schedule of this semester)
     */
    suspend fun getSchedule() : AHUResponse<List<Course>>

    suspend fun getGrade(): AHUResponse<Grade>

    suspend fun getCardMoney(): AHUResponse<Card>

    suspend fun getBathRooms(): AHUResponse<List<BathRoom>>
}
