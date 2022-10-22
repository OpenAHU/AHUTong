package com.ahu.ahutong

import android.os.Bundle
import android.view.WindowManager
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
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.ui.screen.Main
import com.ahu.ahutong.ui.state.DiscoveryViewModel
import com.ahu.ahutong.ui.state.LoginViewModel
import com.ahu.ahutong.ui.state.MainViewModel
import com.ahu.ahutong.ui.state.ScheduleViewModel
import com.ahu.ahutong.ui.theme.AHUTheme
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private val discoveryViewModel: DiscoveryViewModel by viewModels()
    private val scheduleViewModel: ScheduleViewModel by viewModels()

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // 日活统计接口
        mainViewModel.addAppAccess()

        initViewModels()

        loginViewModel.serverLoginResult.observe(this) { result ->
            result.onSuccess {
                initViewModels()
            }
        }

        setContent {
            AHUTheme {
                val navController = rememberAnimatedNavController()
                var isReLoginDialogShown by rememberSaveable { mutableStateOf(false) }
                Main(
                    navController = navController,
                    loginViewModel = loginViewModel,
                    discoveryViewModel = discoveryViewModel,
                    scheduleViewModel = scheduleViewModel,
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

    private fun initViewModels() {
        discoveryViewModel.loadActivityBean()
        scheduleViewModel.loadConfig()
        scheduleViewModel.refreshSchedule()
    }
}
