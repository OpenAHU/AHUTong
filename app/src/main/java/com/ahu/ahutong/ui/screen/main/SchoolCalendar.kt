package com.ahu.ahutong.ui.screen.main

import android.Manifest
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ahu.ahutong.R
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.utils.FileUtils
import com.kyant.capsule.ContinuousCapsule
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun SchoolCalendar(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showPreview by remember { mutableStateOf(false) }
    var calendarFile by remember { mutableStateOf<File?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    val fetchCalendar = {
        scope.launch(Dispatchers.IO) {
            val tag = "SchoolCalendarFetch"
            withContext(Dispatchers.Main) {
                isLoading = true
                progress = 0f
            }
            Log.d(tag, "开始获取校历 (IO Thread)...")

            try {
                val res = AHURepository.getSchoolCalendar()
                val file = if (res.isSuccessful) {
                    val resp = res.data
                    if (resp != null && resp.isSuccessful) {
                        val body = resp.body()
                        if (body != null) {
                            FileUtils.saveResponseBodyToFile(context, body, "xiaoli.jpg") {
                                progress = it
                            }
                        } else null
                    } else null
                } else null

                withContext(Dispatchers.Main) {
                    isLoading = false
                    if (file != null && file.exists()) {
                        calendarFile = file
                        showPreview = true
                    } else {
                        Toast.makeText(context, "获取失败，请检查网络连接", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    Toast.makeText(context, "获取异常: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val savePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted && calendarFile != null) {
            scope.launch(Dispatchers.IO) {
                FileUtils.saveImageToGallery(context, calendarFile!!)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "已保存到相册", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            }
        } else {
            Toast.makeText(context, "需要存储权限才能保存校历", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        fetchCalendar()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding()
    ) {
        if (calendarFile != null) {
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
                    .align(Alignment.BottomEnd)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        savePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    } else {
                        scope.launch(Dispatchers.IO) {
                            FileUtils.saveImageToGallery(context, calendarFile!!)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "已保存到相册", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        }
                    }
                }) {
                    Text("保存", color = Color.White)
                }
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("退出", color = Color.White)
                }
            }
        }

        if (isLoading) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    text = if (progress > 0f) "正在下载 ${(progress * 100).toInt()}%" else "正在获取校历...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
    }

    // No extra UI
}
