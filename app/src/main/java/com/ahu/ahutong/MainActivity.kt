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
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.ui.page.state.LoginViewModel
import com.ahu.ahutong.ui.page.state.MainViewModel
import com.ahu.ahutong.ui.screen.Login
import com.ahu.ahutong.ui.theme.AHUTheme
import com.ahu.ahutong.utils.animatedComposable
import com.ahu.ahutong.widget.ClassWidget
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

class MainActivity : ComponentActivity() {
    private val mState: MainViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()

    private fun loadInitData() {
        // 日活统计接口
        mState.addAppAccess()
        // 更新小部件数据
        val manager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, ClassWidget::class.java)
        val appWidgetIds = manager.getAppWidgetIds(componentName)
        manager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_listview)
    }

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        loadInitData()
        setContent {
            AHUTheme {
                val navController = rememberAnimatedNavController()
                AnimatedNavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    animatedComposable("home") {}
                    animatedComposable("login") {
                        Login(
                            userId = loginViewModel.userID,
                            onUserIdChanged = { loginViewModel.userID = it },
                            password = loginViewModel.password,
                            onPasswordChanged = { loginViewModel.password = it },
                            onLoginButtonClicked = { login(navController = navController) }
                        )
                    }
                }
                if (!AHUCache.isLogin()) {
                    // 退出当前界面
                    navController.popBackStack()
                    // 导航到登录
                    navController.navigate("login")
                }
            }
        }
    }

    private fun login(navController: NavHostController) {
        if (loginViewModel.userID.text.isBlank() || loginViewModel.password.text.isBlank()) {
            Toast.makeText(
                this,
                "请不要输入空气哦！",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            loginViewModel.loginWithServer(
                userID = loginViewModel.userID.text,
                wisdomPassword = loginViewModel.password.text
            )
        }
        loginViewModel.serverLoginResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(
                    this,
                    "登录成功，欢迎您：${it.name}",
                    Toast.LENGTH_SHORT
                ).show()
                navController.popBackStack()
                navController.navigate("home")
            }.onFailure {
                Toast.makeText(
                    this,
                    it.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
