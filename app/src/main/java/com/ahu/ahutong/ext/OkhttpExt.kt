package com.ahu.ahutong.ext

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Response
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CompletionHandler
import okhttp3.Callback
import okhttp3.ResponseBody
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 这个拓展出自coli, 协程中使用okhttp的同步请求，会出现警告
 * 我本以为是IO的警告，加上try、catch还是不行，鼠标一动到黄色位置,提示警告： Inappropriate blocking method call
 * https://juejin.cn/post/7059279388923133959
 */
internal suspend inline fun Call.await(): Response {
    return suspendCancellableCoroutine { continuation ->
        val callback = ContinuationCallback(this, continuation)
        enqueue(callback)
        continuation.invokeOnCancellation(callback)
    }
}

/**
 * 模仿coli
 */
internal suspend inline fun ResponseBody.awaitString(): String {
    return suspendCancellableCoroutine { continuation ->
        try {
            val string = this.string()
            continuation.resume(string)
        } catch (e: Exception) {
            continuation.cancel()
        }
    }
}


internal class ContinuationCallback(
    private val call: Call,
    private val continuation: CancellableContinuation<Response>
) : Callback, CompletionHandler {

    override fun onResponse(call: Call, response: Response) {
        continuation.resume(response)
    }

    override fun onFailure(call: Call, e: IOException) {
        if (!call.isCanceled()) {
            continuation.resumeWithException(e)
        }
    }

    override fun invoke(cause: Throwable?) {
        try {
            call.cancel()
        } catch (_: Throwable) {
        }
    }
}