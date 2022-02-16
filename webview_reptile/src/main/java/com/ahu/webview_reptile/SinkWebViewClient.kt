package com.ahu.webview_reptile

import android.util.Log
import android.webkit.*
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class SinkWebViewClient : WebViewClient() {
    private var loginStatus = 0  // 0->未开始， 1->登录中， 2->成功， 3->登录失败

    var loginCallback: (String, Throwable?) -> Unit = { str, _ ->
        Log.e("SINK", str)
    }


    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        if (url == null) {
            return
        }
        when {
            url.contains(KEY_LOGIN) -> {
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
                view?.loadUrl("javascript:window.location='https://wvpn.ahu.edu.cn/https/77726476706e69737468656265737421fae05988777e69586b468ca88d1b203b/login_cas.aspx'")
            }
            url.contains("77726476706e69737468656265737421fae05988777e69586b468ca88d1b203b/xs_main.aspx") -> {
                loginStatus = 2
                loginCallback("登录成功", null)
            }
        }
    }

}