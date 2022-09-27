package com.ahu.ahutong.ui.screen.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.ui.page.state.ScheduleViewModel
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CourseCard(
    scheduleViewModel: ScheduleViewModel = viewModel(),
    todayCourses: SnapshotStateList<Course>,
    current: Int,
    navController: NavHostController
) {
    val scope = rememberCoroutineScope()
    val draggedFraction = remember { Animatable(0f) }
    val state = rememberDraggableState {
        scope.launch {
            draggedFraction.snapTo((draggedFraction.value - it / 500f).coerceIn(0f..todayCourses.size - 1f))
        }
    }
    var autoScrollLocked by remember { mutableStateOf(false) }
    scheduleViewModel.findCurrentTimeByMinutes(current)?.let { time ->
        todayCourses
            .indexOfLast { time >= it.startTime + it.length - 1 }
            .takeIf { it != -1 }
            ?.let {
                if (!autoScrollLocked) {
                    scope.launch {
                        draggedFraction.snapTo(it.toFloat())
                        autoScrollLocked = true
                    }
                }
            }
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(90.a1 withNight 30.n1)
                .pointerInput(Unit) {
                    detectTapGestures {
                        navController.navigate("schedule")
                    }
                }
                .draggable(state = state, orientation = Orientation.Horizontal, onDragStopped = {
                    draggedFraction.animateTo(
                        draggedFraction.value
                            .roundToInt()
                            .toFloat()
                    )
                })
        ) {
            todayCourses.getOrNull(draggedFraction.value.roundToInt())?.let { course ->
                val range = scheduleViewModel.getCourseTimeRangeInMinutes(course)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(
                            animateFloatAsState(
                                targetValue = if (current in range) {
                                    (current.toFloat() - range.first) / (range.last.toFloat() - range.first)
                                } else 0f
                            ).value
                        )
                        .fillMaxHeight()
                        .background(50.a1 withNight 80.a1)
                )
                Column(
                    modifier = Modifier.padding(24.dp, 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // TODO: fix font color
                    Text(
                        text = course.name,
                        modifier = Modifier
                            .offset(
                                x = 48.dp * if (draggedFraction.value.mod(1f) < 0.5f) {
                                    -draggedFraction.value.mod(1f)
                                } else 1f - draggedFraction.value.mod(1f)
                            )
                            .alpha(
                                animateFloatAsState(
                                    targetValue = if (draggedFraction.value.mod(1f) < 0.5f) {
                                        1f - draggedFraction.value.mod(1f) * 2
                                    } else draggedFraction.value.mod(1f) * 2 - 1f
                                ).value
                            ),
                        color = if (current in range) 100.n1 else 0.n1 withNight 100.n1,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedContent(targetState = course) {
                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(95.a1 withNight 40.n1)
                                    .padding(8.dp, 2.dp)
                                    .animateContentSize(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Timer,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${it.startTime} - ${it.startTime + it.length - 1}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        AnimatedContent(targetState = course) {
                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(95.a1 withNight 40.n1)
                                    .padding(8.dp, 2.dp)
                                    .animateContentSize(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Navigation,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = it.location,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
        ) {
            (0 until todayCourses.size).forEach {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (it == draggedFraction.value.roundToInt()) 50.a1 withNight 80.a1
                            else 90.n1 withNight 30.n1
                        )
                )
            }
        }
    }
}
