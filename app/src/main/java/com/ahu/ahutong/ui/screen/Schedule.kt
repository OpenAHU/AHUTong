package com.ahu.ahutong.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.ui.page.state.ScheduleViewModel
import com.ahu.ahutong.ui.screen.course.CourseCard
import com.ahu.ahutong.ui.screen.course.CourseCardSpec
import com.ahu.ahutong.ui.screen.course.CourseDetailDialog
import com.kyant.monet.Hct.Companion.toHct
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.toColor
import com.kyant.monet.toSrgb
import com.kyant.monet.withNight
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun Schedule(scheduleViewModel: ScheduleViewModel = viewModel()) {
    val scheduleConfig by scheduleViewModel.scheduleConfig.observeAsState()
    val currentWeekday = scheduleConfig?.weekDay ?: 1
    var currentWeek by remember { mutableStateOf(scheduleConfig?.week ?: 1) }
    val schedule = scheduleViewModel.schedule.observeAsState().value?.getOrNull() ?: emptyList()
    val baseColor = 50.a1.toSrgb().toHct()
    val courseColors = run {
        val courseNames = schedule.map { it.name }.distinct()
        courseNames.mapIndexed { index, name ->
            name to baseColor.copy(h = 360.0 * index / courseNames.size).toSrgb().toColor()
        }.toMap()
    }
    val currentWeekCourses = schedule.filter { currentWeek in it.startWeek..it.endWeek }
    var detailedCourse by remember { mutableStateOf<Course?>(null) }
    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(96.n1 withNight 10.n1)
                .systemBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp, 8.dp, 16.dp, 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.title_schedule),
                    style = MaterialTheme.typography.headlineMedium
                )
                Row {
                    IconButton(onClick = { currentWeek = scheduleConfig?.week ?: 1 }) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = null
                        )
                    }
                }
            }
            // week selector
            // TODO: auto center selected week item
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(18) {
                    val week = it + 1
                    val isSelected = week == currentWeek
                    CompositionLocalProvider(
                        LocalIndication provides rememberRipple(
                            color = if (isSelected) 100.n1 withNight 0.n1
                            else 0.n1 withNight 100.n1
                        )
                    ) {
                        Text(
                            text = week.toString(),
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    animateColorAsState(
                                        targetValue = if (isSelected) 40.a1 withNight 90.a1
                                        else 100.n1 withNight 20.n1
                                    ).value
                                )
                                .clickable { currentWeek = week }
                                .padding(16.dp, 8.dp),
                            color = animateColorAsState(
                                targetValue = if (isSelected) 100.n1 withNight 0.n1
                                else 0.n1 withNight 100.n1
                            ).value,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
            // class schedule
            Box(
                modifier = with(CourseCardSpec) {
                    Modifier
                        .horizontalScroll(rememberScrollState())
                        .clip(RoundedCornerShape(32.dp))
                        .background(100.n1)
                        .padding(cellSpacing)
                        .size(
                            mainColumnWidth + (cellWidth + cellSpacing) * 7,
                            mainRowHeight + (cellHeight + cellSpacing) * 11
                        )
                }
            ) {
                // current weekday indicator
                if (currentWeek == scheduleConfig?.week) {
                    Box(
                        modifier = with(CourseCardSpec) {
                            Modifier
                                .size(
                                    cellWidth + cellSpacing,
                                    mainRowHeight + cellHeight * 11 + cellSpacing * 12
                                )
                                .offset(
                                    x = mainColumnWidth + (cellWidth + cellSpacing) * (currentWeekday - 1) + cellSpacing / 2
                                )
                                .border(
                                    width = 2.dp,
                                    color = 70.a1 withNight 60.a1,
                                    shape = RoundedCornerShape(16.dp)
                                )
                        }
                    )
                }
                // weekday tags
                arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEachIndexed { index, weekday ->
                    Column(
                        modifier = with(CourseCardSpec) {
                            Modifier
                                .size(cellWidth, mainRowHeight)
                                .offset(x = mainColumnWidth + (cellWidth + cellSpacing) * index + cellSpacing)
                                .clip(RoundedCornerShape(8.dp))
                        },
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = weekday,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = Calendar.getInstance().apply {
                                time = scheduleConfig!!.startTime
                                add(Calendar.DATE, ((currentWeek - 1) * 7) + index)
                            }.let {
                                SimpleDateFormat("MM-dd", Locale.CHINA).format(it.time)
                            },
                            color = 50.n1 withNight 80.n1,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                // time tags
                scheduleViewModel.timetable.forEach { (index, time) ->
                    Column(
                        modifier = with(CourseCardSpec) {
                            Modifier
                                .size(mainColumnWidth, cellHeight)
                                .offset(y = mainRowHeight + (cellHeight + cellSpacing) * (index - 1) + cellSpacing)
                                .clip(RoundedCornerShape(8.dp))
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
                    key(course.hashCode()) {
                        CourseCard(
                            course = course,
                            color = courseColors.getOrElse(course.name) { 50.a1 },
                            onClick = { detailedCourse = it }
                        )
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
}
