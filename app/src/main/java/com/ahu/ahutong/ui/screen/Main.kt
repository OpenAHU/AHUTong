package com.ahu.ahutong.ui.screen

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ahu.ahutong.BuildConfig
import com.ahu.ahutong.ui.screen.main.BathroomDeposit
import com.ahu.ahutong.ui.screen.main.CardBalanceDeposit
import com.ahu.ahutong.ui.screen.main.ElectricityDeposit
//import com.ahu.ahutong.appwidget.ScheduleAppWidgetReceiver
import com.ahu.ahutong.ui.screen.main.Exam
import com.ahu.ahutong.ui.screen.main.Grade
import com.ahu.ahutong.ui.screen.main.Home
import com.ahu.ahutong.ui.screen.main.PhoneBook
import com.ahu.ahutong.ui.screen.main.Schedule
import com.ahu.ahutong.ui.screen.main.Tools
import com.ahu.ahutong.ui.screen.settings.Contributors
import com.ahu.ahutong.ui.screen.settings.Debug
import com.ahu.ahutong.ui.screen.settings.License
import com.ahu.ahutong.ui.screen.settings.Preferences
import com.ahu.ahutong.ui.screen.setup.Info
import com.ahu.ahutong.ui.screen.setup.Login
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.AboutViewModel
import com.ahu.ahutong.ui.state.DiscoveryViewModel
import com.ahu.ahutong.ui.state.LoginViewModel
import com.ahu.ahutong.ui.state.MainViewModel
import com.ahu.ahutong.ui.state.ScheduleViewModel
import com.ahu.ahutong.utils.animatedComposable
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight
import kotlinx.coroutines.delay
import kotlinx.coroutines.selects.select

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Main(
    navController: NavHostController,
    mainViewModel: MainViewModel = viewModel(),
    loginViewModel: LoginViewModel = viewModel(),
    discoveryViewModel: DiscoveryViewModel = viewModel(),
    scheduleViewModel: ScheduleViewModel = viewModel(),
    aboutViewModel: AboutViewModel = viewModel(),
    isReLoginShown: Boolean,
    onReLoginDismiss: () -> Unit
) {
    val context = LocalContext.current
    Box {
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier
                .fillMaxSize()
                .background(96.n1 withNight 10.n1)
        ) {
            animatedComposable("home") {
                Home(
                    discoveryViewModel = discoveryViewModel,
                    scheduleViewModel = scheduleViewModel,
                    navController = navController
                )
            }
            animatedComposable("setup") {
                Setup(
                    loginViewModel = loginViewModel,
                    scheduleViewModel = scheduleViewModel,
                    aboutViewModel = aboutViewModel,
                    onSetup = {
                        navController.popBackStack()
                        discoveryViewModel.loadActivityBean()
                        scheduleViewModel.loadConfig()
                        scheduleViewModel.refreshSchedule(isRefresh = true)
//                        GlanceAppWidgetManager(context).requestPinGlanceAppWidget(
//                            ScheduleAppWidgetReceiver::class.java
//                        )
                    }
                )
            }
            animatedComposable("login") {
                Login(
                    loginViewModel = loginViewModel,
                    onLoggedIn = {
                        navController.navigate("home"){
                            popUpTo("login") { inclusive = true }
                        }
                        discoveryViewModel.loadActivityBean()
                        scheduleViewModel.loadConfig()
                        scheduleViewModel.refreshSchedule(isRefresh = true)
                    }
                )
            }
            animatedComposable("info") {
                Info(
                    scheduleViewModel = scheduleViewModel,
                    onSetup = { navController.popBackStack() }
                )
            }
            animatedComposable("schedule") {
                Schedule(scheduleViewModel = scheduleViewModel)
            }
            animatedComposable("tools") {
                Tools(navController = navController)
            }
            animatedComposable("grade") {
                Grade()
            }
            animatedComposable("phone_book") {
                PhoneBook()
            }
            animatedComposable("exam") {
                Exam()
            }
            animatedComposable("settings") {
                Settings(
                    navController = navController,
                    mainViewModel = mainViewModel,
                    aboutViewModel = aboutViewModel
                )
            }
            animatedComposable("settings__license") {
                License()
            }
            animatedComposable("settings__contributors") {
                Contributors()
            }

            animatedComposable("electricity_pay"){
                ElectricityDeposit()
            }

            animatedComposable("card_balance_deposit") {
                CardBalanceDeposit()
            }

            animatedComposable("preferences"){
                Preferences()
            }

            animatedComposable("bathroom_deposit"){
                BathroomDeposit()
            }

            animatedComposable("debug"){
                Debug()
            }

            animatedComposable("splash"){
                Splash(navController)
            }


        }
        BottomNavBar(navController = navController)
    }
    if (isReLoginShown) {
        Dialog(
            onDismissRequest = { onReLoginDismiss() },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Column(
                modifier = Modifier
                    .clip(SmoothRoundedCornerShape(32.dp))
                    .background(96.n1 withNight 10.n1)
                    .padding(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "当前登录状态已过期，请重新登录!",
                    modifier = Modifier.padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "重新登录",
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(CircleShape)
                        .background(90.a1 withNight 30.n1)
                        .clickable {
                            navController.navigate("login")
                            onReLoginDismiss()
                        }
                        .padding(12.dp, 8.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun BoxScope.BottomNavBar(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val visible = currentRoute in arrayOf("home", "schedule", "tools")
    val visibilities = remember { mutableStateListOf<Int>() }
    LaunchedEffect(visible) {
        if (visible) {
            repeat(3 - visibilities.size) {
                delay(100)
                visibilities += visibilities.lastIndex + 1
            }
        } else {
            repeat(visibilities.size) {
//                visibilities.removeLast()
                visibilities.removeAt(visibilities.size - 1)
                delay(50)
            }
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .background(95.a1 withNight 30.n1)
            .height(
                animateDpAsState(
                    targetValue = if (visible) {
                        WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding() + 80.dp
                    } else {
                        0.dp
                    },
                    animationSpec = if (visible) {
                        spring()
                    } else {
                        tween(
                            durationMillis = 200,
                            delayMillis = 150
                        )
                    }
                ).value
            )
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(80.dp)
        ) {
            this@Row.AnimatedVisibility(
                visible = 0 in visibilities,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                this@Row.NavigationBarItem(
                    selected = currentRoute == "home",
                    onClick = { navController.navigatePreservingHome("home") },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Home,
                            contentDescription = null
                        )
                    },
                    label = { Text(text = "主页") },
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = 0.n1,
                        selectedTextColor = LocalContentColor.current,
                        indicatorColor = 90.a1,
                        unselectedIconColor = LocalContentColor.current,
                        unselectedTextColor = LocalContentColor.current
                    )
                )
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(80.dp)
        ) {
            this@Row.AnimatedVisibility(
                visible = 1 in visibilities,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                this@Row.NavigationBarItem(
                    selected = currentRoute == "schedule",
                    onClick = {
                        navController.navigatePreservingHome("schedule")
                    },

                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.TableChart,
                            contentDescription = null
                        )
                    },
                    label = { Text(text = "课表") },
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = 0.n1,
                        selectedTextColor = LocalContentColor.current,
                        indicatorColor = 90.a1,
                        unselectedIconColor = LocalContentColor.current,
                        unselectedTextColor = LocalContentColor.current
                    )
                )
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(80.dp)
        ) {
            this@Row.AnimatedVisibility(
                visible = 2 in visibilities,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                this@Row.NavigationBarItem(
                    selected = currentRoute == "tools",
                    onClick = {
                        navController.navigatePreservingHome("tools")
                              },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Build,
                            contentDescription = null
                        )
                    },
                    label = { Text(text = "小工具") },
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = 0.n1,
                        selectedTextColor = LocalContentColor.current,
                        indicatorColor = 90.a1,
                        unselectedIconColor = LocalContentColor.current,
                        unselectedTextColor = LocalContentColor.current
                    )
                )
            }
        }
    }
}
fun NavController.navigatePreservingHome(route: String) {
    val currentRoute = this.currentBackStackEntry?.destination?.route
    if (currentRoute == route) return

    val homeRoute = "home"

    Log.e("TAG", "navigatePreservingHome: $homeRoute", )

    this.navigate(route) {
        popUpTo(homeRoute) {
            inclusive = false
        }
        launchSingleTop = true
    }
}