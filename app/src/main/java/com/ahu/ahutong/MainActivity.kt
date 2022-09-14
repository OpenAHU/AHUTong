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

        // 日活统计接口
        mState.addAppAccess()
    }

    private fun observeData() {

    }

}