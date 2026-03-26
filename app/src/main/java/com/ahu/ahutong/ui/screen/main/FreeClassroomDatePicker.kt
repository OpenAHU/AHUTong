package com.ahu.ahutong.ui.screen.main

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDatePickerDialog(
    initialDate: LocalDate,
    minDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= minDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            }
        }
    )

    val colors = DatePickerDefaults.colors(
        containerColor = 100.n1 withNight 20.n1,
        titleContentColor = 10.n1 withNight 90.n1,
        headlineContentColor = 10.n1 withNight 90.n1,
        weekdayContentColor = 40.n1 withNight 60.n1,
        subheadContentColor = 40.n1 withNight 60.n1,
        yearContentColor = 40.n1 withNight 60.n1,
        currentYearContentColor = 10.n1 withNight 90.n1,
        selectedYearContentColor = 0.n1,
        selectedYearContainerColor = 90.a1,
        dayContentColor = 10.n1 withNight 90.n1,
        disabledDayContentColor = 60.n1 withNight 40.n1,
        selectedDayContentColor = 0.n1,
        disabledSelectedDayContentColor = 60.n1 withNight 40.n1,
        selectedDayContainerColor = 90.a1 ,
        disabledSelectedDayContainerColor = 90.a1,
        todayContentColor = 10.n1 withNight 90.n1,
        todayDateBorderColor = 90.a1,
        navigationContentColor = 10.n1 withNight 90.n1
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onDateSelected(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = 10.n1 withNight 90.n1
                )
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = 10.n1 withNight 90.n1
                )
            ) {
                Text("取消")
            }
        },
        colors = colors
    ) {
        DatePicker(
            state = datePickerState,
            colors = colors
        )
    }
}
