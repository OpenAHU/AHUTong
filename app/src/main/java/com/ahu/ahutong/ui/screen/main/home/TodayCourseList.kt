package com.ahu.ahutong.ui.screen.main.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.ScheduleViewModel
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight
import kotlin.math.roundToInt

@OptIn(ExperimentalTextApi::class)
@Composable
fun TodayCourseList(
    todayCourses: List<Course>,
    currentMinutes: Int,
    navController: NavHostController
) {
    if (todayCourses.isEmpty()) return
    val currentCourseIndex = todayCourses.indexOfFirst {
        val range = ScheduleViewModel.getCourseTimeRangeInMinutes(it)
        if (currentMinutes in range) true
        else currentMinutes < range.first
    }.takeIf { it != -1 } ?: todayCourses.lastIndex
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(SmoothRoundedCornerShape(32.dp))
            .background(100.n1 withNight 20.n1)
            .clickable { navController.navigate("schedule") }
            .padding(16.dp)
            .composed {
                val offColor = 70.n1 withNight 60.n1
                val onColor = 50.a1 withNight 90.a1
                val activatedColor = Color(0xFFFBC02D) withNight Color(0xFFFFECB3)
                val textMeasurer = rememberTextMeasurer()
                val textStyle = MaterialTheme.typography.labelMedium.copy(color = 100.n1 withNight 0.n1)
                drawBehind {
                    drawLine(
                        color = offColor,
                        start = Offset(4.dp.toPx(), 12.dp.toPx()),
                        end = Offset(4.dp.toPx(), size.height - 12.dp.toPx()),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(4.dp.toPx(), 4.dp.toPx()),
                            phase = 8.dp.toPx()
                        )
                    )
                    drawLine(
                        color = onColor,
                        start = Offset(4.dp.toPx(), 12.dp.toPx()),
                        end = Offset(
                            4.dp.toPx(),
                            40.dp.toPx() * currentCourseIndex + 12.dp.toPx()
                        ),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(6.dp.toPx(), 4.dp.toPx()),
                            phase = 4.dp.toPx()
                        )
                    )
                    repeat(currentCourseIndex + 1) {
                        drawCircle(
                            color = onColor,
                            radius = 4.dp.toPx(),
                            center = Offset(
                                4.dp.toPx(),
                                40.dp.toPx() * it + 12.dp.toPx()
                            )
                        )
                    }
                    repeat(todayCourses.size - currentCourseIndex - 1) {
                        drawCircle(
                            color = offColor,
                            radius = 2.dp.toPx(),
                            center = Offset(
                                4.dp.toPx(),
                                40.dp.toPx() * (currentCourseIndex + 1 + it) + 12.dp.toPx()
                            ),
                            style = Stroke(2.dp.toPx())
                        )
                    }
                    val currentCourseRange =
                        ScheduleViewModel.getCourseTimeRangeInMinutes(todayCourses[currentCourseIndex])
                    if (currentMinutes in currentCourseRange) {
                        val ongoingFraction =
                            (currentMinutes.toFloat() - currentCourseRange.first) / (currentCourseRange.last.toFloat() - currentCourseRange.first)
                        drawCircle(
                            color = activatedColor,
                            radius = 12.dp.toPx(),
                            center = Offset(
                                4.dp.toPx(),
                                40.dp.toPx() * currentCourseIndex + 12.dp.toPx()
                            )
                        )
                        drawText(
                            textMeasurer = textMeasurer,
                            text = "${(ongoingFraction * 100f).roundToInt()}",
                            topLeft = Offset(
                                -4.dp.toPx(),
                                40.dp.toPx() * currentCourseIndex + 3.dp.toPx()
                            ),
                            style = textStyle
                        )
                    }
                }
            },
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        todayCourses.forEach { course ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .padding(start = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${course.startTime} - ${course.startTime + course.length - 1}",
                    modifier = Modifier.width(48.dp),
                    color = 50.n1 withNight 80.n1,
                    fontWeight = if (currentMinutes in ScheduleViewModel.getCourseTimeRangeInMinutes(course)) FontWeight.Bold
                    else FontWeight.Normal,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = course.name,
                    fontWeight = if (currentMinutes in ScheduleViewModel.getCourseTimeRangeInMinutes(course)) FontWeight.Bold
                    else FontWeight.Normal,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = course.location
                        .replace("博学", "博")
                        .replace("楼", "")
                        .replace("育场", ""),
                    modifier = Modifier.weight(1f),
                    color = 50.n1 withNight 80.n1,
                    fontWeight = if (currentMinutes in ScheduleViewModel.getCourseTimeRangeInMinutes(course)) FontWeight.Bold
                    else FontWeight.Normal,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
