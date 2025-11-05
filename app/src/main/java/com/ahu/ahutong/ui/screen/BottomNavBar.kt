package com.ahu.ahutong.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ahu.ahutong.ui.components.LiquidBottomTab
import com.ahu.ahutong.ui.components.LiquidBottomTabs
import com.kyant.backdrop.Backdrop

@Composable
fun BoxScope.BottomNavBar(
    navController: NavHostController,
    backdrop: Backdrop
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val selectedTabIndex by rememberUpdatedState(
        when (currentRoute) {
            "home", "electricity_pay", "card_balance_deposit", "bathroom_deposit" -> 0
            "schedule", "info" -> 1
            "tools", "grade", "phone_book", "exam" -> 2
            "settings", "settings__license", "settings__contributors", "preferences" -> 3
            else -> 0
        }
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(vertical = 16f.dp)
            .navigationBarsPadding()
    ) {
        LiquidBottomTabs(
            selectedTabIndex = { selectedTabIndex },
            onTabSelected = {
                navController.navigatePreservingHome(
                    when (it) {
                        0 -> "home"
                        1 -> "schedule"
                        2 -> "tools"
                        3 -> "settings"
                        else -> "home"
                    }
                )
            },
            backdrop = backdrop,
            tabsCount = 4,
            modifier = Modifier.padding(horizontal = 36f.dp)
        ) {
            LiquidBottomTab({ navController.navigatePreservingHome("home") }) {
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = null
                )
                Text(text = "主页", style = MaterialTheme.typography.labelMedium)
            }
            LiquidBottomTab({ navController.navigatePreservingHome("schedule") }) {
                Icon(
                    imageVector = Icons.Outlined.TableChart,
                    contentDescription = null
                )
                Text(text = "课表", style = MaterialTheme.typography.labelMedium)
            }
            LiquidBottomTab({ navController.navigatePreservingHome("tools") }) {
                Icon(
                    imageVector = Icons.Outlined.Build,
                    contentDescription = null
                )
                Text(text = "小工具", style = MaterialTheme.typography.labelMedium)
            }
            LiquidBottomTab({ navController.navigatePreservingHome("settings") }) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null
                )
                Text(text = "设置", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

private fun NavController.navigatePreservingHome(route: String) {
    val currentRoute = this.currentBackStackEntry?.destination?.route
    if (currentRoute == route) return

    val homeRoute = "home"

    Log.e("TAG", "navigatePreservingHome: $homeRoute")

    this.navigate(route) {
        popUpTo(homeRoute) {
            inclusive = false
        }
        launchSingleTop = true
    }
}
