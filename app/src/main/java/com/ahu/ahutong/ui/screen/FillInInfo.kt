package com.ahu.ahutong.ui.screen

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ahu.ahutong.R
import com.ahu.ahutong.ui.page.state.ScheduleViewModel
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight

@Composable
fun FillInInfo(
    scheduleViewModel: ScheduleViewModel = viewModel(),
    navController: NavHostController
) {
    val scheduleConfig by scheduleViewModel.scheduleConfig.observeAsState()
    var schoolYear by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(scheduleViewModel.schoolYear))
    }
    var schoolTerm by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(scheduleViewModel.schoolTerm))
    }
    var currentWeek by rememberSaveable(scheduleConfig?.week, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(scheduleConfig?.week?.toString() ?: "1"))
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(96.n1 withNight 10.n1)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .systemBarsPadding()
                .padding(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.fill_in_info),
                modifier = Modifier.padding(horizontal = 24.dp),
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier)
            Text(
                text = stringResource(id = R.string.school_year),
                modifier = Modifier.padding(horizontal = 24.dp),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            BasicTextField(
                value = schoolYear,
                onValueChange = { schoolYear = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(CircleShape)
                    .background(100.n1 withNight 20.n1),
                textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                cursorBrush = SolidColor(LocalContentColor.current)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(64.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    it()
                }
            }
            Text(
                text = stringResource(id = R.string.school_term),
                modifier = Modifier.padding(horizontal = 24.dp),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            BasicTextField(
                value = schoolTerm,
                onValueChange = { schoolTerm = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(CircleShape)
                    .background(100.n1 withNight 20.n1),
                textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                cursorBrush = SolidColor(LocalContentColor.current)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(64.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    it()
                }
            }
            Text(
                text = stringResource(id = R.string.current_week),
                modifier = Modifier.padding(horizontal = 24.dp),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            BasicTextField(
                value = currentWeek,
                onValueChange = { currentWeek = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(CircleShape)
                    .background(100.n1 withNight 20.n1),
                textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    scheduleViewModel.saveTime(
                        schoolYear = schoolYear.text,
                        schoolTerm = schoolTerm.text,
                        week = currentWeek.text.toIntOrNull() ?: 1
                    )
                    repeat(2) {
                        navController.popBackStack()
                    }
                }),
                singleLine = true,
                cursorBrush = SolidColor(LocalContentColor.current)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(64.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    it()
                }
            }
        }
        CompositionLocalProvider(LocalIndication provides rememberRipple(color = 0.n1)) {
            Text(
                text = stringResource(id = R.string.ok),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(90.a1 withNight 85.a1)
                    .clickable(
                        role = Role.Button,
                        onClick = {
                            scheduleViewModel.saveTime(
                                schoolYear = schoolYear.text,
                                schoolTerm = schoolTerm.text,
                                week = currentWeek.text.toIntOrNull() ?: 1
                            )
                            repeat(2) {
                                navController.popBackStack()
                            }
                        }
                    )
                    .padding(24.dp, 16.dp),
                color = 0.n1,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
