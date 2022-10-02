package com.ahu.webview_reptile

import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient

class SinkWebViewClient : WebViewClient() {
    private var loginStatus = 0 // 0->未开始， 1->登录中， 2->成功， 3->登录失败
    private var isWvpn: Boolean = false
    var loginCallback: (String, Throwable?) -> Unit = { str, _ ->
        Log.e("SINK", str)
    }

    fun getIsWvpn(): Boolean {
        return isWvpn
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        if (request != null) {
            request.requestHeaders.forEach { k, v ->
                Log.e("SINK", "k = $k , v = $v")
            }
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        if (url == null) {
            return
        }
        if (url.contains("one.ahu.edu.cn")) {
            isWvpn = false
        } else if (url.contains("wvpn.ahu.edu.cn")) {
            isWvpn = true
        }
        val key = if (isWvpn) KEY_LOGIN else KEY_LOGIN_ONE
        val loadUrl =
            if (isWvpn) "javascript:window.location='https://wvpn.ahu.edu.cn/https/77726476706e69737468656265737421fae05988777e69586b468ca88d1b203b/login_cas.aspx'"
            else "https://jwxt0.ahu.edu.cn/login_cas.aspx"
        val main =
            if (isWvpn) "77726476706e69737468656265737421fae05988777e69586b468ca88d1b203b/xs_main.aspx" else "xs_main.aspx"
        when {
            url.contains(key) -> {
                if (loginStatus != 0) {
                    loginStatus = 3
                    loginCallback("登录失败", Throwable("登录失败, 账号或者密码错误！"))
                    loginStatus = 0
                    return
                }
                // 登录函数
                view?.loadUrl(CMD_LOGIN)
                loginCallback("登录中", null)
                loginStatus = 1
            }

            url.contains("m=up#act=portal/viewhome") -> {
                loginStatus = 1
                view?.loadUrl(loadUrl)
            }

            url.contains(main) -> {
                loginStatus = 2
                loginCallback("登录成功", null)
            }
        }
    }
}
