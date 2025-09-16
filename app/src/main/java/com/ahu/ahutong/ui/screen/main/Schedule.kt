package com.ahu.ahutong.ui.screen.main

import android.R
import android.preference.PreferenceManager
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ahu.ahutong.data.dao.PreferencesManager
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.ui.screen.main.schedule.CourseCard
import com.ahu.ahutong.ui.screen.main.schedule.CourseCardSpec
import com.ahu.ahutong.ui.screen.main.schedule.CourseDetailDialog
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.ScheduleViewModel
import com.kyant.monet.Hct.Companion.toHct
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.toColor
import com.kyant.monet.toSrgb
import com.kyant.monet.withNight
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun Schedule(scheduleViewModel: ScheduleViewModel = hiltViewModel()) {
    val scope = rememberCoroutineScope()
    val scheduleConfig by scheduleViewModel.scheduleConfig.observeAsState()
    val currentWeekday = scheduleConfig?.weekDay ?: 1
    var currentWeek by rememberSaveable { mutableStateOf(scheduleConfig?.week ?: 1) }
    val state = rememberLazyListState(
        initialFirstVisibleItemIndex = (currentWeek - 3).coerceAtLeast(0)
    )
    val schedule = scheduleViewModel.schedule.observeAsState().value?.getOrNull() ?: emptyList()
    val baseColor = 50.a1.toSrgb().toHct()
    val courseColors by remember(Unit) {
        mutableStateOf(
            schedule.map { it.name }.distinct()
                .mapIndexed { index, name ->
                    name to baseColor.copy(h = 360.0 * index / schedule.map { it.name }.distinct().size).toSrgb().toColor()
                }.toMap()
        )
    }

    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context = context) }

    val isShowAllCourse by preferencesManager.isShowAllCourse.collectAsState(initial = false)
    val currentWeekCourses = schedule

    var detailedCourse by rememberSaveable { mutableStateOf<Course?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .systemBarsPadding()
            .padding(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // week selector
            LazyRow(
                modifier = Modifier.weight(1f),
                state = state,
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(20) {
                    val week = it + 1
                    val isSelected = week == currentWeek
                    CompositionLocalProvider(
                        LocalIndication provides rememberRipple(
                            color = if (isSelected) {
                                100.n1 withNight 0.n1
                            } else {
                                0.n1 withNight 100.n1
                            }
                        )
                    ) {
                        Text(
                            text = week.toString(),
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    animateColorAsState(
                                        targetValue = if (isSelected) {
                                            40.a1 withNight 90.a1
                                        } else {
                                            Color.Transparent
                                        }
                                    ).value
                                )
                                .clickable { currentWeek = week }
                                .padding(16.dp, 8.dp),
                            color = animateColorAsState(
                                targetValue = if (isSelected) {
                                    100.n1 withNight 0.n1
                                } else {
                                    0.n1 withNight 100.n1
                                }
                            ).value,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
            // actions
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(100.n1 withNight 30.n1)
            ) {
                IconButton(
                    onClick = {
                        currentWeek = scheduleConfig?.week ?: 1
                        scope.launch {
                            state.animateScrollToItem((currentWeek - 3).coerceAtLeast(0))
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null
                    )
                }
                IconButton(onClick = { scheduleViewModel.refreshSchedule(isRefresh = true) }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null
                    )
                }
            }
        }
        // schedule
        val cellWidth = (
                LocalConfiguration.current.screenWidthDp.dp -
                        CourseCardSpec.mainColumnWidth -
                        CourseCardSpec.cellSpacing * 9
                ) / 7
        val cellHeight = 48.dp
        Box(
            modifier = with(CourseCardSpec) {
                Modifier
                    .fillMaxWidth()
                    .height(mainRowHeight + (cellHeight + cellSpacing) * 13 + 24.dp)
                    .clip(SmoothRoundedCornerShape(32.dp))
                    .background(99.n1 withNight 20.n1)
                    .padding(top = 8.dp)
                    .padding(cellSpacing)
            }
        ) {
            // TODO: current time indicator
            // weekday tags

            val weekDates by remember(currentWeek, scheduleConfig?.startTime) {
                mutableStateOf(
                    List(7) { index ->
                        Calendar.getInstance().apply {
                            time = scheduleConfig?.startTime
                                ?: SimpleDateFormat("MM-dd", Locale.CHINA).parse("09-01")
                            add(Calendar.DATE, ((currentWeek - 1) * 7) + index)
                        }
                    }
                )
            }

            weekDates.forEachIndexed { index, date ->
                val isCurrentWeekday = currentWeek == scheduleConfig?.week && index + 1 == currentWeekday
                Column(
                    modifier = with(CourseCardSpec) {
                        Modifier
                            .size(cellWidth, mainRowHeight)
                            .offset(
                                x = mainColumnWidth + (cellWidth + cellSpacing) * index + cellSpacing
                            )
                            .clip(SmoothRoundedCornerShape(8.dp))
                            .background(if (isCurrentWeekday) 90.a1 else Color.Unspecified)
                    },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")[index],
                        color = if (isCurrentWeekday) 0.n1 else Color.Unspecified,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = SimpleDateFormat("MM-dd", Locale.CHINA).format(date.time),
                        color = if (isCurrentWeekday) 0.n1 else 50.n1 withNight 80.n1,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // time tags
            ScheduleViewModel.timetable.forEach { (index, time) ->
                Column(
                    modifier = with(CourseCardSpec) {
                        Modifier
                            .size(mainColumnWidth, cellHeight)
                            .offset(
                                y = mainRowHeight + (cellHeight + cellSpacing) * (index - 1) + cellSpacing
                            )
                            .clip(SmoothRoundedCornerShape(8.dp))
                    },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = index.toString(),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = time.substringBefore("-"),
                        color = 50.n1 withNight 80.n1,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            // courses
            currentWeekCourses.forEach { course ->

                val isCurrentWeek = currentWeek in course.weekIndexes

                if ((currentWeek >= 1
                    && currentWeek <= course.weekIndexes.last() && isShowAllCourse)

                    || isCurrentWeek

                ) {
                    key(course.hashCode()) {

                        CourseCard(
                            course = course,
                            color =  courseColors.getOrElse(course.name) { 50.a1 },
                            cellWidth = cellWidth,
                            cellHeight = cellHeight,
                            isCurrentWeek = isCurrentWeek,
                            onClick = { detailedCourse = it }
                        )
                    }
                }


            }
        }
    }
    // course dialog
    detailedCourse?.let {
        CourseDetailDialog(
            course = it,
            onDismiss = { detailedCourse = null }
        )
    }
}
