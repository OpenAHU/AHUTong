package com.ahu.ahutong.data.reptile

import android.widget.Toast
import arch.sink.utils.Utils
import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.base.BaseDataSource
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.*
import com.ahu.ahutong.data.reptile.store.DefaultCookieStore
import com.ahu.ahutong.ext.createFailureResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @Author: SinkDev
 * @Date: 2021/8/13-下午2:45
 * @Email: 468766131@qq.com
 */
class ReptileDataSource(user: ReptileUser) : BaseDataSource {
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
        return checkLoginStatus() ?: WebViewReptile.getSchedule(schoolYear, schoolTerm)
    }


    override suspend fun getGrade(): AHUResponse<Grade> {
        return checkLoginStatus() ?: WebViewReptile.getGrade()
    }


    override suspend fun getCardMoney(): AHUResponse<Card> {
        return checkLoginStatus() ?: WebViewReptile.getCardMoney()
    }

    private fun <T> checkLoginStatus(): AHUResponse<T>? {
        if (!ReptileManager.getInstance().isLoginStatus) {
            return createFailureResponse("本地爬虫正在登录中,或当前登录密码错误！")
        }
        return null
    }
}