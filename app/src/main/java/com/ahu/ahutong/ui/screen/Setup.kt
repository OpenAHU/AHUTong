package com.ahu.ahutong.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.ui.screen.setup.Info
import com.ahu.ahutong.ui.screen.setup.Login
import com.ahu.ahutong.ui.screen.setup.Splash
import com.ahu.ahutong.ui.state.AboutViewModel
import com.ahu.ahutong.ui.state.LoginViewModel
import com.ahu.ahutong.ui.state.ScheduleViewModel
import com.ahu.ahutong.utils.animatedComposable

import com.kyant.monet.n1
import com.kyant.monet.withNight
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Setup(
    loginViewModel: LoginViewModel = viewModel(),
    scheduleViewModel: ScheduleViewModel = viewModel(),
    aboutViewModel: AboutViewModel = viewModel(),
    onSetup: () -> Unit
) {
    val navController = rememberNavController()
    // 用户从老版本升级到 1.0.0-beta6 或更新版本时清空缓存
    LaunchedEffect(Unit) {
        aboutViewModel.versionName?.let {
            if (it < "1.0.0-beta6") {
                AHUCache.clearAll()
            }
        }
    }
    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = Modifier
            .fillMaxSize()
            .background(96.n1 withNight 10.n1)
    ) {
        animatedComposable("splash") {
            Splash()
        }
        animatedComposable("login") {
            Login(
                loginViewModel = loginViewModel,
                onLoggedIn = {
//                    navController.navigate("info")
                    onSetup()
                }
            )
        }
        animatedComposable("info") {
            Info(
                scheduleViewModel = scheduleViewModel,
                onSetup = onSetup
            )
        }
    }
    LaunchedEffect(Unit) {
        navController.navigate("splash") // to fix a bug
        delay(100)
        navController.popBackStack()
        navController.navigate("login")
    }
    // intercept back key if user has NOT logged in
    BackHandler(enabled = !AHUCache.isLogin()) {}
}
