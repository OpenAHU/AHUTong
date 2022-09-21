package com.ahu.ahutong.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ahu.ahutong.R
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.model.Course
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun Schedule() {
    var week by rememberSaveable { mutableStateOf(1) }
    val schedule = remember { mutableStateListOf<Course>() }
    val courseColors = remember { mutableStateMapOf<String, Color>() }
    val weeklyCourses = remember { mutableStateListOf<Course>() }
    val baseColor = 50.a1.toSrgb().toHct()
    LaunchedEffect(Unit) {
        AHURepository.getSchedule("2022-2023", "1", true).onSuccess { courses ->
            schedule += courses
            val courseNames = schedule.map { it.name }.distinct()
            courseNames.forEachIndexed { index, name ->
                courseColors += name to baseColor.copy(h = 360.0 * index / courseNames.size).toSrgb().toColor()
            }
            weeklyCourses += schedule.filter { week in it.startWeek..it.endWeek }
        }
    }
    LaunchedEffect(schedule, week) {
        withContext(Dispatchers.IO) {
            weeklyCourses.clear()
            weeklyCourses += schedule.filter { week in it.startWeek..it.endWeek }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(96.n1 withNight 10.n1)
            .systemBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${stringResource(id = R.string.title_schedule)} (第",
                style = MaterialTheme.typography.headlineLarge
            )
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(80.n1 withNight 30.n1)
                    .combinedClickable(
                        onDoubleClick = {
                            if (week > 1) {
                                week--
                            }
                        }
                    ) { week++ }
                    .padding(horizontal = 8.dp)
            ) {
                AnimatedContent(
                    targetState = week,
                    transitionSpec = {
                        fadeIn() + slideInVertically { it } with
                            fadeOut() + slideOutVertically { it }
                    }
                ) {
                    Text(
                        text = it.toString(),
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            }
            Text(
                text = "周)",
                style = MaterialTheme.typography.headlineLarge
            )
        }
        val mainColumnWidth = 32.dp
        val mainRowHeight = 24.dp
        val cellWidth = 56.dp
        val cellHeight = 48.dp
        val cellSpacing = 8.dp
        Box(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(cellSpacing)
                .size(
                    mainColumnWidth + (cellWidth + cellSpacing) * 7,
                    mainRowHeight + (cellHeight + cellSpacing) * 11
                )
        ) {
            arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEachIndexed { index, weekday ->
                Box(
                    modifier = Modifier
                        .size(cellWidth, mainRowHeight)
                        .offset(x = mainColumnWidth + (cellWidth + cellSpacing) * index + cellSpacing)
                        .clip(RoundedCornerShape(8.dp)),
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
                    modifier = Modifier
                        .size(mainColumnWidth, cellHeight)
                        .offset(y = mainRowHeight + (cellHeight + cellSpacing) * (index - 1) + cellSpacing)
                        .clip(RoundedCornerShape(8.dp)),
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
            weeklyCourses.forEach {
                CompositionLocalProvider(
                    LocalTonalPalettes provides courseColors.getValue(it.name).toTonalPalettes(
                        style = PaletteStyle.Vibrant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .size(
                                cellWidth,
                                cellHeight * it.length + cellSpacing * (it.length - 1)
                            )
                            .offset(
                                mainColumnWidth + (cellWidth + cellSpacing) * (it.weekday - 1) + cellSpacing,
                                mainRowHeight + (cellHeight + cellSpacing) * (it.startTime - 1) + cellSpacing
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .background(70.a1 withNight 60.a1),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = it.name,
                            modifier = Modifier.padding(4.dp),
                            color = 100.n1,
                            fontWeight = FontWeight.Bold,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 3,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = it.location
                                .replace("博学", "博")
                                .replace("楼", ""),
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(95.a1 withNight 30.n2)
                                .padding(2.dp),
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
