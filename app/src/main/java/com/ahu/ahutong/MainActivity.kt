package com.ahu.ahutong

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.ui.page.state.AboutViewModel
import com.ahu.ahutong.ui.page.state.BusinessViewModel
import com.ahu.ahutong.ui.page.state.DeveloperViewModel
import com.ahu.ahutong.ui.page.state.DiscoveryViewModel
import com.ahu.ahutong.ui.page.state.ExamViewModel
import com.ahu.ahutong.ui.page.state.GradeViewModel
import com.ahu.ahutong.ui.page.state.LicenseViewModel
import com.ahu.ahutong.ui.page.state.LoginViewModel
import com.ahu.ahutong.ui.page.state.MainViewModel
import com.ahu.ahutong.ui.page.state.ScheduleViewModel
import com.ahu.ahutong.ui.screen.Exam
import com.ahu.ahutong.ui.screen.FillInInfo
import com.ahu.ahutong.ui.screen.Grade
import com.ahu.ahutong.ui.screen.Home
import com.ahu.ahutong.ui.screen.LoggingIn
import com.ahu.ahutong.ui.screen.Login
import com.ahu.ahutong.ui.screen.PhoneBook
import com.ahu.ahutong.ui.screen.Schedule
import com.ahu.ahutong.ui.screen.Settings
import com.ahu.ahutong.ui.screen.settings.Contributors
import com.ahu.ahutong.ui.screen.settings.License
import com.ahu.ahutong.ui.theme.AHUTheme
import com.ahu.ahutong.utils.animatedComposable
import com.ahu.ahutong.widget.ClassWidget
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private val discoveryViewModel: DiscoveryViewModel by viewModels()
    private val scheduleViewModel: ScheduleViewModel by viewModels()
    private val gradeViewModel: GradeViewModel by viewModels()
    private val examViewModel: ExamViewModel by viewModels()
    private val aboutViewModel: AboutViewModel by viewModels()
    private val licenseViewModel: LicenseViewModel by viewModels()
    private val developerViewModel: DeveloperViewModel by viewModels()
    private val businessViewModel: BusinessViewModel by viewModels()

    // TODO: refresh after logging in
    private fun initViewModels() {
        discoveryViewModel.loadActivityBean()
        scheduleViewModel.loadConfig()
        scheduleViewModel.refreshSchedule()
        if (AHUCache.getCurrentUser() != null) {
            gradeViewModel.getGarde()
        }
    }

    // TODO: fix widget
    private fun loadInitData() {
        // 日活统计接口
        mainViewModel.addAppAccess()
        // 更新小部件数据
        val manager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, ClassWidget::class.java)
        val appWidgetIds = manager.getAppWidgetIds(componentName)
        manager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_listview)

        initViewModels()
    }

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadInitData()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        setContent {
            AHUTheme {
                val navController = rememberAnimatedNavController()
                AnimatedNavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    animatedComposable("home") {
                        Home(
                            discoveryViewModel = discoveryViewModel,
                            scheduleViewModel = scheduleViewModel,
                            navController = navController
                        )
                    }
                    animatedComposable("login") {
                        Login(
                            loginViewModel = loginViewModel,
                            navController = navController
                        )
                    }
                    animatedComposable("logging_in") { LoggingIn() }
                    animatedComposable("fill_in_info") {
                        FillInInfo(
                            scheduleViewModel = scheduleViewModel,
                            navController = navController
                        )
                    }
                    animatedComposable("schedule") {
                        Schedule(scheduleViewModel = scheduleViewModel)
                    }
                    animatedComposable("grade") {
                        Grade(gradeViewModel = gradeViewModel)
                    }
                    animatedComposable("phone_book") { PhoneBook() }
                    animatedComposable("exam") {
                        Exam(examViewModel = examViewModel)
                    }
                    animatedComposable("settings") {
                        Settings(
                            aboutViewModel = aboutViewModel,
                            navController = navController
                        )
                    }
                    animatedComposable("settings__license") {
                        License(licenseViewModel = licenseViewModel)
                    }
                    animatedComposable("settings__contributors") {
                        Contributors(
                            developerViewModel = developerViewModel,
                            businessViewModel = businessViewModel
                        )
                    }
                }
                LaunchedEffect(loginViewModel.isLoggingIn) {
                    if (loginViewModel.isLoggingIn) {
                        navController.navigate("logging_in")
                    } else if (!AHUCache.isLogin()) {
                        navController.navigate("login")
                    }
                }
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                // TODO
                BackHandler(enabled = currentRoute == "login") {
                    finish()
                }
            }
        }
    }
}
