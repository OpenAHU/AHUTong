package com.ahu.ahutong.ui.screen.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.ahu.ahutong.R
//import com.ahu.ahutong.appwidget.ScheduleAppWidgetReceiver
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight

@Composable
fun Tools(navController: NavHostController) {
    val context = LocalContext.current
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
            mainAxisAlignment = MainAxisAlignment.SpaceEvenly,
            crossAxisSpacing = 16.dp
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
                    .clip(CircleShape)
                    .background(90.a1)
                    .clickable {
//                        GlanceAppWidgetManager(context).requestPinGlanceAppWidget(
//                            ScheduleAppWidgetReceiver::class.java
//                        )
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
