package com.ahu.ahutong.ui.screen.setup

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahu.ahutong.R
import com.ahu.ahutong.ui.state.ScheduleViewModel
import com.kyant.capsule.ContinuousCapsule
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight

@Composable
fun Info(
    scheduleViewModel: ScheduleViewModel = viewModel(),
    onSetup: () -> Unit
) {
    val scheduleConfig by scheduleViewModel.scheduleConfig.observeAsState()
    var schoolYear by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(scheduleViewModel.schoolYear))
    }
    var schoolTerm by rememberSaveable { mutableStateOf(scheduleViewModel.schoolTerm) }
    var currentWeek by rememberSaveable(scheduleConfig?.week, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(scheduleConfig?.week?.toString() ?: "1"))
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .systemBarsPadding()
                .padding(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.fill_in_info),
                modifier = Modifier.padding(24.dp, 32.dp),
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
                    .padding(horizontal = 16.dp)
                    .clip(ContinuousCapsule)
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
                text = stringResource(id = R.string.school_term),
                modifier = Modifier.padding(horizontal = 24.dp),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            LazyRow(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(ContinuousCapsule)
                    .background(100.n1 withNight 20.n1),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(arrayOf("1", "2", "3")) {
                    val isSelected = it == schoolTerm
                    Text(
                        text = it,
                        modifier = Modifier
                            .clip(ContinuousCapsule)
                            .background(if (isSelected) 90.a1 else Color.Unspecified)
                            .clickable { schoolTerm = it }
                            .padding(16.dp, 8.dp),
                        color = if (isSelected) 0.n1 else Color.Unspecified,
                        style = MaterialTheme.typography.titleMedium
                    )
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
                    .padding(horizontal = 16.dp)
                    .clip(ContinuousCapsule)
                    .background(100.n1 withNight 20.n1),
                textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    scheduleViewModel.saveTime(
                        schoolYear = schoolYear.text,
                        schoolTerm = schoolTerm,
                        week = currentWeek.text.toIntOrNull() ?: 1
                    )
                    onSetup()
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
        CompositionLocalProvider(LocalIndication provides ripple(color = 0.n1)) {
            Text(
                text = stringResource(id = R.string.ok),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .clip(ContinuousCapsule)
                    .background(90.a1 withNight 85.a1)
                    .clickable(
                        role = Role.Button,
                        onClick = {
                            scheduleViewModel.saveTime(
                                schoolYear = schoolYear.text,
                                schoolTerm = schoolTerm,
                                week = currentWeek.text.toIntOrNull() ?: 1
                            )
                            onSetup()
                        }
                    )
                    .padding(24.dp, 16.dp),
                color = 0.n1,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
