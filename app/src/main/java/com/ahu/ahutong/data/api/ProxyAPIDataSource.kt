package com.ahu.ahutong.data.api

import com.ahu.ahutong.AHUApplication
import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.base.BaseDataSource
import com.ahu.ahutong.data.model.*

class ProxyAPIDataSource(private val apiDataSource: APIDataSource) : BaseDataSource {

    override suspend fun getSchedule(
        schoolYear: String,
        schoolTerm: String
    ): AHUResponse<List<Course>> {
        return try {
            apiDataSource.getSchedule(schoolYear, schoolTerm)
        } catch (e: Exception) {
            failureHandle(e)
        }
    }

    override suspend fun getExamInfo(
        schoolYear: String,
        schoolTerm: String
    ): AHUResponse<List<Exam>> {
        return try {
            apiDataSource.getExamInfo(schoolYear, schoolTerm)
        } catch (e: Exception) {
            failureHandle(e)
        }
    }

    override suspend fun getEmptyRoom(
        campus: String,
        weekday: String,
        weekNum: String,
        time: String
    ): AHUResponse<List<Room>> {
        return try {
            apiDataSource.getEmptyRoom(campus, weekday, weekNum, time)
        } catch (e: Exception) {
            failureHandle(e)
        }
    }

    override suspend fun getGrade(): AHUResponse<Grade> {
        return try {
            apiDataSource.getGrade()
        } catch (e: Exception) {
            failureHandle(e)
        }
    }

    override suspend fun getCardMoney(): AHUResponse<Card> {
        return try {
            apiDataSource.getCardMoney()
        } catch (e: Exception) {
            failureHandle(e)
        }
    }

    private fun <T> failureHandle(e: Exception): AHUResponse<T> {
        e.printStackTrace()
        AHUApplication.loginType.setValue(User.UserType.AHU_LOCAL)
        val ahuResponse = AHUResponse<T>()
        ahuResponse.msg = "服务器或网络异常，请切换本地数据源！"
        ahuResponse.code = -1
        return ahuResponse
    }
}



