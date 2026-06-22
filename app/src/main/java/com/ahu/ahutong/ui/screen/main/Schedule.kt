package com.ahu.ahutong.ui.screen.main

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.ui.screen.main.schedule.CourseCard
import com.ahu.ahutong.ui.screen.main.schedule.CourseCardSpec
import com.ahu.ahutong.ui.screen.main.schedule.CourseDetailDialog
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.ScheduleViewModel
import com.kyant.capsule.ContinuousCapsule
import com.kyant.monet.Hct.Companion.toHct
import com.kyant.monet.LocalTonalPalettes
import com.kyant.monet.PaletteStyle
import com.kyant.monet.TonalPalettes.Companion.toTonalPalettes
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.n2
import com.kyant.monet.toColor
import com.kyant.monet.toSrgb
import com.kyant.monet.withNight
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import android.widget.Toast
import com.ahu.ahutong.ui.screen.main.schedule.CourseCardSpec.cellSpacing

@Composable
fun Schedule(scheduleViewModel: ScheduleViewModel = hiltViewModel()) {
    val scope = rememberCoroutineScope()
    val scheduleConfig by scheduleViewModel.scheduleConfig.observeAsState()
    val currentWeekday = scheduleConfig?.weekDay ?: 1
    var currentWeek by rememberSaveable { mutableStateOf(scheduleConfig?.week ?: 1) }
    val pagerState = rememberPagerState(
        initialPage = (currentWeek - 1).coerceAtLeast(0),
        pageCount = { 20 }
    )
    val state = rememberLazyListState(
        initialFirstVisibleItemIndex = (currentWeek - 3).coerceAtLeast(0)
    )
    val scheduleResult = scheduleViewModel.schedule.observeAsState().value
    val nextScheduleResult = scheduleViewModel.nextSchedule.observeAsState().value
    var isPreviewNextSemester by rememberSaveable { mutableStateOf(false) }
    var isOverviewSchedule by rememberSaveable { mutableStateOf(false) }
    var isSettingsVisible by rememberSaveable { mutableStateOf(false) }
    val activeScheduleResult = if (isPreviewNextSemester) nextScheduleResult else scheduleResult
    val schedule = activeScheduleResult?.getOrNull() ?: emptyList()
    val context = LocalContext.current

    LaunchedEffect(currentWeek) {
        state.animateScrollToItem(
            (currentWeek - 3).coerceAtLeast(0)
        )
    }

    LaunchedEffect(scheduleConfig?.week) {
        if (!isPreviewNextSemester) {
            scheduleConfig?.week?.let { currentWeek = it }
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        currentWeek = pagerState.currentPage + 1
    }

    LaunchedEffect(scheduleResult) {
        scheduleResult?.exceptionOrNull()?.let {
            Toast.makeText(context, "加载课表失败: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(nextScheduleResult) {
        nextScheduleResult?.exceptionOrNull()?.let {
            Toast.makeText(context, "加载下学期课表失败: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(isPreviewNextSemester) {
        if (isPreviewNextSemester && nextScheduleResult == null) {
            scheduleViewModel.refreshNextSchedule()
        }
        val targetWeek = if (isPreviewNextSemester) 1 else scheduleConfig?.week ?: 1
        currentWeek = targetWeek
        pagerState.animateScrollToPage((targetWeek - 1).coerceAtLeast(0))
    }

    val baseColor = 50.a1.toSrgb().toHct()
    val courseColors by remember(schedule) {
        mutableStateOf(
            schedule.map { it.name }.distinct()
                .mapIndexed { index, name ->
                    name to baseColor.copy(
                        h = 360.0 * index / schedule.map { it.name }
                            .distinct().size.coerceAtLeast(1)
                    ).toSrgb()
                        .toColor()
                }.toMap()
        )
    }

    val currentWeekCourses = schedule

    var detailedCourse by rememberSaveable { mutableStateOf<Course?>(null) }
    val settingsCardColor = 100.n1 withNight 20.n1
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
                        LocalIndication provides ripple(
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
                                .clip(ContinuousCapsule)
                                .background(
                                    animateColorAsState(
                                        targetValue = if (isSelected) {
                                            40.a1 withNight 90.a1
                                        } else {
                                            Color.Transparent
                                        }
                                    ).value
                                )
                                .clickable {
                                    scope.launch {
                                        pagerState.animateScrollToPage(week - 1)
                                    }
                                }
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
                    .clip(ContinuousCapsule)
                    .background(100.n1 withNight 30.n1)
                    .padding(horizontal = 2.dp, vertical = 2.dp)
            ) {

                IconButton(
                    modifier = Modifier.size(38.dp),
                    onClick = {
                        if (isPreviewNextSemester) {
                            isPreviewNextSemester = false
                        }
                        scope.launch {
                            state.animateScrollToItem((currentWeek - 3).coerceAtLeast(0))
                        }
                        scope.launch {
                            pagerState.animateScrollToPage((scheduleConfig?.week ?: 1) - 1)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    modifier = Modifier.size(38.dp),
                    onClick = { isSettingsVisible = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    modifier = Modifier.size(38.dp),
                    onClick = {
                        if (isPreviewNextSemester) {
                            scheduleViewModel.refreshNextSchedule(true)
                        } else {
                            scheduleViewModel.refreshSchedule(true)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
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
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val pageWeek = page + 1
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

                val weekDates by remember(pageWeek, scheduleConfig?.startTime) {
                    mutableStateOf(
                        List(7) { index ->
                            Calendar.getInstance().apply {
                                time = scheduleConfig?.startTime
                                    ?: SimpleDateFormat("MM-dd", Locale.CHINA).parse("09-01")
                                add(Calendar.DATE, ((pageWeek - 1) * 7) + index)
                            }
                        }
                    )
                }

                weekDates.forEachIndexed { index, date ->
                    val isCurrentWeekday =
                        !isPreviewNextSemester && pageWeek == scheduleConfig?.week && index + 1 == currentWeekday
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
                            text = arrayOf(
                                "周一",
                                "周二",
                                "周三",
                                "周四",
                                "周五",
                                "周六",
                                "周日"
                            )[index],
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
                if (isOverviewSchedule) {
                    currentWeekCourses
                        .groupBy { "${it.weekday}-${it.startTime}-${it.length}" }
                        .values
                        .forEach { sameTimeCourses ->
                            key(sameTimeCourses.joinToString("-") { it.hashCode().toString() }) {
                                OverviewCourseGroupCard(
                                    courses = sameTimeCourses,
                                    colors = courseColors,
                                    cellWidth = cellWidth,
                                    cellHeight = cellHeight,
                                    currentWeek = pageWeek,
                                    onClick = { detailedCourse = it }
                                )
                            }
                        }
                } else {
                    currentWeekCourses.forEach { course ->
                        val isCurrentWeek = pageWeek in course.weekIndexes
                        if (isCurrentWeek) {
                            key(course.hashCode()) {

                                CourseCard(
                                    course = course,
                                    color = courseColors.getOrElse(course.name) { 50.a1 },
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
        }
        if (isSettingsVisible) {
            ScheduleSettingsDialog(
                isOverviewSchedule = isOverviewSchedule,
                isPreviewNextSemester = isPreviewNextSemester,
                backdropColor = settingsCardColor,
                onOverviewChange = { isOverviewSchedule = it },
                onPreviewNextSemesterChange = { isPreviewNextSemester = it },
                onDismiss = { isSettingsVisible = false }
            )
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

@Composable
private fun ScheduleSettingsDialog(
        isOverviewSchedule: Boolean,
        isPreviewNextSemester: Boolean,
        backdropColor: Color,
        onOverviewChange: (Boolean) -> Unit,
        onPreviewNextSemesterChange: (Boolean) -> Unit,
        onDismiss: () -> Unit
    ) {
        AlertDialog(
            containerColor = backdropColor,
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "课表设置",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = 0.n1 withNight 100.n1
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ScheduleSettingRow(
                        title = "总览课表",
                        description = "显示全部周次的课程，重叠课程会平分同一块时间区域",
                        selected = isOverviewSchedule,
                        onSelect = onOverviewChange
                    )
                    ScheduleSettingRow(
                        title = "预览下学期课表",
                        description = "切换到教务系统中的下学期课表",
                        selected = isPreviewNextSemester,
                        onSelect = onPreviewNextSemesterChange
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = "完成")
                }
            }
        )
    }


    @Composable
    private fun ScheduleSettingRow(
        title: String,
        description: String,
        selected: Boolean,
        onSelect: (Boolean) -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(SmoothRoundedCornerShape(12.dp))
                .clickable { onSelect(!selected) }
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = 0.n1 withNight 100.n1
                )
                Text(
                    text = description,
                    color = 50.n1 withNight 80.n1,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(
                checked = selected,
                onCheckedChange = onSelect,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }

@Composable
private fun OverviewCourseGroupCard(
    courses: List<Course>,
    colors: Map<String, Color>,
    cellWidth: Dp,
    cellHeight: Dp,
    currentWeek: Int,
    onClick: (Course) -> Unit
) {
    val course = courses.firstOrNull() ?: return
    val fullHeight = cellHeight * course.length + cellSpacing * (course.length - 1)
    Box(
        modifier = with(CourseCardSpec) {
            Modifier
                .size(
                    cellWidth,
                    fullHeight
                )
                .offset(
                    x = mainColumnWidth +
                                (cellWidth + cellSpacing) * (course.weekday - 1) +
                                cellSpacing,
                    y = mainRowHeight +
                            (cellHeight + cellSpacing) * (course.startTime - 1) +
                                cellSpacing
                )
                .clip(SmoothRoundedCornerShape(8.dp))
                .background(95.a1 withNight 30.n1)
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            courses.forEachIndexed { index, item ->
                val isCurrentWeek = currentWeek in item.weekIndexes
                val color = colors.getOrElse(item.name) { 50.a1 }
                CompositionLocalProvider(
                    LocalTonalPalettes provides color.toTonalPalettes(
                        style = PaletteStyle.Vibrant,
                        tonalValues = doubleArrayOf()
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(if (isCurrentWeek) color else color.copy(alpha = 0.45f))
                            .clickable { onClick(item) }
                            .padding(4.dp)
                    ) {
                        Text(
                            text = item.name,
                            modifier = Modifier.padding(bottom = 38.dp),
                            color = 100.n1,
                            fontWeight = FontWeight.Bold,
                            maxLines = if (courses.size <= 2) 3 else 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${item.startWeek}-${item.endWeek}",
                                color = 100.n1,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = item.location.shortLocation(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(SmoothRoundedCornerShape(6.dp))
                                    .background(95.a1 withNight 30.n2)
                                    .padding(2.dp),
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 2,
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    color = 10.n1 withNight 90.n1,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
                if (index != courses.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(100.n1.copy(alpha = 0.65f) withNight 0.n1.copy(alpha = 0.35f))
                    )
                }
            }
        }
    }
}

private fun String?.shortLocation(): String {
    val location = this
        ?.replace("博学北楼", "博北")
        ?.replace("博学南楼", "博南")
        ?.replace("笃行南楼", "笃南")
        ?.replace("笃行北楼", "笃北")
        ?.replace("互联大楼", "互楼")
        ?.replace("体育场", "体")
    val labRoom = location
        ?.trim()
        ?.let { Regex("(?<![A-Za-z0-9])([A-Za-z]\\d{3})\\s*\\[").


        find(it)?.groupValues?.get(1) }
    return labRoom ?: location.takeIf { !it.isNullOrBlank() } ?: "未知"
}
