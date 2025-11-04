package com.ahu.ahutong.ui.screen.main.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.ScheduleViewModel
import com.kyant.monet.a1
import com.kyant.monet.withNight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AtAGlance(
    todayCourses: List<Course>,
    currentMinutes: Int,
    navController: NavHostController
) {
    val currentCourse = todayCourses.find {
        currentMinutes in ScheduleViewModel.getCourseTimeRangeInMinutes(it)
    }
    val currentCourseIndex = todayCourses.indexOfFirst {
        val range = ScheduleViewModel.getCourseTimeRangeInMinutes(it)
        if (currentMinutes in range) {
            true
        } else {
            currentMinutes < range.first
        }
    }.takeIf { it != -1 } ?: todayCourses.lastIndex
    val hasRemainingCourses = if (todayCourses.isNotEmpty()) {
        currentMinutes <= ScheduleViewModel.getCourseTimeRangeInMinutes(todayCourses.last()).last
    } else {
        false
    }
    val date = SimpleDateFormat("MM-dd / EE", Locale.CHINA).format(Date())
    Column(
        modifier = Modifier.padding(vertical = 0.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp, 8.dp, 16.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.bodyLarge
            )
            IconButton(onClick = { navController.navigate("settings") }) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(SmoothRoundedCornerShape(32.dp))
                .clickable { navController.navigate("schedule") }
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = when {
                    currentCourse != null -> "正在上课"
                    hasRemainingCourses -> "下节课是"
                    else -> "今日课程"
                },
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = when {
                    currentCourse != null -> currentCourse.name
                    hasRemainingCourses -> todayCourses[currentCourseIndex].name
                    else -> "已全部上完"
                },
                modifier = if (currentCourse != null || hasRemainingCourses) {
                    Modifier
                        .composed {
                            val color = 50.a1 withNight 90.a1
                            drawBehind {
                                drawLine(
                                    color = color,
                                    start = Offset.Zero,
                                    end = Offset(0f, size.height),
                                    strokeWidth = 4.dp.toPx()
                                )
                            }
                        }
                        .padding(start = 8.dp)
                } else {
                    Modifier
                },
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = when {
                    currentCourse != null -> {
                        val duration =
                            ScheduleViewModel.getCourseTimeRangeInMinutes(currentCourse).last - currentMinutes
                        "距下课还有 " + when {
                            duration % 60 == 0 -> "${duration / 60}小时整"
                            duration > 60 -> "${duration / 60}小时${duration % 60}分钟"
                            else -> "${duration}分钟"
                        }
                    }

                    hasRemainingCourses -> {
                        val duration =
                            ScheduleViewModel.getCourseTimeRangeInMinutes(
                                todayCourses[currentCourseIndex]
                            ).first - currentMinutes
                        "还有 " + when {
                            duration % 60 == 0 -> "${duration / 60}小时整"
                            duration > 60 -> "${duration / 60}小时${duration % 60}分钟"
                            else -> "${duration}分钟"
                        } + "，地点在 ${todayCourses[currentCourseIndex].location}"
                    }

                    else -> "准备您自己的安排吧"
                },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
