package com.ahu.ahutong.ui.screen.settings

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.crawler.manager.CookieManager
import com.ahu.ahutong.data.crawler.manager.TokenManager
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.debug.DebugClock
import com.ahu.ahutong.notification.CourseReminderScheduler
import com.ahu.ahutong.ui.components.LiquidToggle
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.ScheduleViewModel
import com.kyant.backdrop.backdrops.rememberCanvasBackdrop
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun Debug(
    scheduleViewModel: ScheduleViewModel = viewModel()
) {
    val context = LocalContext.current
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") }
    val cardColor = 100.n1 withNight 20.n1
    val subCardColor = 96.n1 withNight 16.n1
    val primaryButtonColor = 90.a1 withNight 85.a1
    val secondaryTextColor = 50.n1 withNight 78.n1
    val backdrop = rememberCanvasBackdrop { drawRect(cardColor) }

    var mockedData by remember { mutableStateOf(AHUCache.getMockData()) }
    var mockTimeEnabled by remember { mutableStateOf(DebugClock.isMocked()) }
    var effectiveTimeText by remember { mutableStateOf(formatter.format(DebugClock.nowLocalDateTime())) }
    var mockTimeInput by remember {
        mutableStateOf(
            AHUCache.getMockCurrentTimeMillis()?.let {
                formatter.format(
                    LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(it),
                        ZoneId.systemDefault()
                    )
                )
            }.orEmpty()
        )
    }

    val applyEffectiveTime: (Long?) -> Unit = { millis ->
        mockTimeEnabled = millis != null
        effectiveTimeText = formatter.format(
            millis?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
            } ?: DebugClock.nowLocalDateTime()
        )
    }

    val shiftMockTime: (Long) -> Unit = { days ->
        val baseMillis = AHUCache.getMockCurrentTimeMillis() ?: DebugClock.nowDate().time
        val shiftedMillis = baseMillis + days * 24L * 60L * 60L * 1000L
        AHUCache.saveMockCurrentTimeMillis(shiftedMillis)
        mockTimeInput = formatter.format(
            LocalDateTime.ofInstant(Instant.ofEpochMilli(shiftedMillis), ZoneId.systemDefault())
        )
        scheduleViewModel.loadConfig()
        applyEffectiveTime(shiftedMillis)
        Toast.makeText(context, "已调整 mock 时间", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .systemBarsPadding()
            .padding(bottom = 80.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Debug",
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "调试网络、时间和缓存状态。当前时间模拟会影响首页、课表和小组件。",
                color = secondaryTextColor,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        DebugSection(
            title = "数据源",
            subtitle = "切换课程和考试等本地 mock 数据",
            cardColor = cardColor
        ) {
            DebugToggleRow(
                title = "使用 mock 数据",
                subtitle = if (mockedData) "当前为模拟数据源" else "当前为真实数据源",
                checked = mockedData,
                backdrop = backdrop,
                onCheckedChange = {
                    mockedData = it
                    AHUCache.setMockData(it)
                    AHURepository.initializeDataSource(it)
                    AHUCache.clearMockCurrentTimeMillis()
                    Toast.makeText(
                        context,
                        if (it) "已开启 mock 数据" else "已关闭 mock 数据",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        DebugSection(
            title = "时间模拟",
            subtitle = "按本地学期起始日期推算当前周",
            cardColor = cardColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SmoothRoundedCornerShape(20.dp))
                    .background(subCardColor)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (mockTimeEnabled) "Mock 当前时间已启用" else "Mock 当前时间未启用",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = effectiveTimeText,
                    color = 20.n1 withNight 90.n1,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = if (mockTimeEnabled) "服务端周次校准已暂停，当前周完全按本地日期推算" else "未启用时仍会按需要走服务端校准",
                    color = secondaryTextColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            OutlinedTextField(
                value = mockTimeInput,
                onValueChange = { mockTimeInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("yyyy-MM-dd HH:mm") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DebugActionButton(
                    text = "-5天",
                    modifier = Modifier.weight(1f),
                    onClick = { shiftMockTime(-5) }
                )
                DebugActionButton(
                    text = "+3天",
                    modifier = Modifier.weight(1f),
                    onClick = { shiftMockTime(3) }
                )
                DebugActionButton(
                    text = "+7天",
                    modifier = Modifier.weight(1f),
                    onClick = { shiftMockTime(7) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DebugActionButton(
                    text = "应用时间",
                    modifier = Modifier.weight(1f),
                    primary = true,
                    containerColor = primaryButtonColor,
                    onClick = {
                        val value = runCatching {
                            LocalDateTime.parse(mockTimeInput, formatter)
                                .atZone(ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli()
                        }.getOrNull()

                        if (value == null) {
                            Toast.makeText(
                                context,
                                "时间格式错误，示例：2026-03-11 14:30",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@DebugActionButton
                        }

                        AHUCache.saveMockCurrentTimeMillis(value)
                        scheduleViewModel.loadConfig()
                        applyEffectiveTime(value)
                        Toast.makeText(context, "已应用 mock 时间", Toast.LENGTH_SHORT).show()
                    }
                )
                DebugActionButton(
                    text = "填入现在",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        mockTimeInput = formatter.format(DebugClock.nowLocalDateTime())
                    }
                )
                DebugActionButton(
                    text = "恢复系统时间",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        AHUCache.clearMockCurrentTimeMillis()
                        scheduleViewModel.loadConfig()
                        applyEffectiveTime(null)
                        Toast.makeText(context, "已恢复系统时间", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        DebugSection(
            title = "维护",
            subtitle = "清理登录和缓存状态",
            cardColor = cardColor
        ) {
            DebugActionRow(
                title = "清除 Adwmh Cookie",
                subtitle = "只清理智慧安大相关 Cookie",
                onClick = {
                    CookieManager.cookieJar.clearCookiesForUrl("https://adwmh.ahu.edu.cn/")
                    Toast.makeText(context, "已清除 Adwmh Cookie", Toast.LENGTH_SHORT).show()
                }
            )
            DebugActionRow(
                title = "清除所有 Cookie 和 Token",
                subtitle = "保留其他本地缓存，重置登录态调试环境",
                onClick = {
                    CookieManager.cookieJar.clear()
                    TokenManager.clear()
                    CookieManager.cookieJar.logAllCookies()
                    Log.e("TAG", "Debug: ${TokenManager.getToken()}")
                    Toast.makeText(context, "已清除所有 Cookie 和 Token", Toast.LENGTH_SHORT).show()
                }
            )
            DebugActionRow(
                title = "清除缓存",
                subtitle = "包含课表、周次、mock 时间和本地持久化数据",
                onClick = {
                    AHUCache.clearAll()
                    applyEffectiveTime(null)
                    mockTimeInput = ""
                    mockedData = false
                    Toast.makeText(context, "已清除缓存", Toast.LENGTH_SHORT).show()
                }
            )
        }

        DebugSection(
            title = "通知测试",
            subtitle = "独立预约测试通知，不影响正式的下一次课前提醒；岛卡测试需先在设置页开启实验开关",
            cardColor = cardColor
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DebugActionButton(
                    text = "10 秒后模拟提醒",
                    modifier = Modifier.weight(1f),
                    primary = true,
                    containerColor = primaryButtonColor,
                    onClick = {
                        CourseReminderScheduler.scheduleDebugReminder(context, 1)
                        Toast.makeText(context, "已预约 10 秒后测试通知", Toast.LENGTH_SHORT).show()
                    }
                )
                DebugActionButton(
                    text = "50 秒后模拟提醒",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        CourseReminderScheduler.scheduleDebugReminder(context, 5)
                        Toast.makeText(context, "已预约 50 秒后测试通知", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DebugActionButton(
                    text = "模拟3分钟岛卡",
                    modifier = Modifier.weight(1f),
                    containerColor = primaryButtonColor,
                    onClick = {
                        val nextCourseName =
                            CourseReminderScheduler.scheduleDebugNextCourseInThreeMinutes(context)
                        if (nextCourseName == null) {
                            Toast.makeText(
                                context,
                                "未找到下一节课，请先刷新课表并确认当前周有课",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "已预约：3分钟后上《$nextCourseName》",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
                DebugActionButton(
                    text = "50 秒后模拟岛卡",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        CourseReminderScheduler.scheduleDebugLiveUpdateReminder(context, 5)
                        Toast.makeText(
                            context,
                            "已预约岛卡测试，50 秒后触发并显示 3 分钟倒计时",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    }
}

@Composable
private fun DebugSection(
    title: String,
    subtitle: String,
    cardColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SmoothRoundedCornerShape(24.dp))
            .background(cardColor)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = subtitle,
                color = 50.n1 withNight 78.n1,
                style = MaterialTheme.typography.bodyMedium
            )
            content()
        }
    )
}

@Composable
private fun DebugToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    backdrop: com.kyant.backdrop.Backdrop,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SmoothRoundedCornerShape(20.dp))
            .background(96.n1 withNight 16.n1)
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = subtitle,
                color = 50.n1 withNight 78.n1,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        LiquidToggle(
            selected = { checked },
            onSelect = onCheckedChange,
            backdrop = backdrop
        )
    }
}

@Composable
private fun DebugActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    containerColor: Color = if (primary) 90.a1 withNight 85.a1 else 96.n1 withNight 16.n1
) {
    Row(
        modifier = modifier
            .clip(SmoothRoundedCornerShape(18.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = if (primary) 0.n1 else Color.Unspecified,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@Composable
private fun DebugActionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SmoothRoundedCornerShape(20.dp))
            .background(96.n1 withNight 16.n1)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = subtitle,
            color = 50.n1 withNight 78.n1,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
