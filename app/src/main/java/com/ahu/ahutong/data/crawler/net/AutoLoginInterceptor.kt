package com.ahu.ahutong.data.crawler.net

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response


class AutoLoginInterceptor : Interceptor {

    val TAG = "AutoLoginInterceptor"

    override fun intercept(chain: Interceptor.Chain): Response {
        var originalRequest = chain.request()
        var response = chain.proceed(originalRequest)
        Log.e(TAG, "${originalRequest.url} ${response.code} ${response.headers["Location"]} ")

        val location = response.header("Location")
        if (response.code == 302 && location != null && (location.contains("tologin") || location.contains("refer"))) {
            return response.newBuilder()
                .code(401)
                .build()
        }

        return response
    }
}