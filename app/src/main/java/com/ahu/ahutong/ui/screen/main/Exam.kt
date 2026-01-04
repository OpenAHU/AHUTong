package com.ahu.ahutong.ui.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahu.ahutong.R
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.ExamViewModel
import com.kyant.monet.n1
import com.kyant.monet.withNight
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun Exam(
    examViewModel: ExamViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        examViewModel.loadExam()
    }
    val exam = examViewModel.data.observeAsState().value?.getOrNull()
    val isLoading by examViewModel.isLoading.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
            .systemBarsPadding()
            .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.exam),
            modifier = Modifier.padding(24.dp, 32.dp),
            style = MaterialTheme.typography.headlineMedium
        )


        if (isLoading != true) {
            if (!exam.isNullOrEmpty()) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(SmoothRoundedCornerShape(32.dp)),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    exam.forEach {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SmoothRoundedCornerShape(4.dp))
                                .background(100.n1 withNight 20.n1)
                                .clickable {}
                                .padding(24.dp, 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(
                                    text = it.course,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )

                                Spacer(modifier = Modifier.width(8.dp))


                                val isFinished = calcTime(it.time)  //-1：错误  1：未考试 2：考试结束 3：考试中

                                val cardColor: Color = when (isFinished) {
                                    1 -> Color(0xFF4CAF50)
                                    2 -> Color.Gray
                                    3 -> Color(0xFFFFC107)
                                    else -> {
                                        Color.Red
                                    }
                                }


                                Box(
                                    modifier = Modifier
                                        .background(color = cardColor, shape = SmoothRoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (isFinished) {
                                            1 -> "未开始"
                                            2 -> "已结束"
                                            3 -> "进行中"
                                            else -> {
                                                "时间解析错误"
                                            }
                                        },
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            Text(
                                text = "考试时间：${it.time}",
                                color = 30.n1 withNight 90.n1,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "地点：${it.location}，座位号：${it.seatNum}",
                                color = 50.n1 withNight 80.n1,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "目前没有任何考试",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator()
                    Text("加载中...(这个接口有点慢)")
                }
            }
        }


    }
}


fun calcTime(time: String): Int {
    val now = LocalDateTime.now()
    val parts = time.split(" ")
    if (parts.size != 2 || !parts[1].contains("~")) {
        return -1;
    }

    val datePart = parts[0]                   // "2025-06-23"
    val timeParts = parts[1].split("~")       // ["10:20", "12:20"]

    if (timeParts.size != 2) {
        return -1
    }

    val startDateTimeStr = "${datePart} ${timeParts[0]}"
    val endDateTimeStr = "${datePart} ${timeParts[1]}"

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val startTime = LocalDateTime.parse(startDateTimeStr, formatter)
    val endTime = LocalDateTime.parse(endDateTimeStr, formatter)

    when {
        now.isBefore(startTime) -> return 1
        now.isAfter(endTime) -> return 2
        else -> return 3
    }
}