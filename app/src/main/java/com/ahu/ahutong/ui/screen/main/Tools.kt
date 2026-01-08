package com.ahu.ahutong.ui.screen.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.navigation.NavHostController
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.os.Build
import android.util.Log
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ahu.ahutong.sdk.RustSDK
import com.ahu.ahutong.R
import com.ahu.ahutong.appwidget.ScheduleAppWidgetReceiver
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.kyant.capsule.ContinuousCapsule
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun Tools(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var showPreview by remember { mutableStateOf(false) }
    var calendarFile by remember { mutableStateOf<File?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    val fetchCalendar = {
        scope.launch(Dispatchers.IO) {
            val tag = "SchoolCalendarFetch"
            
            val cached = RustSDK.getCachedSchoolCalendar(context)
            if (cached != null) {
                withContext(Dispatchers.Main) {
                    calendarFile = cached
                    showPreview = true
                }
                return@launch
            }

            withContext(Dispatchers.Main) {
                isLoading = true
                progress = 0f
            }
            Log.d(tag, "开始获取校历 (IO Thread)...")

            try {
                val file = RustSDK.fetchSchoolCalendar(context) {
                    progress = it
                }

                withContext(Dispatchers.Main) {
                    isLoading = false
                    if (file != null && file.exists()) {
                        calendarFile = file
                        showPreview = true
                    } else {
                        Log.e(tag, "获取失败：SDK 返回 null")
                        Toast.makeText(context, "获取失败，请检查网络连接", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "异常: ${e.message}")
                withContext(Dispatchers.Main) {
                    isLoading = false
                    Toast.makeText(context, "获取异常: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            fetchCalendar()
        } else {
            Toast.makeText(context, "需要存储权限才能保存校历", Toast.LENGTH_SHORT).show()
        }
    }

    if (isLoading) {
        Dialog(onDismissRequest = { }) {
            Surface(
                shape = SmoothRoundedCornerShape(24.dp),
                color = 96.n1 withNight 10.n1,
                shadowElevation = 8.dp,
                tonalElevation = 6.dp,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    if (progress > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                        Text(
                            text = "正在下载 ${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = 10.n1 withNight 96.n1
                        )
                    } else {
                        CircularProgressIndicator()
                        Text(
                            text = "正在获取校历...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = 10.n1 withNight 96.n1
                        )
                    }
                }
            }
        }
    }

    if (showPreview && calendarFile != null) {
        Dialog(
            onDismissRequest = { showPreview = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                var scale by remember { mutableFloatStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(calendarFile)
                        .crossfade(true)
                        .build(),
                    contentDescription = "School Calendar",
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(0.5f, 5f)
                                offset += pan
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    scale = 1f
                                    offset = Offset.Zero
                                }
                            )
                        }
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        ),
                    contentScale = ContentScale.Fit
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(16.dp)
                        .systemBarsPadding(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showPreview = false }) {
                        Text("退出", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        scope.launch(Dispatchers.IO) {
                            RustSDK.saveImageToGallery(context, calendarFile!!)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "已保存到相册", Toast.LENGTH_SHORT).show()
                                showPreview = false
                            }
                        }
                    }) {
                        Text("保存到相册", color = Color.White)
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .systemBarsPadding()
            .padding(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "小工具",
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp, 32.dp),
            style = MaterialTheme.typography.headlineMedium
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ToolItem(
                stringId = R.string.grade,
                iconId = R.drawable.ic_grade,
                tint = Color(0xFFFFC107),
                onClick = { navController.navigate("grade") }
            )
            ToolItem(
                stringId = R.string.phone_book,
                iconId = R.drawable.ic_phonebook,
                tint = Color(0xFF009688),
                onClick = { navController.navigate("phone_book") }
            )
            ToolItem(
                stringId = R.string.exam,
                iconId = R.drawable.ic_exam,
                tint = Color(0xFF4CAF50),
                onClick = { navController.navigate("exam") }
            )
            ToolItem(
                stringId = R.string.school_calendar,
                iconId = R.drawable.ic_schedule,
                tint = Color(0xFF9C27B0),
                onClick = {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    } else {
                        fetchCalendar()
                    }
                }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(SmoothRoundedCornerShape(32.dp))
                .background(100.n1 withNight 30.n1),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "添加桌面课表微件",
                modifier = Modifier.padding(24.dp),
                style = MaterialTheme.typography.titleLarge
            )
            Image(
                painter = painterResource(id = R.mipmap.schedule_widget_prev),
                contentDescription = "桌面课表微件",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = "添加",
                modifier = Modifier
                    .padding(16.dp)
                    .clip(ContinuousCapsule)
                    .background(90.a1)
                    .clickable {
                        scope.launch {
                            GlanceAppWidgetManager(context).requestPinGlanceAppWidget(
                                ScheduleAppWidgetReceiver::class.java
                            )
                        }
                    }
                    .padding(16.dp, 8.dp),
                color = 0.n1,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun ToolItem(
    stringId: Int,
    iconId: Int,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(SmoothRoundedCornerShape(16.dp))
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .padding(16.dp, 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = iconId),
            modifier = Modifier.size(40.dp),
            contentDescription = null,
            tint = tint
        )
        Text(
            text = stringResource(id = stringId),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
