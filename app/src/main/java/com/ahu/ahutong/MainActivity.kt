package com.ahu.ahutong

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.widget.Toast
import arch.sink.ui.BarConfig
import arch.sink.ui.page.BaseActivity
import arch.sink.ui.page.DataBindingConfig
import arch.sink.utils.NightUtils
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.User
import com.ahu.ahutong.data.reptile.ReptileManager
import com.ahu.ahutong.data.reptile.ReptileUser
import com.ahu.ahutong.data.reptile.WebViewLoginer
import com.ahu.ahutong.data.reptile.login.SinkWebViewClient
import com.ahu.ahutong.databinding.ActivityMainBinding
import com.ahu.ahutong.ext.buildDialog
import com.ahu.ahutong.ui.page.state.MainViewModel
import com.ahu.ahutong.widget.ClassWidget

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private lateinit var mState: MainViewModel
    private lateinit var loginer: WebViewLoginer
    override fun initViewModel() {
        mState = getActivityScopeViewModel(MainViewModel::class.java);
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_main, BR.state, mState)
    }

    override fun getBarConfig(): BarConfig {
        val barConfig = BarConfig()
        if (NightUtils.isNightMode(this)) {
            barConfig.dark()
        } else {
            barConfig.light()
        }
        return barConfig
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeData()
        //更新小部件数据
        val manager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, ClassWidget::class.java)
        val appWidgetIds = manager.getAppWidgetIds(componentName)
        manager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_listview)
        // 检查更新
        mState.getAppLatestVersion()
        // 日活统计接口
        mState.addAppAccess()
        // 初始化登录

        val webView = WebView(this)
        loginer = WebViewLoginer(webView)
    }

    private fun observeData() {
        // 检查更新
        val localVersion = packageManager.getPackageInfo(packageName, 0).versionName
        mState.latestVersions.observe(this) {
            it.onSuccess {
                if (!it.isSuccessful) {
                    Toast.makeText(this, "检查更新失败：${it.msg}", Toast.LENGTH_SHORT).show()
                    return@onSuccess
                }
                if (it.data.version != localVersion) {
                    Log.i("Update", it.data.version)
                    buildDialog(
                        "更新",
                        "发现新版本！\n新版特性：\n ${it.data.message}",
                        "前往下载", { _, _ ->
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse(it.data.url)
                        }, "取消"
                    ).show()
                    return@onSuccess
                }
                Toast.makeText(this, "已是最新版本！", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this, "检查更新失败：${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        mState.isLogin.observe(this) {
            // 本地登录
            if (!it || AHUCache.getLoginType() != User.UserType.AHU_LOCAL) {
                ReptileManager.getInstance().isLoginStatus = false
                return@observe
            }
            val username = AHUCache.getCurrentUser()?.name ?: return@observe
            val password = AHUCache.getCurrentUserPassword() ?: return@observe
            loginer.login(ReptileUser(username, password)) { status, e ->
                mState.localReptileLoginStatus.value = status
                if (status == SinkWebViewClient.STATUS_LOGIN_SUCCESS) {
                    ReptileManager.getInstance().isLoginStatus = true
                }
                if (SinkWebViewClient.STATUS_LOGIN_FAILURE == status) {
                    Toast.makeText(this, e?.message, Toast.LENGTH_SHORT).show()
                    mState.logout()
                }
            }
        }
    }
}