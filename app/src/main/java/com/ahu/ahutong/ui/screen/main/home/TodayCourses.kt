package com.ahu.ahutong.ui.screen.main.home

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.ScheduleViewModel
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sign

@Composable
fun TodayCourses(
    todayCourses: List<Course>,
    currentMinutes: Int,
    navController: NavHostController
) {
    val scope = rememberCoroutineScope()
    val selectedCourseIndex = todayCourses.indexOfFirst {
        val range = ScheduleViewModel.getCourseTimeRangeInMinutes(it)
        if (currentMinutes in range) true
        else currentMinutes < range.first
    }
    val draggedFraction = remember { Animatable(selectedCourseIndex.toFloat()) }
    val state = rememberDraggableState {
        scope.launch {
            draggedFraction.snapTo((draggedFraction.value - it / 500f).coerceIn(0f..todayCourses.size - 1f))
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(SmoothRoundedCornerShape(32.dp))
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        todayCourses.getOrNull(draggedFraction.value.roundToInt())?.let { course ->
            val range = ScheduleViewModel.getCourseTimeRangeInMinutes(course)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (currentMinutes in range) "正在上课"
                    else {
                        val distance = range.first - currentMinutes
                        if (distance >= 0) {
                            val hour = distance / 60
                            val minutes = distance - hour * 60
                            if (hour > 0) "还有 $hour 小时 $minutes 分钟"
                            else "还有 $minutes 分钟"
                        } else "已经结束"
                    },
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)) {
                    todayCourses.indices.forEach {
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
            Box(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .clip(SmoothRoundedCornerShape(32.dp))
                    .background(90.a1 withNight 30.n1)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            navController.navigate("schedule")
                        }
                    }
                    .draggable(
                        state = state,
                        orientation = Orientation.Horizontal,
                        onDragStopped = {
                            draggedFraction.animateTo(draggedFraction.value.roundToInt().toFloat())
                        }
                    )
            ) {
                val ongoingFraction = if (currentMinutes in range) {
                    (currentMinutes.toFloat() - range.first) / (range.last.toFloat() - range.first)
                } else 0f
                var ongoingWidth by rememberSaveable { mutableStateOf(0) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animateFloatAsState(targetValue = ongoingFraction).value)
                        .fillMaxHeight()
                        .onSizeChanged { ongoingWidth = it.width }
                        .background(50.a1 withNight 80.a1)
                )
                Column(
                    modifier = Modifier.padding(24.dp, 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    val partialFraction = draggedFraction.value.mod(1f)
                    val offsetX = 56.dp * (partialFraction.roundToInt() - partialFraction)
                    val alpha = sign(partialFraction - 0.5f) * (partialFraction * 2 - 1f)
                    Box(
                        modifier = Modifier
                            .offset(x = offsetX)
                            .alpha(alpha)
                    ) {
                        Text(
                            text = course.name,
                            color = if (currentMinutes in range) 0.n1 else 0.n1 withNight 100.n1,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = course.name,
                            modifier = Modifier.clip(
                                object : Shape {
                                    override fun createOutline(
                                        size: Size,
                                        layoutDirection: LayoutDirection,
                                        density: Density
                                    ) = Outline.Rectangle(
                                        with(density) {
                                            size.toRect().copy(right = ongoingWidth - 24.dp.toPx() - offsetX.toPx())
                                        }
                                    )
                                }
                            ),
                            color = if (currentMinutes in range) 100.n1 else 0.n1 withNight 100.n1,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                                text = "${course.startTime} - ${course.startTime + course.length - 1}",
                                maxLines = 1,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
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
                                text = course.location
                                    .replace("博学", "博")
                                    .replace("楼", "")
                                    .replace("育场", ""),
                                maxLines = 1,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
