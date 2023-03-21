package com.ahu.ahutong.data.reptile

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebView
import com.ahu.ahutong.data.reptile.login.SinkWebViewClient

@SuppressLint("SetJavaScriptEnabled")
class WebViewLoginer(val webView: WebView) {
    val client = SinkWebViewClient()

    init {
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = client
        // 清理之前的Cookie
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
    }

    fun login(user: ReptileUser, loginCallback: (Int, Throwable?) -> Unit) {
        client.login(webView, user, loginCallback)
    }
}
