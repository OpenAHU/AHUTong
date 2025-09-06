package com.ahu.ahutong.data.crawler.manager

import android.content.Context
import android.util.Log
import arch.sink.utils.Utils
import androidx.core.content.edit
import com.ahu.ahutong.data.crawler.api.ycard.YcardApi
import okhttp3.Cookie
import java.net.URLDecoder

object TokenManager {

    val TAG = "TokenManager"

    private var token :String? = null


    fun getToken():String?{
        if (!token.isNullOrBlank()) return token

        Log.e(TAG, "getToken: token is null", )

        try {

            CookieManager.cookieJar.logAllCookies()

            val loginResponse = YcardApi.API.login().execute()      //假设已经登陆过one.ahu.ehu.cn
            val redirectUrl = loginResponse.raw().request.url.toString()

            val regex = Regex("[?&]ticket=([^&]+)")
            val match = regex.find(redirectUrl)
            val ticket = match?.groupValues?.get(1) ?: return null
            Log.e(TAG, "ticket: $ticket", )
            val decodedUsername = URLDecoder.decode(URLDecoder.decode(ticket, "UTF-8"), "UTF-8")

            val tokenResponse = YcardApi.API.getToken(
                username = decodedUsername,
                password = decodedUsername
            ).execute()

            if (tokenResponse.isSuccessful) {
                token = tokenResponse.body()?.access_token
                Log.e(TAG, "getToken: $token", )
                return token
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun clear(){
        Log.e(TAG, "clear: Token", )
        token = null
    }

}