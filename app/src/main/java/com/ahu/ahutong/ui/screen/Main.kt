package com.ahu.ahutong.ui.screen

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.ahu.ahutong.appwidget.ScheduleAppWidgetReceiver
import com.ahu.ahutong.ui.screen.main.BathroomDeposit
import com.ahu.ahutong.ui.screen.main.CardBalanceDeposit
import com.ahu.ahutong.ui.screen.main.ElectricityDeposit
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
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.capsule.ContinuousCapsule
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight
import kotlinx.coroutines.launch

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
    Box {
        val backdrop = rememberLayerBackdrop()
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier
                .layerBackdrop(backdrop)
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
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                Setup(
                    scheduleViewModel = scheduleViewModel,
                    aboutViewModel = aboutViewModel,
                    onSetup = {
                        navController.popBackStack()
                        discoveryViewModel.loadActivityBean()
                        scheduleViewModel.loadConfig()
                        scheduleViewModel.refreshSchedule()
                        scope.launch {
                            GlanceAppWidgetManager(context).requestPinGlanceAppWidget(
                                ScheduleAppWidgetReceiver::class.java
                            )
                        }
                    }
                )
            }
            animatedComposable("login") {
                Login(
                    loginViewModel = loginViewModel,
                    onLoggedIn = {
                        scheduleViewModel.clear()
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                        discoveryViewModel.loadActivityBean()
                        scheduleViewModel.loadConfig()
                        scheduleViewModel.refreshSchedule()
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

            animatedComposable("preferences") {
                Preferences()
            }

            animatedComposable("electricity_pay") {
                ElectricityDeposit()
            }

            animatedComposable("card_balance_deposit") {
                CardBalanceDeposit()
            }

            animatedComposable("bathroom_deposit") {
                BathroomDeposit()
            }

            animatedComposable("debug") {
                Debug()
            }

            animatedComposable("splash") {
                Splash(navController)
            }


        }
        BottomNavBar(navController, backdrop)
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
                        .clip(ContinuousCapsule)
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
