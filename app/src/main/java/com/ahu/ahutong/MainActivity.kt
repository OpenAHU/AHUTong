package com.ahu.ahutong

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.navigation.findNavController
import arch.sink.ui.BarConfig
import arch.sink.ui.page.BaseActivity
import arch.sink.ui.page.DataBindingConfig
import arch.sink.utils.NightUtils
import com.ahu.ahutong.data.reptile.WebViewLoginer
import com.ahu.ahutong.databinding.ActivityMainBinding
import com.ahu.ahutong.ext.buildProgressDialog
import com.ahu.ahutong.ui.page.state.MainViewModel
import com.ahu.ahutong.widget.ClassWidget
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private lateinit var mState: MainViewModel
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
                    MaterialAlertDialogBuilder(this).apply {
                        setTitle("更新")
                        setMessage("发现新版本！\n新版特性：\n ${it.data.message}")
                        setPositiveButton("前往下载") { _, _ ->
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse(it.data.url)
                            startActivity(intent)
                        }
                        setNegativeButton("取消", null)
                    }.show()
                    return@onSuccess
                }
                Toast.makeText(this, "已是最新版本！", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this, "检查更新失败：${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        val loginDialog = buildProgressDialog("正在登录...")
        AHUApplication.retryLogin.observe(this) {
            handler.post {
                // 显示dialog
                MaterialAlertDialogBuilder(this).apply {
                    setTitle("温馨提示")
                    setMessage("登录状态过期，如果您未修改密码请点击重新验证，如果密码已经被修改，请点击前往登录。")
                    setPositiveButton("重新验证") { _, _ ->
                        mState.retryLogin()
                        loginDialog.createAndShow()
                    }
                    setNegativeButton("前往登录") { _, _ ->
                        findNavController(R.id.fragment_container)
                            .navigate(R.id.action_home_fragment_to_login_fragment)
                        mState.logout()
                    }
                    setCancelable(false)
                }.show()
            }
        }

        mState.retryLoginResult.observe(this) {
            it.onSuccess {
                Toast.makeText(this, "重新登录成功！", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this, "重新登录失败：${it.message}", Toast.LENGTH_SHORT).show()
                findNavController(R.id.fragment_container)
                    .navigate(R.id.action_home_fragment_to_login_fragment)
                mState.logout()
            }
            loginDialog.dismiss()
        }
    }

}