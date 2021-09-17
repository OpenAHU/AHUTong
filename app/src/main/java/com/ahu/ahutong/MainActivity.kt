package com.ahu.ahutong

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import arch.sink.ui.page.BaseActivity
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.databinding.ActivityMainBinding
import com.ahu.ahutong.ext.buildDialog
import com.ahu.ahutong.ui.page.state.MainViewModel
import com.ahu.ahutong.widget.ClassWidget
import com.simon.library.AppUpdate
import java.lang.Exception

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private lateinit var mState: MainViewModel

    override fun initViewModel() {
        mState = getActivityScopeViewModel(MainViewModel::class.java);
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_main, BR.state, mState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //更新小部件数据
        val manager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, ClassWidget::class.java)
        val appWidgetIds = manager.getAppWidgetIds(componentName)
        manager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_listview)
        AppUpdate.check(
            AHUApplication.version,
            object : AppUpdate.CallBack {
                override fun appUpdate(url: String?, msg: String?) {
                    val message = "发现新版本！\n" +
                            "新版特性：\n $msg"
                    Looper.prepare()
                    buildDialog("更新", message, "前往下载", { _, _ ->
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        startActivity(intent)
                    }, "取消").show()
                    Looper.loop()
                }

                override fun requestError(e: Exception?) {
                }

                override fun onLatestVersion() {
                }

            })
    }


}