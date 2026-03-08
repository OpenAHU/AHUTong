package com.ahu.ahutong.appwidget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.ahu.ahutong.MainActivity
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.crawler.api.jwxt.JwxtApi
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.ui.state.ScheduleViewModel
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.collections.filter
import kotlin.collections.orEmpty
import kotlin.collections.sortedBy

class ScheduleAppWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val teachWeek = runCatching { JwxtApi.API.getCurrentTeachWeek() }.getOrNull()
        teachWeek?.let { AHUCache.saveSchoolTerm(it.currentSemester) }
        val currentWeek = teachWeek?.weekIndex ?: 1
        val weekDay =
            (Calendar.getInstance(Locale.CHINA)[Calendar.DAY_OF_WEEK] - 1).takeIf { it != 0 } ?: 7
        val schedule =
            runCatching { AHURepository.getSchedule(false) }.getOrNull()?.getOrNull().orEmpty()
        val calendar = Calendar.getInstance(Locale.CHINA)
        val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val todayCourses = schedule
            .filter { currentWeek in it.startWeek..it.endWeek }
            .filter { it.weekday == weekDay }
            .filter {
                if (currentWeek in it.weekIndexes) {
                    true
                } else {
                    currentWeek % 2 == it.startWeek % 2
                }
            }
            .sortedBy { it.startTime }
        provideContent {
            ScheduleWidgetContent(
                context = context,
                todayCourses = todayCourses,
                currentMinutes = currentMinutes
            )
        }
    }
}

class ScheduleAppWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ScheduleAppWidget()
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        ScheduleAppWidget().update(context, glanceId)
    }
}

@Composable
private fun ScheduleWidgetContent(
    context: Context,
    todayCourses: List<Course>,
    currentMinutes: Int
) {
    val openAppAction = actionStartActivity(
        Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    )
    val dateText = SimpleDateFormat("MM-dd / EE", Locale.CHINA).format(Date())
    val currentCourseIndex = todayCourses.indexOfLast {
        val range = ScheduleViewModel.getCourseTimeRangeInMinutes(it)
        currentMinutes > range.first
    }.coerceAtLeast(0)
    val backgroundColor = cp(100.n1, 20.n1)
    val offColor = cp(70.n1, 60.n1)
    val onColor = cp(50.a1, 90.a1)
    val activatedRowColor = cp(90.a1, 70.a1)
    val primaryTextColor = cp(10.n1, 95.n1)
    val secondaryTextColor = cp(50.n1, 80.n1)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .cornerRadius(28.dp)
            .clickable(openAppAction)
            .padding(12.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "今日课程 ${todayCourses.size} 节",
                    style = TextStyle(
                        color = primaryTextColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = dateText,
                    style = TextStyle(
                        color = secondaryTextColor,
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = "刷新",
                    modifier = GlanceModifier.clickable(actionRunCallback<RefreshAction>()),
                    style = TextStyle(
                        color = onColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            Spacer(modifier = GlanceModifier.height(10.dp))
            if (todayCourses.isEmpty()) {
                Text(
                    text = "今日无课程",
                    style = TextStyle(
                        color = primaryTextColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp)
                )
                Text(
                    text = "点击打开安大通查看完整课表",
                    style = TextStyle(
                        color = secondaryTextColor,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = GlanceModifier.fillMaxWidth()
                )
            } else {
                Column(
                    modifier = GlanceModifier.fillMaxWidth()
                ) {
                    todayCourses.forEachIndexed { index, course ->
                        val currentRange = ScheduleViewModel.getCourseTimeRangeInMinutes(course)
                        val isOngoing = currentMinutes in currentRange
                        val isPassed = index <= currentCourseIndex
                        val rowColor = if (isOngoing) activatedRowColor else cp(
                            Color.Transparent,
                            Color.Transparent
                        )
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .background(rowColor)
                                .cornerRadius(20.dp)
                                .padding(horizontal = 6.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isPassed) "●" else "○",
                                style = TextStyle(
                                    color = if (isPassed) onColor else offColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = GlanceModifier.width(10.dp))
                            Column(
                                modifier = GlanceModifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = course.name,
                                    style = TextStyle(
                                        color = if (isOngoing) cp(0.n1, 0.n1) else primaryTextColor,
                                        fontSize = 14.sp,
                                        fontWeight = if (isOngoing) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    maxLines = 1
                                )
                                Spacer(modifier = GlanceModifier.height(2.dp))
                                Row(
                                    modifier = GlanceModifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${course.startTime}-${course.startTime + course.length - 1}节",
                                        style = TextStyle(
                                            color = if (isOngoing) cp(20.n1, 20.n1) else secondaryTextColor,
                                            fontSize = 11.sp
                                        )
                                    )
                                    Spacer(modifier = GlanceModifier.width(8.dp))
                                    Text(
                                        text = shortenLocation(course.location),
                                        style = TextStyle(
                                            color = if (isOngoing) cp(20.n1, 20.n1) else secondaryTextColor,
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        if (index != todayCourses.lastIndex) {
                            Spacer(modifier = GlanceModifier.height(4.dp))
                        }
                    }
                }
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

private val LOCATION_PATTERN =
    Regex(LOCATION_SHORTEN_MAP.keys.joinToString("|") { Regex.escape(it) })

private fun shortenLocation(location: String?): String =
    (location ?: "").replace(LOCATION_PATTERN) { LOCATION_SHORTEN_MAP[it.value].orEmpty() }

private fun cp(light: Color, dark: Color): ColorProvider = ColorProvider(light, dark)
