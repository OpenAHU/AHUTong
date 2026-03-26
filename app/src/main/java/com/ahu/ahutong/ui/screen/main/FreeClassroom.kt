package com.ahu.ahutong.ui.screen.main

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahu.ahutong.R
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.FreeClassroomViewModel
import com.kyant.capsule.ContinuousCapsule
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.text.style.TextAlign

@Composable
fun FreeClassroom(
    freeClassroomViewModel: FreeClassroomViewModel = viewModel()
) {
    val campusOptions = freeClassroomViewModel.campusOptions
    val selectedCampusId by freeClassroomViewModel.selectedCampusId.collectAsState()
    val buildings by freeClassroomViewModel.buildings.collectAsState()
    val selectedBuildingIds by freeClassroomViewModel.selectedBuildingIds.collectAsState()
    val selectedUnits by freeClassroomViewModel.selectedUnits.collectAsState()
    val startDate by freeClassroomViewModel.startDate.collectAsState()
    val endDate by freeClassroomViewModel.endDate.collectAsState()
    val isLoadingBuildings by freeClassroomViewModel.isLoadingBuildings.collectAsState()
    val isSearching by freeClassroomViewModel.isSearching.collectAsState()
    val rooms by freeClassroomViewModel.freeRooms.collectAsState()
    val errorMessage by freeClassroomViewModel.errorMessage.collectAsState()
    val context = LocalContext.current
    var isFilterCollapsed by rememberSaveable { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            freeClassroomViewModel.errorMessage.value = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .systemBarsPadding()
            .padding(bottom = 96.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(id = R.string.free_classroom),
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp, 32.dp),
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        AnimatedVisibility(
            visible = !isFilterCollapsed,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                FilterCard(title = "选择校区") {
                HorizontalChipRow {
                    items(campusOptions) { campus ->
                        FilterChip(
                            text = campus.name,
                            selected = selectedCampusId == campus.id,
                            onClick = { freeClassroomViewModel.selectCampus(campus.id) },
                            isSingle = true
                        )
                    }
                }
            }

            FilterCard(title = "选择教学楼") {
                when {
                    selectedCampusId == null -> {
                        Text(
                            text = "请先选择校区",
                            style = MaterialTheme.typography.bodyMedium,
                            color = 50.n1 withNight 80.n1
                        )
                    }

                    isLoadingBuildings -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = 90.a1
                        )
                    }

                    buildings.isEmpty() -> {
                        Text(
                            text = "当前校区暂无教学楼",
                            style = MaterialTheme.typography.bodyMedium,
                            color = 50.n1 withNight 80.n1
                        )
                    }

                    else -> {
                        HorizontalChipRow {
                            items(buildings) { building ->
                                FilterChip(
                                    text = building.nameZh,
                                    selected = building.id in selectedBuildingIds,
                                    onClick = { freeClassroomViewModel.toggleBuilding(building.id) },
                                    isSingle = false
                                )
                            }
                        }
                    }
                }
            }

            FilterCard(
                title = "选择节次",
                trailingHeader = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShortcutChip(
                            text = "上午",
                            selected = (1..5).all { it in selectedUnits },
                            onClick = { freeClassroomViewModel.toggleUnitsRange(1, 5) }
                        )
                        ShortcutChip(
                            text = "下午",
                            selected = (6..10).all { it in selectedUnits },
                            onClick = { freeClassroomViewModel.toggleUnitsRange(6, 10) }
                        )
                        ShortcutChip(
                            text = "晚上",
                            selected = (11..13).all { it in selectedUnits },
                            onClick = { freeClassroomViewModel.toggleUnitsRange(11, 13) }
                        )
                    }
                }
            ) {
                HorizontalChipRow {
                    items((1..13).toList()) { unit ->
                        FilterChip(
                            text = "${unit}节",
                            selected = unit in selectedUnits,
                            onClick = { freeClassroomViewModel.toggleUnit(unit) },
                            isSingle = false
                        )
                    }
                }
                Text(
                    text = "未选择节次时，默认按 1-13 节查询",
                    style = MaterialTheme.typography.bodySmall,
                    color = 50.n1 withNight 80.n1
                )
            }

            FilterCard(
                title = "选择日期",
                trailingHeader = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShortcutChip(
                            text = "今天",
                            selected = startDate == LocalDate.now() && endDate == LocalDate.now(),
                            onClick = { freeClassroomViewModel.setDateRange(LocalDate.now(), LocalDate.now()) }
                        )
                        ShortcutChip(
                            text = "明天",
                            selected = startDate == LocalDate.now().plusDays(1) && endDate == LocalDate.now().plusDays(1),
                            onClick = { freeClassroomViewModel.setDateRange(LocalDate.now().plusDays(1), LocalDate.now().plusDays(1)) }
                        )
                    }
                }
            ) {
                if (showStartDatePicker) {
                    MyDatePickerDialog(
                        initialDate = startDate,
                        minDate = LocalDate.now(),
                        onDateSelected = {
                            freeClassroomViewModel.setStartDate(it)
                            showStartDatePicker = false
                        },
                        onDismiss = { showStartDatePicker = false }
                    )
                }

                if (showEndDatePicker) {
                    MyDatePickerDialog(
                        initialDate = endDate,
                        minDate = startDate,
                        onDateSelected = {
                            freeClassroomViewModel.setEndDate(it)
                            showEndDatePicker = false
                        },
                        onDismiss = { showEndDatePicker = false }
                    )
                }

                HorizontalChipRow {
                    item {
                        FilterChip(
                            text = "开始: " + startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                            selected = true,
                            onClick = { showStartDatePicker = true },
                            isSingle = true
                        )
                    }
                    item {
                        FilterChip(
                            text = "结束: " + endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                            selected = true,
                            onClick = { showEndDatePicker = true },
                            isSingle = true
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        }

        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (isSearching) "查询中..." else "开始查询空闲教室",
                modifier = Modifier
                    .weight(1f)
                    .clip(ContinuousCapsule)
                    .background(if (selectedCampusId != null) 90.a1 else 70.n1 withNight 30.n1)
                    .clickable(enabled = selectedCampusId != null && !isSearching) {
                        freeClassroomViewModel.searchFreeRooms()
                        isFilterCollapsed = true
                    }
                    .padding(16.dp, 10.dp),
                color = if (selectedCampusId != null) 0.n1 else 60.n1 withNight 60.n1,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = { isFilterCollapsed = !isFilterCollapsed },
                modifier = Modifier
                    .clip(ContinuousCapsule)
                    .background(95.n1 withNight 25.n1)
            ) {
                Icon(
                    imageVector = if (isFilterCollapsed) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                    contentDescription = if (isFilterCollapsed) "展开筛选条件" else "收起筛选条件",
                    tint = 10.n1 withNight 90.n1
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clip(SmoothRoundedCornerShape(32.dp))
                .background(100.n1 withNight 20.n1)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "查询结果",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "共 ${rooms.size} 间",
                    style = MaterialTheme.typography.bodyMedium,
                    color = 40.n1 withNight 80.n1
                )
            }
            if (isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = 90.a1
                )
            } else if (rooms.isEmpty()) {
                Text(
                    text = "暂无数据，请先设置条件后查询",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                rooms.forEach { room ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SmoothRoundedCornerShape(20.dp))
                            .background(95.n1 withNight 25.n1)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = room.nameZh, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${room.building.nameZh}  ${room.floor}层  ${room.remark ?: ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = 40.n1 withNight 80.n1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HorizontalChipRow(
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
private fun FilterCard(
    title: String,
    trailingHeader: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(SmoothRoundedCornerShape(32.dp))
            .background(100.n1 withNight 20.n1)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            trailingHeader?.invoke()
        }
        content()
    }
}

@Composable
private fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    isSingle: Boolean
) {
    Text(
        text = text,
        modifier = Modifier
            .clip(ContinuousCapsule)
            .background(
                when {
                    selected -> 90.a1
                    isSingle -> 95.n1 withNight 25.n1
                    else -> 95.n1 withNight 30.n1
                }
            )
            .clickable { onClick() }
            .padding(14.dp, 8.dp),
        color = if (selected) 0.n1 else Color.Unspecified,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun ShortcutChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = text,
        modifier = Modifier
            .clip(ContinuousCapsule)
            .background(if (selected) 80.a1 else 95.n1 withNight 30.n1)
            .clickable { onClick() }
            .padding(14.dp, 8.dp),
        color = if (selected) 0.n1 else Color.Unspecified,
        style = MaterialTheme.typography.bodySmall
    )
}
