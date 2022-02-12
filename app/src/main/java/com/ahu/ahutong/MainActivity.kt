package com.ahu.ahutong

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import arch.sink.ui.BarConfig
import arch.sink.ui.page.BaseActivity
import arch.sink.ui.page.DataBindingConfig
import arch.sink.utils.NightUtils
import com.ahu.ahutong.databinding.ActivityMainBinding
import com.ahu.ahutong.ext.buildDialog
import com.ahu.ahutong.ui.page.state.MainViewModel
import com.ahu.ahutong.widget.ClassWidget

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private lateinit var mState: MainViewModel

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
        
        mState.getAppLatestVersion()
    }

    fun observeData() {
        val localVersion = packageManager.getPackageInfo(packageName, 0).versionName
        mState.laestVserion.observe(this) {
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
    }
}