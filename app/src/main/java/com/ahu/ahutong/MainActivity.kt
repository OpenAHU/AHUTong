package com.ahu.ahutong

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import androidx.navigation.findNavController
import arch.sink.ui.BarConfig
import arch.sink.ui.page.BaseActivity
import arch.sink.ui.page.DataBindingConfig
import arch.sink.utils.NightUtils
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.databinding.ActivityMainBinding
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

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 监听session过期
        AHUApplication.sessionUpdated.observe(this) {
            // 防止多次的弹出
            if (!AHUCache.isLogin()) return@observe
            // 登录过期
            mState.logout()
            // 重新登录
            MaterialAlertDialogBuilder(this).apply {
                setTitle("提示")
                setMessage("当前登录状态已过期，请重新登录!")
                setCancelable(false)
                setPositiveButton("重新登录") { _, _ ->
                    val navController = findNavController(R.id.fragment_container)
                    // 退出当前界面
                    while (navController.popBackStack()) {
                        Log.d(TAG, "popBackStack")
                    }
                    // 导航到登录
                    navController.navigate(R.id.login_fragment)
                }
            }.show()
        }
    }

    companion object {
        val TAG = MainActivity::class.simpleName!!
    }
}