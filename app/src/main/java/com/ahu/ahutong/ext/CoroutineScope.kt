package com.ahu.ahutong.ext

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import arch.sink.utils.Utils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException


val GlobalCoroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
    Log.e("CoroutineExceptionHandler", "协程异常: ${throwable::class.java} - ${throwable.message}")
    Handler(Looper.getMainLooper()).post {
        when (throwable) {
            is UnknownHostException -> {
                Toast.makeText(
                    Utils.getApp(),
                    "网络不可用，请检查网络连接",
                    Toast.LENGTH_SHORT
                ).show()
            }
            is SocketTimeoutException -> {
                Toast.makeText(
                    Utils.getApp(),
                    "请求超时，请重试",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                Toast.makeText(
                    Utils.getApp(),
                    "发生未知错误: ${throwable.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

fun CoroutineScope.launchSafe(
    block: suspend CoroutineScope.() -> Unit
) = launch(GlobalCoroutineExceptionHandler) {
    block()
}