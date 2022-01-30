package com.ahu.ahutong.data.fake

import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.base.BaseDataSource
import com.ahu.ahutong.data.model.*

class FakeDataSource : BaseDataSource {

    override suspend fun getSchedule(
        schoolYear: String,
        schoolTerm: String
    ): AHUResponse<List<Course>> {
        return default()
    }


    override suspend fun getExamInfo(
        schoolYear: String,
        schoolTerm: String
    ): AHUResponse<List<Exam>> {
        return default()
    }

    override suspend fun getEmptyRoom(
        campus: String,
        weekday: String,
        weekNum: String,
        time: String
    ): AHUResponse<List<Room>> {
        return default()
    }

    override suspend fun getGrade(): AHUResponse<Grade> {
        return default()
    }


    override suspend fun getCardMoney(): AHUResponse<Card> {
        return default()
    }

    private fun <T> default(): AHUResponse<T> {
        val res = AHUResponse<T>()
        res.code = -1
        res.msg = "请先进行登录"
        return res
    }
}