package com.ahu.ahutong.ui.screen.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.ahu.ahutong.R
import com.ahu.ahutong.notification.CourseReminderScheduler
import com.ahu.ahutong.ui.components.LiquidToggle
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.PreferencesViewModel
import com.kyant.backdrop.backdrops.rememberCanvasBackdrop
import com.kyant.monet.n1
import com.kyant.monet.withNight


@Composable
fun Preferences() {

    val preferencesViewModel: PreferencesViewModel = hiltViewModel()
    val context = LocalContext.current

    val showQRCode by preferencesViewModel.showQRCode.collectAsState()
    val isShowAllCourse by preferencesViewModel.isShowAllCourse.collectAsState()
    val useLiquidGlass by preferencesViewModel.useLiquidGlass.collectAsState()
    val courseReminderEnabled by preferencesViewModel.courseReminderEnabled.collectAsState()

    val cardColor = 100.n1 withNight 20.n1
    val backdrop = rememberCanvasBackdrop { drawRect(cardColor) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            preferencesViewModel.setCourseReminderEnabled(true)
            CourseReminderScheduler.reschedule(context)
        } else {
            preferencesViewModel.setCourseReminderEnabled(false)
            Toast.makeText(context, "未授予通知权限，无法开启课前提醒", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
            .systemBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.preferences),
            modifier = Modifier.padding(24.dp, 32.dp),
            style = MaterialTheme.typography.headlineLarge
        )
        Column(
            modifier =
                Modifier
                    .clip(SmoothRoundedCornerShape(16.dp))
                    .background(cardColor)
                    .clickable { preferencesViewModel.setShowQRCode(!preferencesViewModel.showQRCode.value) }
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "主页", style = MaterialTheme.typography.headlineSmall)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SmoothRoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "主页默认显示支付二维码")
                LiquidToggle(
                    selected = { showQRCode },
                    onSelect = { preferencesViewModel.setShowQRCode(!preferencesViewModel.showQRCode.value) },
                    backdrop = backdrop
                )
            }
        }

        Column(
            modifier =
                Modifier
                    .clip(SmoothRoundedCornerShape(16.dp))
                    .background(cardColor)
                    .clickable {
                        if (!courseReminderEnabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                            ) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                preferencesViewModel.setCourseReminderEnabled(true)
                                CourseReminderScheduler.reschedule(context)
                            }
                        } else {
                            preferencesViewModel.setCourseReminderEnabled(false)
                            CourseReminderScheduler.cancel(context)
                        }
                    }
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "通知", style = MaterialTheme.typography.headlineSmall)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SmoothRoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "课前提醒")
                    Text(
                        text = "上课前 10 分钟提醒下一节课",
                        color = 50.n1 withNight 80.n1,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                LiquidToggle(
                    selected = { courseReminderEnabled },
                    onSelect = { enabled ->
                        if (enabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                            ) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                preferencesViewModel.setCourseReminderEnabled(true)
                                CourseReminderScheduler.reschedule(context)
                            }
                        } else {
                            preferencesViewModel.setCourseReminderEnabled(false)
                            CourseReminderScheduler.cancel(context)
                        }
                    },
                    backdrop = backdrop
                )
            }
        }

        Column(
            modifier =
                Modifier
                    .clip(SmoothRoundedCornerShape(16.dp))
                    .background(cardColor)
                    .clickable { preferencesViewModel.setIsShowAllCourse(!preferencesViewModel.isShowAllCourse.value) }
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "课表", style = MaterialTheme.typography.headlineSmall)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SmoothRoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "显示非本周课程")
                LiquidToggle(
                    selected = { isShowAllCourse },
                    onSelect = { preferencesViewModel.setIsShowAllCourse(!preferencesViewModel.isShowAllCourse.value) },
                    backdrop = backdrop
                )
            }
        }

        Column(
            modifier =
                Modifier
                    .clip(SmoothRoundedCornerShape(16.dp))
                    .background(cardColor)
                    .clickable { preferencesViewModel.setUseLiquidGlass(!preferencesViewModel.useLiquidGlass.value) }
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "液态玻璃", style = MaterialTheme.typography.headlineSmall)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SmoothRoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "启用液态玻璃效果")
                LiquidToggle(
                    selected = { useLiquidGlass },
                    onSelect = { preferencesViewModel.setUseLiquidGlass(!preferencesViewModel.useLiquidGlass.value) },
                    backdrop = backdrop
                )
            }
        }
    }
}
