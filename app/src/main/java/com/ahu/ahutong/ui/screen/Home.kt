package com.ahu.ahutong.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ahu.ahutong.R
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.ui.page.state.DiscoveryViewModel
import com.ahu.ahutong.ui.page.state.ScheduleViewModel
import com.ahu.ahutong.ui.screen.component.BathroomOpenStates
import com.ahu.ahutong.ui.screen.component.CampusCard
import com.ahu.ahutong.ui.screen.component.EmptyCourse
import com.ahu.ahutong.ui.screen.component.FunctionalButton
import com.ahu.ahutong.ui.screen.component.SquigglyUnderlinedText
import com.ahu.ahutong.ui.screen.component.TodayCourses
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.kyant.monet.n1
import com.kyant.monet.withNight
import java.util.*

@Composable
fun Home(
    discoveryViewModel: DiscoveryViewModel = viewModel(),
    scheduleViewModel: ScheduleViewModel = viewModel(),
    navController: NavHostController
) {
    val user = AHUCache.getCurrentUser() ?: return
    val schedule = scheduleViewModel.schedule.observeAsState().value?.getOrNull() ?: emptyList()
    val scheduleConfig by scheduleViewModel.scheduleConfig.observeAsState()
    val todayCourses = schedule
        .filter { scheduleConfig?.week in it.startWeek..it.endWeek }
        .filter { it.weekday == (scheduleConfig?.weekDay ?: 1) }
        .sortedBy { it.startTime }
    val calendar = Calendar.getInstance(Locale.CHINA)
    val currentMinutes = calendar.time.let {
        calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(96.n1 withNight 10.n1)
            .systemBarsPadding()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SquigglyUnderlinedText(
                text = buildAnnotatedString {
                    withStyle(style = MaterialTheme.typography.headlineLarge.toSpanStyle()) {
                        append("Hi, ")
                        withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                            append(user.name)
                        }
                    }
                }
            )
            IconButton(onClick = { navController.navigate("settings") }) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null
                )
            }
        }
        if (todayCourses.isNotEmpty()) {
            if (currentMinutes <= scheduleViewModel.getCourseTimeRangeInMinutes(todayCourses.last()).last) {
                TodayCourses(
                    scheduleViewModel = scheduleViewModel,
                    todayCourses = todayCourses,
                    currentMinutes = currentMinutes,
                    navController = navController
                )
            } else {
                EmptyCourse(navController = navController)
            }
        } else {
            EmptyCourse(navController = navController)
        }
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            mainAxisAlignment = MainAxisAlignment.SpaceEvenly,
            crossAxisSpacing = 16.dp
        ) {
            FunctionalButton(
                stringId = R.string.title_schedule,
                iconId = R.mipmap.schedule_on,
                onClick = { navController.navigate("schedule") }
            )
            FunctionalButton(
                stringId = R.string.grade,
                iconId = R.mipmap.score,
                onClick = { navController.navigate("grade") }
            )
            FunctionalButton(
                stringId = R.string.phone_book,
                iconId = R.mipmap.telephone_directory,
                onClick = { navController.navigate("phone_book") }
            )
            FunctionalButton(
                stringId = R.string.exam,
                iconId = R.mipmap.examination_room,
                onClick = { navController.navigate("exam") }
            )
        }
        FlowRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            mainAxisSpacing = 16.dp,
            crossAxisSpacing = 16.dp
        ) {
            CampusCard(
                balance = discoveryViewModel.balance,
                transitionBalance = discoveryViewModel.transitionBalance
            )
            BathroomOpenStates(discoveryViewModel = discoveryViewModel)
        }
    }
}
