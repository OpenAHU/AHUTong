package com.ahu.ahutong

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import androidx.navigation.findNavController
import arch.sink.ui.BarConfig
import arch.sink.ui.page.BaseActivity
import arch.sink.ui.page.DataBindingConfig
import arch.sink.utils.NightUtils
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.databinding.ActivityMainBinding
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

    override fun loadInitData() {
        super.loadInitData()
        // 日活统计接口
        mState.addAppAccess()
        //更新小部件数据
        val manager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, ClassWidget::class.java)
        val appWidgetIds = manager.getAppWidgetIds(componentName)
        manager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_listview)
    }

    override fun onStart() {
        super.onStart()
        // 不可以在onCreate方法使用 findNavController
        // 根据登录状态进入判断是否进入登录界面
        if (!AHUCache.isLogin()) {
            val navController = findNavController(R.id.fragment_container)
            // 退出当前界面
            navController.popBackStack()
            // 导航到登录
            navController.navigate(R.id.login_fragment)
        }
    }


}