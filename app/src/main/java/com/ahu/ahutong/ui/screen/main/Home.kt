package com.ahu.ahutong.ui.screen.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.schedule.CurrentWeekResolver
import androidx.navigation.NavHostController
import com.ahu.ahutong.data.debug.DebugClock
import com.ahu.ahutong.ui.screen.main.home.AtAGlance
import com.ahu.ahutong.ui.screen.main.home.HomeWidgetDragOverlay
import com.ahu.ahutong.ui.screen.main.home.HomeWidgetLibrarySheet
import com.ahu.ahutong.ui.screen.main.home.HomeWidgetRegistry
import com.ahu.ahutong.ui.screen.main.home.HomeWidgetSlotLayout
import com.ahu.ahutong.ui.screen.main.home.TodayCourseList
import com.ahu.ahutong.ui.state.DiscoveryViewModel
import com.ahu.ahutong.ui.state.ScheduleViewModel
import kotlinx.coroutines.delay
import kotlin.math.hypot
import kotlin.math.roundToInt

private const val HOME_REFRESH_INTERVAL_MS = 30_000L

private data class ActiveHomeWidgetDrag(
    val widgetId: String,
    val sourceSlot: Int?,
    val topLeft: Offset,
    val size: IntSize
) {
    val center: Offset
        get() = topLeft + Offset(size.width / 2f, size.height / 2f)
}

@Composable
fun Home(
    discoveryViewModel: DiscoveryViewModel = viewModel(),
    scheduleViewModel: ScheduleViewModel = viewModel(),
    navController: NavHostController
) {
    val density = LocalDensity.current
    val schedule = scheduleViewModel.schedule.observeAsState().value?.getOrNull() ?: emptyList()
    val scheduleConfig by scheduleViewModel.scheduleConfig.observeAsState()
    val effectiveScheduleConfig = CurrentWeekResolver.resolveLocalConfig()?.config ?: scheduleConfig
    val currentWeek = effectiveScheduleConfig?.week ?: 1
    val todayCourses = schedule
        .filter { effectiveScheduleConfig?.week in it.startWeek..it.endWeek }
        .filter { it.weekday == (effectiveScheduleConfig?.weekDay ?: 1) }
        .filter {
            if (currentWeek in it.weekIndexes) {
                true
            } else {
                currentWeek % 2 == it.startWeek % 2
            }
        }
        .sortedBy { it.startTime }
    var currentMinutes by remember { mutableIntStateOf(DebugClock.currentMinutes()) }
    var isEditingHome by remember { mutableStateOf(false) }
    var homeWidgetSlots by remember {
        mutableStateOf(normalizeHomeWidgetSlots(AHUCache.getHomeWidgetSlots()))
    }
    val slotBounds = remember { mutableStateMapOf<Int, Rect>() }
    var libraryBounds by remember { mutableStateOf<Rect?>(null) }
    var rootTopLeft by remember { mutableStateOf(Offset.Zero) }
    var activeDrag by remember { mutableStateOf<ActiveHomeWidgetDrag?>(null) }
    val dropSlopPx = with(density) { 48.dp.toPx() }
    val highlightedSlot = activeDrag?.let {
        findHomeWidgetDropSlot(
            drag = it,
            slots = homeWidgetSlots,
            slotBounds = slotBounds,
            dropSlopPx = dropSlopPx
        )
    }

    fun saveHomeWidgetSlots(slots: List<String?>) {
        val normalizedSlots = normalizeHomeWidgetSlots(slots)
        homeWidgetSlots = normalizedSlots
        AHUCache.saveHomeWidgetSlots(normalizedSlots)
    }

    fun startDrag(widgetId: String, sourceSlot: Int?, bounds: Rect) {
        isEditingHome = true
        activeDrag = ActiveHomeWidgetDrag(
            widgetId = widgetId,
            sourceSlot = sourceSlot,
            topLeft = bounds.topLeft,
            size = IntSize(
                width = bounds.width.roundToInt().coerceAtLeast(1),
                height = bounds.height.roundToInt().coerceAtLeast(1)
            )
        )
    }

    fun stopDrag() {
        val drag = activeDrag ?: return
        val dragCenter = drag.center
        val nextSlots = homeWidgetSlots.toMutableList()
        val targetSlot = findHomeWidgetDropSlot(
            drag = drag,
            slots = homeWidgetSlots,
            slotBounds = slotBounds,
            dropSlopPx = dropSlopPx
        )

        if (drag.sourceSlot != null && libraryBounds?.contains(dragCenter) == true) {
            nextSlots[drag.sourceSlot - 1] = null
            saveHomeWidgetSlots(nextSlots)
        } else if (targetSlot != null) {
            val targetIndex = targetSlot - 1
            val sourceSlot = drag.sourceSlot

            if (sourceSlot == null) {
                if (nextSlots[targetIndex] == null) {
                    nextSlots[targetIndex] = drag.widgetId
                    saveHomeWidgetSlots(nextSlots)
                }
            } else if (sourceSlot != targetSlot) {
                val sourceIndex = sourceSlot - 1
                val targetWidget = nextSlots[targetIndex]
                nextSlots[targetIndex] = drag.widgetId
                nextSlots[sourceIndex] = targetWidget
                saveHomeWidgetSlots(nextSlots)
            }
        }

        activeDrag = null
    }

    fun addWidgetToFirstEmptySlot(widgetId: String) {
        val nextSlots = homeWidgetSlots.toMutableList()
        val targetIndex = nextSlots.indexOfFirst { it == null }
        if (targetIndex == -1 || widgetId in nextSlots) return
        nextSlots[targetIndex] = widgetId
        saveHomeWidgetSlots(nextSlots)
    }

    fun removeHomeWidget(slotIndex: Int) {
        val nextSlots = homeWidgetSlots.toMutableList()
        if (slotIndex !in 1..nextSlots.size) return
        nextSlots[slotIndex - 1] = null
        saveHomeWidgetSlots(nextSlots)
    }

    fun exitHomeEditMode() {
        activeDrag = null
        isEditingHome = false
    }

    BackHandler(enabled = isEditingHome) {
        exitHomeEditMode()
    }

    LaunchedEffect(Unit) {
        exitHomeEditMode()
        discoveryViewModel.loadActivityBean()

        repeat(2 - discoveryViewModel.visibilities.size) {
            delay(100)
            discoveryViewModel.visibilities += discoveryViewModel.visibilities.lastIndex + 1
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(HOME_REFRESH_INTERVAL_MS)
            currentMinutes = DebugClock.currentMinutes()
            discoveryViewModel.refreshCardBalance()
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            exitHomeEditMode()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { rootTopLeft = it.boundsInRoot().topLeft }
            .pointerInput(isEditingHome) {
                if (isEditingHome) {
                    awaitEachGesture {
                        val down = awaitFirstDown(
                            requireUnconsumed = false,
                            pass = PointerEventPass.Final
                        )
                        val start = down.position
                        var shouldExit = !down.isConsumed
                        var waitingForUp = true
                        while (waitingForUp) {
                            val event = awaitPointerEvent(PointerEventPass.Final)
                            val change = event.changes.firstOrNull { it.id == down.id }
                            if (change == null) {
                                waitingForUp = false
                            } else {
                                if (change.isConsumed ||
                                    (change.position - start).getDistance() > viewConfiguration.touchSlop
                                ) {
                                    shouldExit = false
                                }
                                if (!change.pressed) {
                                    waitingForUp = false
                                }
                            }
                        }
                        if (shouldExit) {
                            exitHomeEditMode()
                        }
                    }
                } else {
                    detectTapGestures(
                        onLongPress = {
                            isEditingHome = true
                        }
                    )
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .systemBarsPadding()
                .padding(bottom = if (isEditingHome) 520.dp else 96.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            AtAGlance(
                todayCourses = todayCourses,
                currentMinutes = currentMinutes,
                navController = navController,
                enabled = !isEditingHome
            )
            if (todayCourses.isNotEmpty()) {
                SlideInContent(visible = 0 in discoveryViewModel.visibilities) {
                    TodayCourseList(
                        todayCourses = todayCourses,
                        currentMinutes = currentMinutes,
                        navController = navController,
                        enabled = !isEditingHome
                    )
                }
            }
            SlideInContent(visible = 1 in discoveryViewModel.visibilities) {
                HomeWidgetSlotLayout(
                    balance = discoveryViewModel.balance,
                    transitionBalance = discoveryViewModel.transitionBalance,
                    onRefreshBalance = discoveryViewModel::refreshCardBalance,
                    navController = navController,
                    slots = homeWidgetSlots,
                    isEditing = isEditingHome,
                    highlightedSlot = highlightedSlot,
                    draggingWidgetId = activeDrag?.widgetId,
                    onEnterEdit = { isEditingHome = true },
                    onHomeWidgetClick = ::removeHomeWidget,
                    onSlotPositioned = { slotIndex, bounds ->
                        slotBounds[slotIndex] = bounds
                    },
                    onHomeWidgetDragStarted = { widgetId, slotIndex, bounds ->
                        startDrag(widgetId, slotIndex, bounds)
                    },
                    onHomeWidgetDragged = { dragAmount ->
                        activeDrag = activeDrag?.let {
                            it.copy(topLeft = it.topLeft + dragAmount)
                        }
                    },
                    onHomeWidgetDragStopped = ::stopDrag
                )
            }
        }

        val placedWidgetIds = homeWidgetSlots.filterNotNull().toSet()
        val availableWidgets = HomeWidgetRegistry.widgets.filter { it.id !in placedWidgetIds }
        val isDraggingFromLibrary = activeDrag != null && activeDrag?.sourceSlot == null
        HomeWidgetLibrarySheet(
            visible = isEditingHome,
            hiddenDuringLibraryDrag = isDraggingFromLibrary,
            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
            availableWidgets = availableWidgets,
            onDismiss = {
                activeDrag = null
                isEditingHome = false
            },
            onBoundsChanged = { libraryBounds = it },
            onLibraryWidgetClick = ::addWidgetToFirstEmptySlot,
            onLibraryWidgetDragStarted = { widgetId, bounds ->
                startDrag(widgetId, null, bounds)
            },
            onLibraryWidgetDragged = { dragAmount ->
                activeDrag = activeDrag?.let {
                    it.copy(topLeft = it.topLeft + dragAmount)
                }
            },
            onLibraryWidgetDragStopped = ::stopDrag
        )

        activeDrag?.let { drag ->
            HomeWidgetRegistry.widgetById[drag.widgetId]?.let { spec ->
                val previewSlot = if (drag.sourceSlot == null) {
                    highlightedSlot
                        ?: homeWidgetSlots.indexOfFirst { it == null }
                            .takeIf { it != -1 }
                            ?.let { it + 1 }
                } else {
                    null
                }
                val previewBounds = previewSlot?.let { slotBounds[it] }
                val previewSize = previewBounds?.let {
                    IntSize(
                        width = it.width.roundToInt().coerceAtLeast(1),
                        height = it.height.roundToInt().coerceAtLeast(1)
                    )
                } ?: drag.size
                val previewTopLeft = if (previewBounds != null) {
                    drag.center - Offset(previewSize.width / 2f, previewSize.height / 2f)
                } else {
                    drag.topLeft
                }

                HomeWidgetDragOverlay(
                    spec = spec,
                    topLeft = previewTopLeft,
                    size = previewSize,
                    rootTopLeft = rootTopLeft
                )
            }
        }
    }
}

private fun normalizeHomeWidgetSlots(slots: List<String?>): List<String?> {
    val knownIds = HomeWidgetRegistry.widgetById.keys
    val seen = mutableSetOf<String>()
    return List(HomeWidgetRegistry.slotCount) { index ->
        val id = slots.getOrNull(index)?.takeIf { it in knownIds }
        if (id != null && seen.add(id)) id else null
    }
}

private fun findHomeWidgetDropSlot(
    drag: ActiveHomeWidgetDrag,
    slots: List<String?>,
    slotBounds: Map<Int, Rect>,
    dropSlopPx: Float
): Int? {
    val center = drag.center
    return slotBounds
        .filterKeys { it in 1..HomeWidgetRegistry.slotCount }
        .mapNotNull { (slotIndex, bounds) ->
            if (!bounds.expandedBy(dropSlopPx).contains(center)) return@mapNotNull null
            if (drag.sourceSlot == null && slots.getOrNull(slotIndex - 1) != null) return@mapNotNull null
            slotIndex to bounds.centerDistanceTo(center)
        }
        .minByOrNull { it.second }
        ?.first
}

private fun Rect.expandedBy(padding: Float): Rect {
    return Rect(
        left = left - padding,
        top = top - padding,
        right = right + padding,
        bottom = bottom + padding
    )
}

private fun Rect.centerDistanceTo(point: Offset): Float {
    return hypot(center.x - point.x, center.y - point.y)
}

@Composable
fun SlideInContent(
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
