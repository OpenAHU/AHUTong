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
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.utils.FileUtils
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
                    navController.navigate("school_calendar")
                }
            )
            ToolItem(
                stringId = R.string.free_classroom,
                iconId = R.drawable.ic_round_business_24,
                tint = Color(0xFF03A9F4),
                onClick = { navController.navigate("free_classroom") }
            )
            ToolItem(
                stringId = R.string.lost_found,
                iconId = R.drawable.lost_and_found,
                tint = Color(0xFF1976D2),
                onClick = { navController.navigate("lost_found") }
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
            .width(88.dp)
            .clip(SmoothRoundedCornerShape(16.dp))
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .padding(vertical = 16.dp, horizontal = 4.dp),
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
