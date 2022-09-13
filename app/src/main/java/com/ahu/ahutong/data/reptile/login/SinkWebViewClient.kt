package com.ahu.ahutong.data.reptile.login

import android.util.Log
import android.webkit.*
import com.ahu.ahutong.data.reptile.Constants
import com.ahu.ahutong.data.reptile.ReptileManager
import com.ahu.ahutong.data.reptile.ReptileUser

class SinkWebViewClient : WebViewClient() {
    companion object {
        const val STATUS_LOGIN_BEFORE = 0
        const val STATUS_LOGIN_ING = 1
        const val STATUS_LOGIN_SUCCESS = 2
        const val STATUS_LOGIN_FAILURE = 3
    }

    private var loginStatus = STATUS_LOGIN_BEFORE  // 0->未开始， 1->登录中， 2->成功， 3->登录失败
    private lateinit var user: ReptileUser
    private var loginCallback: (Int, Throwable?) -> Unit = { status, _ ->
    }

    fun login(view: WebView, user: ReptileUser, loginCallback: (Int, Throwable?) -> Unit) {
        this.user = user
        this.loginCallback = loginCallback
        view.loadUrl("https://wvpn.ahu.edu.cn/")
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        if (url == null) {
            return
        }
        when {
            // WVPN 的第一步
            url.contains(Constants.KEY_WVPN_LOGIN)-> {
                if (loginStatus != STATUS_LOGIN_BEFORE) {
                    loginStatus = STATUS_LOGIN_FAILURE
                    loginCallback(STATUS_LOGIN_FAILURE, Throwable("登录失败, 账号或者密码错误！"))
                    loginStatus = STATUS_LOGIN_BEFORE
                    return
                }
                // 设置登录模式
                ReptileManager.getInstance().isWVPN = true
                // 登录函数
                view?.loadUrl(Constants.CMD_LOGIN.format(user.username, user.password))
                loginCallback(STATUS_LOGIN_ING, null)
                loginStatus = STATUS_LOGIN_ING
            }
            // one的第一步
            url.contains(Constants.KEY_ONE_LOGIN) -> {
                if (loginStatus != STATUS_LOGIN_BEFORE) {
                    loginStatus = STATUS_LOGIN_FAILURE
                    loginCallback(STATUS_LOGIN_FAILURE, Throwable("登录失败, 账号或者密码错误！"))
                    loginStatus = STATUS_LOGIN_BEFORE
                    return
                }
                // 设置登录模式
                ReptileManager.getInstance().isWVPN = false
                // 登录函数
                view?.loadUrl(Constants.CMD_LOGIN.format(user.username, user.password))
                loginCallback(STATUS_LOGIN_ING, null)
                loginStatus = STATUS_LOGIN_ING
            }
            // 跳到教务系统
            url.contains(Constants.KEY_MAIN) -> {
                loginStatus = STATUS_LOGIN_ING
                view?.loadUrl(if (ReptileManager.getInstance().isWVPN) Constants.CMD_GOTO_TECH else Constants.CMD_GOTO_TECH_ONE)
            }
            // WVPN 的最后一步
            url.contains(Constants.KEY_TEACH_MAIN) && ReptileManager.getInstance().isWVPN -> {
                loginStatus = STATUS_LOGIN_SUCCESS
                loginCallback(STATUS_LOGIN_SUCCESS, null)
            }
            // one的最后一步
            url.contains(Constants.KEY_TEACH_MAIN_ONE) && !ReptileManager.getInstance().isWVPN -> {
                loginStatus = STATUS_LOGIN_SUCCESS
                loginCallback(STATUS_LOGIN_SUCCESS, null)
            }
        }
    }

}