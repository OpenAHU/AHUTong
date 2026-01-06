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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.ScheduleViewModel
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight

@Composable
fun TodayCourseList(
    todayCourses: List<Course>,
    currentMinutes: Int,
    navController: NavHostController
) {
    val currentCourseIndex = todayCourses.indexOfLast {
        val range = ScheduleViewModel.getCourseTimeRangeInMinutes(it)
        currentMinutes > range.first
    }.coerceAtLeast(0)
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
                val activatedColor = 90.a1
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
                        ScheduleViewModel.getCourseTimeRangeInMinutes(
                            todayCourses[currentCourseIndex]
                        )
                    if (currentMinutes in currentCourseRange) {
                        drawRoundRect(
                            color = activatedColor,
                            topLeft = Offset(
                                0f,
                                40.dp.toPx() * currentCourseIndex - 12.dp.toPx()
                            ),
                            size = Size(size.width + 8.dp.toPx(), 48.dp.toPx()),
                            cornerRadius = CornerRadius(24.dp.toPx())
                        )
                    }
                }
            },
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        todayCourses.forEach { course ->
            val isOngoing = currentMinutes in ScheduleViewModel.getCourseTimeRangeInMinutes(course)
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
                    color = if (isOngoing) 20.n1 else 50.n1 withNight 80.n1,
                    fontWeight = if (isOngoing) FontWeight.Bold else null,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = course.name,
                    modifier = Modifier.weight(1f),
                    color = if (isOngoing) 0.n1 else Color.Unspecified,
                    fontWeight = if (isOngoing) FontWeight.Bold else null,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = shortenLocation(course.location),
                    color = if (isOngoing) 20.n1 else 50.n1 withNight 80.n1,
                    fontWeight = if (isOngoing) FontWeight.Bold else null,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

private val LOCATION_SHORTEN_MAP = mapOf(
    "博学北楼" to "博北",
    "博学南楼" to "博南",
    "笃行北楼" to "笃北",
    "笃行南楼" to "笃南",
    "互联大楼" to "互楼",
    "体育场" to "体"
)

private val LOCATION_PATTERN = Regex(LOCATION_SHORTEN_MAP.keys.joinToString("|") { Regex.escape(it) })

private fun shortenLocation(location: String?): String =
    (location ?: "").replace(LOCATION_PATTERN) { LOCATION_SHORTEN_MAP[it.value].orEmpty() }
