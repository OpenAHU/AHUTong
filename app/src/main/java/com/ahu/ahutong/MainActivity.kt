package com.ahu.ahutong

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.ahu.ahutong.ext.buildProgressDialog
import com.ahu.ahutong.ui.page.state.MainViewModel
import com.ahu.ahutong.widget.ClassWidget

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private lateinit var mState: MainViewModel
    private lateinit var loginer: WebViewLoginer
    private val handler = Handler(Looper.getMainLooper())
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

        // 创建ProgressDialog
        val progressDialog = buildProgressDialog("正在切换本地数据源中！");
        AHUApplication.loginType.addObserver {
            handler.post {
                // 切换本地数据源，打出提示
                if (it == User.UserType.AHU_LOCAL) {
                    val username = AHUCache.getCurrentUser()?.name
                    val password = AHUCache.getWisdomPassword()
                    if (username == null || password.isNullOrBlank()) {
                        buildDialog("温馨提示", "登录状态过期，请重新登录！", "确定", { _, _ ->
                            mState.logout()
                        }).show()
                        return@post
                    }
                    // 打出提示
                    progressDialog.create()

                    // 启动登录
                    loginer.login(ReptileUser(username, password)) { status, e ->
                        // 更新实时登录状态
                        mState.localReptileLoginStatus.value = status
                        // 打印信息
                        Log.e(MainActivity::class.java.name, "登录状态为：${status}, 异常信息：${e}")
                        if (status == SinkWebViewClient.STATUS_LOGIN_SUCCESS) {
                            // 更新全局爬虫登录状态
                            ReptileManager.getInstance().isLoginStatus = true
                            // 关闭 Dialog
                            progressDialog.dismiss()
                        }
                        if (SinkWebViewClient.STATUS_LOGIN_FAILURE == status) {
                            // 关闭Dialog
                            progressDialog.dismiss()
                            buildDialog("登录失败",
                                "登录失败的原因可能是网络问题，建议您重新尝试！如果近期修改过密码，请点击重新登录！",
                                "重试", { _, _ ->
                                    AHUApplication.loginType.setValue(User.UserType.AHU_LOCAL)
                                }, "重新登录", { _, _ ->
                                    mState.logout()
                                }).show()
                        }
                    }
                }

            }

        }
    }

}