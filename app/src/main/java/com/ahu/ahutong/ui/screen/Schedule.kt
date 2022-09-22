package com.ahu.ahutong.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.ahu.ahutong.R
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.ui.screen.course.CourseCard
import com.ahu.ahutong.ui.screen.course.CourseCardSpec
import com.ahu.ahutong.ui.screen.course.CourseDetails
import com.kyant.monet.Hct.Companion.toHct
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.toColor
import com.kyant.monet.toSrgb
import com.kyant.monet.withNight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// TODO: limit the range of week
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Schedule() {
    val currentWeekday = 5
    var currentWeekTextFieldValue by remember { mutableStateOf(TextFieldValue("1")) }
    val currentWeek = currentWeekTextFieldValue.text.toIntOrNull()
    val schedule = remember { mutableStateListOf<Course>() }
    val courseColors = remember { mutableStateMapOf<String, Color>() }
    val weeklyCourses = remember { mutableStateListOf<Course>() }
    var detailedCourse by remember { mutableStateOf<Course?>(null) }
    val baseColor = 50.a1.toSrgb().toHct()
    LaunchedEffect(Unit) {
        AHURepository.getSchedule("2022-2023", "1", true).onSuccess { courses ->
            schedule += courses
            val courseNames = schedule.map { it.name }.distinct()
            courseNames.forEachIndexed { index, name ->
                courseColors += name to baseColor.copy(h = 360.0 * index / courseNames.size).toSrgb().toColor()
            }
            weeklyCourses += schedule.filter { currentWeek in it.startWeek..it.endWeek }
        }
    }
    LaunchedEffect(schedule, currentWeek) {
        withContext(Dispatchers.IO) {
            weeklyCourses.clear()
            weeklyCourses += schedule.filter { currentWeek in it.startWeek..it.endWeek }
        }
    }
    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(96.n1 withNight 10.n1)
                .alpha(animateFloatAsState(targetValue = if (detailedCourse != null) 0.38f else 1f).value)
                .blur(animateDpAsState(targetValue = if (detailedCourse != null) 16.dp else 0.dp).value)
                .systemBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stringResource(id = R.string.title_schedule)} (第",
                    style = MaterialTheme.typography.headlineLarge
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = {
                        currentWeekTextFieldValue = currentWeekTextFieldValue.copy(
                            currentWeekTextFieldValue.text.toIntOrNull()?.minus(1)?.coerceAtLeast(1)?.toString()
                                ?: "1"
                        )
                    }) {
                        Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = null)
                    }
                }
                val strokeColor = 40.a1 withNight 80.a1
                BasicTextField(
                    value = currentWeekTextFieldValue,
                    onValueChange = { currentWeekTextFieldValue = it },
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .padding(horizontal = 8.dp)
                        .drawBehind {
                            drawLine(
                                color = strokeColor,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = 4.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        },
                    textStyle = MaterialTheme.typography.headlineLarge.copy(color = LocalContentColor.current),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(LocalContentColor.current)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = {
                        currentWeekTextFieldValue = currentWeekTextFieldValue.copy(
                            currentWeekTextFieldValue.text.toIntOrNull()?.plus(1)?.toString() ?: "1"
                        )
                    }) {
                        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null)
                    }
                }
                Text(
                    text = "周)",
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            Box(
                modifier = with(CourseCardSpec) {
                    Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(cellSpacing)
                        .size(
                            mainColumnWidth + (cellWidth + cellSpacing) * 7,
                            mainRowHeight + (cellHeight + cellSpacing) * 11
                        )
                }
            ) {
                Box(
                    modifier = with(CourseCardSpec) {
                        Modifier
                            .size(
                                cellWidth + cellSpacing,
                                mainRowHeight + cellHeight * 11 + cellSpacing * 12
                            )
                            .offset(
                                mainColumnWidth + (cellWidth + cellSpacing) * (currentWeekday - 1) + cellSpacing / 2,
                                -cellSpacing / 2
                            )
                            .border(
                                width = 2.dp,
                                color = 70.a1 withNight 60.a1,
                                shape = RoundedCornerShape(16.dp)
                            )
                    }
                )
                arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEachIndexed { index, weekday ->
                    Box(
                        modifier = with(CourseCardSpec) {
                            Modifier
                                .size(cellWidth, mainRowHeight)
                                .offset(x = mainColumnWidth + (cellWidth + cellSpacing) * index + cellSpacing)
                                .clip(RoundedCornerShape(8.dp))
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = weekday,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                mapOf(
                    1 to "08:20-09:05",
                    2 to "09:15-10:00",
                    3 to "10:20-11:05",
                    4 to "11:15-12:00",
                    5 to "14:00-14:45",
                    6 to "14:55-15:40",
                    7 to "15:50-16:35",
                    8 to "16:45-17:30",
                    9 to "19:00-19:45",
                    10 to "19:55-20:40",
                    11 to "20:50-21:35"
                ).forEach { (index, time) ->
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
                weeklyCourses.forEach { course ->
                    key(course.hashCode()) {
                        CourseCard(
                            course = course,
                            color = courseColors.getValue(course.name),
                            onClick = { detailedCourse = it }
                        )
                    }
                }
            }
        }
        AnimatedContent(
            targetState = detailedCourse,
            transitionSpec = {
                fadeIn() + slideInVertically { it } with
                    fadeOut() + slideOutVertically { it }
            }
        ) {
            it?.let {
                CourseDetails(
                    course = it,
                    onDismiss = { detailedCourse = null }
                )
            }
        }
    }
    BackHandler(enabled = detailedCourse != null) {
        detailedCourse = null
    }
}
