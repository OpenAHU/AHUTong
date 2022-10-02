package com.ahu.ahutong.appwidget

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
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
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import arch.sink.utils.TimeUtils
import com.ahu.ahutong.MainActivity
import com.ahu.ahutong.R
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.Course
import java.text.SimpleDateFormat
import java.util.*

class ScheduleAppWidget : GlanceAppWidget() {
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val schedule = remember { mutableStateListOf<Course>() }
        val schoolYear = AHUCache.getSchoolYear() ?: "2022-2023"
        val schoolTerm = AHUCache.getSchoolTerm() ?: "1"

        if (AHUCache.isLogin()) {
            schedule += AHUCache.getSchedule(schoolYear, schoolTerm).orEmpty()
        }

        var time = AHUCache.getSchoolTermStartTime(schoolYear, schoolTerm)
        if (time == null) {
            time = "2022-2-21"
        }
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(time)
        val week = (TimeUtils.getTimeDistance(Date(), date) / 7 + 1).toInt()
        val weekDay = (Calendar.getInstance(Locale.CHINA)[Calendar.DAY_OF_WEEK] - 1).takeIf { it != 0 } ?: 7

        val todayCourses = schedule
            .filter { week in it.startWeek..it.endWeek }
            .filter { it.weekday == weekDay }
            .sortedBy { it.startTime }

        // TODO: go to schedule screen directly & don't launch multiple activities
        val baseModifier = GlanceModifier.clickable(
            actionStartActivity(Intent(context, MainActivity::class.java))
        )

        // TODO: not log in
        Box(
            modifier = baseModifier
                .fillMaxSize()
                .cornerRadius(32.dp)
                .background(R.color.material_dynamic_primary95 withNight R.color.material_dynamic_neutral20),
            contentAlignment = Alignment.Center
        ) {
            Column(modifier = baseModifier.fillMaxSize()) {
                Row(
                    modifier = baseModifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "课表",
                        modifier = baseModifier,
                        style = TextStyle(
                            color = ColorProvider(R.color.material_dynamic_neutral20 withNight R.color.material_dynamic_neutral90),
                            fontSize = 20.sp
                        )
                    )
                    Spacer(modifier = baseModifier.width(8.dp))
                    Image(
                        provider = ImageProvider(R.drawable.baseline_refresh_24),
                        contentDescription = null,
                        modifier = GlanceModifier.clickable(
                            actionRunCallback<RefreshAction>()
                        )
                    )
                    Text(
                        text = SimpleDateFormat("MM月dd日", Locale.CHINA)
                            .format(Calendar.getInstance(Locale.CHINA).time),
                        modifier = baseModifier.fillMaxWidth(),
                        style = TextStyle(
                            color = ColorProvider(R.color.material_dynamic_neutral50 withNight R.color.material_dynamic_neutral70),
                            fontSize = 16.sp,
                            textAlign = TextAlign.End
                        )
                    )
                }
                if (todayCourses.isNotEmpty()) {
                    LazyColumn(modifier = baseModifier.fillMaxSize()) {
                        items(todayCourses) { course ->
                            Column(modifier = baseModifier.padding(16.dp, 4.dp)) {
                                Column(
                                    modifier = baseModifier
                                        .fillMaxWidth()
                                        .cornerRadius(24.dp)
                                        .background(R.color.material_dynamic_neutral99)
                                        .padding(16.dp, 12.dp)
                                ) {
                                    Text(
                                        text = course.name,
                                        modifier = baseModifier,
                                        style = TextStyle(
                                            color = ColorProvider(R.color.material_dynamic_neutral20),
                                            fontSize = 18.sp
                                        )
                                    )
                                    Spacer(modifier = baseModifier.height(4.dp))
                                    Row {
                                        Text(
                                            text = "${course.startTime} - ${course.startTime + course.length - 1}",
                                            modifier = baseModifier,
                                            style = TextStyle(
                                                color = ColorProvider(R.color.material_dynamic_neutral40),
                                                fontSize = 16.sp
                                            )
                                        )
                                        // TODO: more shortenings
                                        Text(
                                            text = course.location
                                                .replace("博学", "博")
                                                .replace("楼", "")
                                                .replace("夫图书馆", ""),
                                            modifier = baseModifier.fillMaxWidth(),
                                            style = TextStyle(
                                                color = ColorProvider(R.color.material_dynamic_neutral40),
                                                fontSize = 16.sp,
                                                textAlign = TextAlign.End
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            Spacer(modifier = baseModifier.height(16.dp))
                        }
                    }
                }
            }
            if (todayCourses.isEmpty()) {
                Text(
                    text = "今日无课",
                    modifier = baseModifier.padding(16.dp),
                    style = TextStyle(
                        color = ColorProvider(R.color.material_dynamic_neutral50 withNight R.color.material_dynamic_neutral70),
                        fontSize = 18.sp
                    )
                )
            }
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

// TODO: observe dark mode changes
@Composable
private infix fun Int.withNight(night: Int): Int {
    val context = LocalContext.current
    val uiMode = context.resources.configuration.uiMode
    return if ((uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) night else this
}
