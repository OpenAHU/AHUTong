package com.ahu.ahutong

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.sdk.RustSDK
import com.ahu.ahutong.ui.screen.Main
import com.ahu.ahutong.ui.state.AboutViewModel
import com.ahu.ahutong.ui.state.DiscoveryViewModel
import com.ahu.ahutong.ui.state.LoginViewModel
import com.ahu.ahutong.ui.state.MainViewModel
import com.ahu.ahutong.ui.state.ScheduleViewModel
import com.ahu.ahutong.ui.theme.AHUTheme
import dagger.hilt.android.AndroidEntryPoint
import com.ahu.ahutong.ui.component.HotUpdateDialog

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private val discoveryViewModel: DiscoveryViewModel by viewModels()
    private val scheduleViewModel: ScheduleViewModel by viewModels()
    private val aboutViewModel: AboutViewModel by viewModels()

    private var showHotUpdateDialog by mutableStateOf(false)

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        init()

        setContent {
            AHUTheme {
                val navController = rememberNavController()
                var isReLoginDialogShown by rememberSaveable { mutableStateOf(false) }

                if (showHotUpdateDialog) {
                    HotUpdateDialog(
                        onConfirm = {
                            // 调用 SDK 的重启逻辑
                            RustSDK.restartApp(this)
                        }
                    )
                }

                Main(
                    navController = navController,
                    loginViewModel = loginViewModel,
                    discoveryViewModel = discoveryViewModel,
                    scheduleViewModel = scheduleViewModel,
                    aboutViewModel = aboutViewModel,
                    isReLoginShown = isReLoginDialogShown,
                    onReLoginDismiss = { isReLoginDialogShown = false }
                )
            }
        }
    }

    private fun init() {
        RustSDK.loadLibrary(this) {
            showHotUpdateDialog = true
        }

        if (AHUCache.isLogin()) {
            val user = AHUCache.getCurrentUser()
            val pwd = AHUCache.getWisdomPassword()

            discoveryViewModel.loadActivityBean()
            scheduleViewModel.loadConfig()
            scheduleViewModel.refreshSchedule()

            // 启动时自动登录 Rust SDK，以确保 Session 有效（解决覆盖安装或重启后余额不显示问题）
            lifecycleScope.launch(Dispatchers.IO) {
                if (user != null && !pwd.isNullOrEmpty()) {
                    RustSDK.loginSafe(user.name, pwd)
                }
                
                withContext(Dispatchers.Main) {
                    // 登录后再次刷新，确保获取最新数据（如果之前的请求因未登录失败）
                    discoveryViewModel.loadActivityBean()
                    scheduleViewModel.loadConfig()
                    scheduleViewModel.refreshSchedule()
                }
            }
        }
    }
}
