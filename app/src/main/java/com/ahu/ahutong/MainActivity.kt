package com.ahu.ahutong

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.rememberNavController
import arch.sink.utils.Utils
import com.ahu.ahutong.data.api.AHUCookieJar
//import com.ahu.ahutong.appwidget.ScheduleAppWidgetReceiver
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.ui.screen.Main
import com.ahu.ahutong.ui.state.AboutViewModel
import com.ahu.ahutong.ui.state.DiscoveryViewModel
import com.ahu.ahutong.ui.state.LoginViewModel
import com.ahu.ahutong.ui.state.MainViewModel
import com.ahu.ahutong.ui.state.ScheduleViewModel
import com.ahu.ahutong.ui.theme.AHUTheme
import com.ahu.ahutong.widget.ClassWidget
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private val discoveryViewModel: DiscoveryViewModel by viewModels()
    private val scheduleViewModel: ScheduleViewModel by viewModels()
    private val aboutViewModel: AboutViewModel by viewModels()

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // 日活统计接口
//        mainViewModel.addAppAccess()


        init()

//        loginViewModel.serverLoginResult.observe(this) { result ->
//            result.onSuccess {
//                init(refreshSchedule = true)
//            }
//        }

        setContent {
            AHUTheme {
                val navController = rememberNavController()
                var isReLoginDialogShown by rememberSaveable { mutableStateOf(false) }
                Main(
                    navController = navController,
                    loginViewModel = loginViewModel,
                    discoveryViewModel = discoveryViewModel,
                    scheduleViewModel = scheduleViewModel,
                    aboutViewModel = aboutViewModel,
                    isReLoginShown = isReLoginDialogShown,
                    onReLoginDismiss = { isReLoginDialogShown = false }
                )
                LaunchedEffect(Unit) {
                    if (!AHUCache.isLogin()) {
                        navController.navigate("setup")
                    }
                }
                LaunchedEffect(Unit) {
                    AHUApplication.sessionUpdated.observe(this@MainActivity) {
                        // 防止多次的弹出
                        if (AHUCache.isLogin()) {
                            // 登录过期
                            mainViewModel.logout()
                            // 重新登录
                            isReLoginDialogShown = true
                        }
                    }
                }
            }
        }
    }

    private fun init(refreshSchedule: Boolean = false) {
        discoveryViewModel.loadActivityBean()
        scheduleViewModel.loadConfig()
        scheduleViewModel.refreshSchedule(isRefresh = refreshSchedule)
        // 更新小部件数据
        val manager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, ClassWidget::class.java)
        val appWidgetIds = manager.getAppWidgetIds(componentName)
        manager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_listview)
//        mainViewModel.viewModelScope.launch {
//            ScheduleAppWidgetReceiver().glanceAppWidget.updateAll(this@MainActivity)
//        }
    }
}
