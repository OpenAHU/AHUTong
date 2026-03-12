package com.ahu.ahutong.ui.screen.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.ahu.ahutong.R
import com.ahu.ahutong.notification.CourseReminderScheduler
import com.ahu.ahutong.ui.components.LiquidToggle
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.PreferencesViewModel
import com.kyant.backdrop.backdrops.rememberCanvasBackdrop
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight


@Composable
fun Preferences() {

    val preferencesViewModel: PreferencesViewModel = hiltViewModel()
    val context = LocalContext.current
    var isRequestingPermission by remember { mutableStateOf(false) }

    val showQRCode by preferencesViewModel.showQRCode.collectAsState()
    val isShowAllCourse by preferencesViewModel.isShowAllCourse.collectAsState()
    val useLiquidGlass by preferencesViewModel.useLiquidGlass.collectAsState()
    val courseReminderEnabled by preferencesViewModel.courseReminderEnabled.collectAsState()

    val cardColor = 100.n1 withNight 20.n1
    val backdrop = rememberCanvasBackdrop { drawRect(cardColor) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        isRequestingPermission = false
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
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                if (!isRequestingPermission) {
                                    isRequestingPermission = true
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
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
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                if (!isRequestingPermission) {
                                    isRequestingPermission = true
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
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

        ThemeColorSelector(preferencesViewModel, cardColor)
    }
}

@Composable
fun ThemeColorSelector(
    viewModel: PreferencesViewModel,
    cardColor: androidx.compose.ui.graphics.Color
) {
    val themeColor by viewModel.themeColor.collectAsState()
    var showCustomColorDialog by remember { mutableStateOf(false) }
    var customColorInput by remember { mutableStateOf("") }

    val colors = listOf(
        null to "默认",
        "#FF4A90E2" to "极光蓝",
        "#FFE07A9F" to "樱花粉",
        "#FFF4A261" to "落日橙",
        "#FF5C6BC0" to "靛夜蓝",
        "#FF6A994E" to "苔藓绿",
        "#FF9B7EDE" to "薰衣草紫",
        "#FFD64550" to "绯红花",
        "#FF4CC9F0" to "天空青",
        "#FF2E8B57" to "森林翡翠",
        "#FF6A4C93" to "午夜紫",
        "#FFFF6F61" to "珊瑚粉",
        "#FF7ED9C3" to "北极薄荷"
    )

    val isCustomColor = themeColor != null && colors.none { it.first == themeColor }

    if (showCustomColorDialog) {
        AlertDialog(
            containerColor = cardColor,
            onDismissRequest = { showCustomColorDialog = false },
            title = {
                Text(
                    text = "自定义主题颜色",
                    color = 10.n1 withNight 100.n1,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "请输入ARGB Hex颜色代码 (例如 #FF007FAC)",
                        color = 30.n1 withNight 80.n1,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = customColorInput,
                        onValueChange = { customColorInput = it },
                        label = { Text("Hex Color") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = SmoothRoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = 40.a1 withNight 80.a1,
                            unfocusedBorderColor = 50.n1 withNight 60.n1,
                            focusedLabelColor = 40.a1 withNight 80.a1,
                            unfocusedLabelColor = 50.n1 withNight 60.n1,
                            cursorColor = 40.a1 withNight 80.a1,
                            focusedTextColor = 10.n1 withNight 100.n1,
                            unfocusedTextColor = 10.n1 withNight 100.n1
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            // Validate color parsing
                            android.graphics.Color.parseColor(customColorInput)
                            viewModel.setThemeColor(customColorInput)
                            showCustomColorDialog = false
                        } catch (e: Exception) {
                            // Invalid color, maybe show error or just ignore
                        }
                    }
                ) {
                    Text(
                        text = "确定",
                        color = 40.a1 withNight 80.a1,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomColorDialog = false }) {
                    Text(
                        text = "取消",
                        color = 40.a1 withNight 80.a1,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .clip(SmoothRoundedCornerShape(16.dp))
            .background(cardColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "主题颜色", style = MaterialTheme.typography.headlineSmall)

        // Use FlowRow or LazyRow if there are many colors. For now, a simple wrapped layout or Column is fine.
        // Let's use a FlowRow equivalent or just a simple vertical list of rows if we want to be safe without experimental APIs,
        // or just a Row with horizontal scroll if we expect few items.
        // Given the design, a horizontal scrollable Row seems appropriate for color circles.

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Custom Color Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    customColorInput = if (isCustomColor) themeColor ?: "" else ""
                    showCustomColorDialog = true
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(SmoothRoundedCornerShape(12.dp))
                        .background(
                            if (isCustomColor && themeColor != null) Color(
                                android.graphics.Color.parseColor(
                                    themeColor
                                )
                            ) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCustomColor) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Selected",
                            tint = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Custom",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = "自定义",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            colors.forEach { (colorHex, name) ->
                val isSelected = themeColor == colorHex
                val color = if (colorHex != null) {
                    Color(android.graphics.Color.parseColor(colorHex))
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        colorResource(id = android.R.color.system_accent1_500)
                    } else {
                        Color(0xFF007FAC)
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { viewModel.setThemeColor(colorHex) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(SmoothRoundedCornerShape(12.dp))
                            .background(color),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Selected",
                                tint = Color.White
                            )
                        }
                    }
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) {
                            50.a1
                        } else {
                            Color.Black withNight Color.White
                        }
                    )
                }
            }
        }
    }
}
