package com.ahu.ahutong.ui.screen.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.navigation.NavHostController
import android.widget.Toast
import com.ahu.ahutong.sdk.RustSDK
import com.ahu.ahutong.R
import com.ahu.ahutong.appwidget.ScheduleAppWidgetReceiver
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.kyant.capsule.ContinuousCapsule
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight
import kotlinx.coroutines.launch

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
                    scope.launch {
                        Toast.makeText(context, "正在下载校历...", Toast.LENGTH_SHORT).show()
                        val success = RustSDK.downloadSchoolCalendarToAlbum(context)
                        if (success) {
                            Toast.makeText(context, "校历已保存到相册", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show()
                        }
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
