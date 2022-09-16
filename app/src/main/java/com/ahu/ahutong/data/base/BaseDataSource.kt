package com.ahu.ahutong.data.base

import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.api.AHUService
import com.ahu.ahutong.data.model.*

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

    suspend fun getGrade(): AHUResponse<Grade>


    suspend fun getCardMoney(): AHUResponse<Card>
}