package com.ahu.ahutong.data.crawler.net

import android.util.Log
import com.ahu.ahutong.AHUApplication
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.dao.AHUCache
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator : Authenticator {

    val TAG = "TokenAuthenticator"

    override fun authenticate(route: Route?, response: Response): Request? {

        if (response.request.header("Authorization") != null && response.code == 302) {
            Log.e(TAG, "authenticate: 这是什么情况？", )
            return null
        }

        synchronized(AHUApplication.reLoginMutex) {
            if (!AHUApplication.sessionExpired) {
                return response.request.newBuilder()
                    .build()
            }
            Log.e(TAG, "authenticate: 尝试重新登录", )
            return runBlocking {
                val loginResponse = AHURepository.loginWithCrawler(
                    AHUCache.getCurrentUser()!!.xh.toString(),
                    AHUCache.getWisdomPassword().toString()
                )

                if (loginResponse.isSuccessful) {
                    AHUApplication.sessionExpired = false
                    Log.e(TAG, "authenticate: 登录成功", )
                    return@runBlocking response.request.newBuilder()
                        .build()
                } else {
                    AHUApplication.sessionExpired = true
                    Log.e(TAG, "authenticate: 登录失败了", )
                    return@runBlocking null
                }
            }
        }

    }
}