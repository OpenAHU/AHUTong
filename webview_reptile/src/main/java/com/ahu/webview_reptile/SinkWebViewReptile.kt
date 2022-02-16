package com.ahu.webview_reptile

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.internal.wait
import java.lang.Exception
import kotlin.concurrent.thread

@SuppressLint("SetJavaScriptEnabled")
class SinkWebViewReptile(val webView: WebView) {
    val client = SinkWebViewClient()

    init {
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = client
        // 清理之前的Cookie
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush()
    }

    private fun login(loginCallback: (String, Throwable?) -> Unit) {
        client.loginCallback = loginCallback
        webView.loadUrl("https://wvpn.ahu.edu.cn/")
    }


    fun loginTeach() {
        login { str, e ->
            Log.e("SINK", str)
        }
    }

    suspend fun getCardMoney() {
        val response = withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .header("Accept", "application/json")
                .header("Cookie", CookieManager.getInstance().getCookie("https://wvpn.ahu.edu.cn/"))
                .url("https://wvpn.ahu.edu.cn/http/77726476706e69737468656265737421fff944d226387d1e7b0c9ce29b5b/tp_up/up/subgroup/getCardMoney")
                .post("{}".toRequestBody("application/json".toMediaType()))
                .build()
            return@withContext OkHttpClient.Builder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build()
                .newCall(request)
                .execute()
        }
        if (response.isSuccessful) {
            val json = response.body?.string() ?: return
            Log.e("SINK","json = ${json}")
        } else {
            Log.e("SINK","json = ${response.message}")
        }
    }

    suspend fun getSchedule() {
        val response = withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .header("Referer", "https://wvpn.ahu.edu.cn/https/77726476706e69737468656265737421fae05988777e69586b468ca88d1b203b/xsxkqk.aspx")
                .header("Cookie", CookieManager.getInstance().getCookie("https://wvpn.ahu.edu.cn/"))
                .url("https://wvpn.ahu.edu.cn/https/77726476706e69737468656265737421fae05988777e69586b468ca88d1b203b/xsxkqk.aspx?xh=Y02014373&gnmkdm=N121615")
                .post("ddlXN=2021-2022&ddlXQ=2".toRequestBody("application/x-www-form-urlencoded".toMediaType()))
                .build()
            return@withContext OkHttpClient.Builder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build()
                .newCall(request)
                .execute()
        }
        if (response.isSuccessful) {
            val html = response.body?.string() ?: return
            Log.e("SINK",html)
        } else {
            Log.e("SINK","error = ${response.message}")
        }
    }


}