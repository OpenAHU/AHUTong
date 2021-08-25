package com.ahu.ahutong

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import arch.sink.ui.page.BaseActivity
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.databinding.ActivityMainBinding
import com.ahu.ahutong.ui.page.state.MainViewModel
import com.ahu.ahutong.widget.ClassWidget
import com.sink.library.update.CookApkUpdate
import com.sink.library.update.bean.App

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
        //检查更新
        CookApkUpdate.checkUpdate(object : CookApkUpdate.UpdateListener {
            override fun onNeedUpdate(app: App) {

            }

            override fun onLatestVersion() {

            }

            override fun checkFailure(throwable: Throwable) {

            }
        })
    }
}