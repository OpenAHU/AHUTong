package com.ahu.ahutong.data.crawler.net

import android.util.Log
import com.ahu.ahutong.AHUApplication
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.crawler.manager.CookieManager
import com.ahu.ahutong.data.crawler.manager.TokenManager
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


        // 每个接口发现重定向都可能会进入触发重新登录，这里要保证只有一个请求在重新登录
        synchronized(AHUApplication.reLoginMutex) {


            
            if (!AHUApplication.sessionExpired) { // 新请求如果发现之前有重新登录成功了，那就直接重新构造请求
                Log.e(TAG, "authenticate: 成功登录了", )
                return response.request.newBuilder()
                    .build()
            }


            //
            Log.e(TAG, "authenticate: ${response.request.url} 尝试重新登录", )
            return runBlocking {
                CookieManager.cookieJar.clear()
                TokenManager.clear()


                AHUCache.getCurrentUser()?.let{
                    val loginResponse = AHURepository.loginWithCrawler(
                        it.xh.toString(),
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

                Log.e(TAG, "authenticate: 未找到用户信息", )
                AHUApplication.sessionExpired = true
                return@runBlocking null
            }
        }

    }
}