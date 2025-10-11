package com.ahu.ahutong.ui.screen.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ahu.ahutong.ui.screen.main.home.AtAGlance
import com.ahu.ahutong.ui.screen.main.home.BathroomOpening
import com.ahu.ahutong.ui.screen.main.home.CampusCard
import com.ahu.ahutong.ui.screen.main.home.ElectricityCard
import com.ahu.ahutong.ui.screen.main.home.TodayCourseList
import com.ahu.ahutong.ui.state.DiscoveryViewModel
import com.ahu.ahutong.ui.state.ScheduleViewModel
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun Home(
    discoveryViewModel: DiscoveryViewModel = viewModel(),
    scheduleViewModel: ScheduleViewModel = viewModel(),
    navController: NavHostController
) {
    val schedule = scheduleViewModel.schedule.observeAsState().value?.getOrNull() ?: emptyList()
    val scheduleConfig by scheduleViewModel.scheduleConfig.observeAsState()
    val currentWeek = scheduleConfig?.week ?: 1
    val todayCourses = schedule
        .filter { scheduleConfig?.week in it.startWeek..it.endWeek }
        .filter { it.weekday == (scheduleConfig?.weekDay ?: 1) }
        .filter {
            if (currentWeek in it.weekIndexes) {
                true
            } else {
                currentWeek % 2 == it.startWeek % 2
            }
        }
        .sortedBy { it.startTime }
    val calendar = Calendar.getInstance(Locale.CHINA)
    val currentMinutes = calendar.time.let {
        calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
    }
    LaunchedEffect(Unit) {
        repeat(2 - discoveryViewModel.visibilities.size) {
            delay(100)
            discoveryViewModel.visibilities += discoveryViewModel.visibilities.lastIndex + 1
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .systemBarsPadding()
            .padding(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        AtAGlance(
            todayCourses = todayCourses,
            currentMinutes = currentMinutes,
            navController = navController
        )
        if (todayCourses.isNotEmpty()) {
            SlideInContent(visible = 0 in discoveryViewModel.visibilities) {
                TodayCourseList(
                    todayCourses = todayCourses,
                    currentMinutes = currentMinutes,
                    navController = navController
                )
            }
        }
        SlideInContent(visible = 1 in discoveryViewModel.visibilities) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            )  {

                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CampusCard(
                        balance = discoveryViewModel.balance,
                        transitionBalance = discoveryViewModel.transitionBalance,
                        navController
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BathroomOpening(navController = navController, discoveryViewModel = discoveryViewModel)
                        ElectricityCard(navController = navController, discoveryViewModel = discoveryViewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun SlideInContent(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn() + slideInVertically { it / 2 },
        content = content
    )
}
