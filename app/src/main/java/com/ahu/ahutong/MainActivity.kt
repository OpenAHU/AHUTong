package com.ahu.ahutong

//import com.ahu.ahutong.appwidget.ScheduleAppWidgetReceiver
import android.appwidget.AppWidgetManager
import android.content.ComponentName
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
import com.ahu.ahutong.ui.screen.Main
import com.ahu.ahutong.ui.state.AboutViewModel
import com.ahu.ahutong.ui.state.DiscoveryViewModel
import com.ahu.ahutong.ui.state.LoginViewModel
import com.ahu.ahutong.ui.state.MainViewModel
import com.ahu.ahutong.ui.state.ScheduleViewModel
import com.ahu.ahutong.ui.theme.AHUTheme
import com.ahu.ahutong.widget.ClassWidget
import dagger.hilt.android.AndroidEntryPoint

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
        enableEdgeToEdge()
        init()

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
            }
        }
    }

    private fun init() {
        if (AHUCache.isLogin()) {
            discoveryViewModel.loadActivityBean()
            scheduleViewModel.loadConfig()
            scheduleViewModel.refreshSchedule()
            // 更新小部件数据
            val manager = AppWidgetManager.getInstance(this)
            val componentName = ComponentName(this, ClassWidget::class.java)
            val appWidgetIds = manager.getAppWidgetIds(componentName)
            manager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_listview)
        }
    }
}
