package com.ahu.ahutong.data.api

import com.ahu.ahutong.AHUApplication
import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.base.BaseDataSource
import com.ahu.ahutong.data.model.*
import retrofit2.HttpException

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
        val ahuResponse = AHUResponse<T>()
        if (e is HttpException && e.code() == 400) {
            // token 过期
            AHUApplication.retryLogin.callFromOtherThread()
            ahuResponse.msg = "登录状态过期！"
        }else{
            AHUApplication.loginType.setValue(User.UserType.AHU_LOCAL)
            ahuResponse.msg = "服务器或网络异常，请切换本地数据源！"
        }
        e.printStackTrace()
        ahuResponse.code = -1
        return ahuResponse
    }
}



