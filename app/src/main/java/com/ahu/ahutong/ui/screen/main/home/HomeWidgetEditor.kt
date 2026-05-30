package com.ahu.ahutong.ui.screen.main.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.roundToInt

@Composable
fun HomeWidgetSlotLayout(
    balance: Double,
    transitionBalance: Double,
    onRefreshBalance: () -> Unit,
    navController: NavHostController,
    slots: List<String?>,
    isEditing: Boolean,
    highlightedSlot: Int?,
    draggingWidgetId: String?,
    onEnterEdit: () -> Unit,
    onHomeWidgetClick: (slotIndex: Int) -> Unit,
    onSlotPositioned: (slotIndex: Int, bounds: Rect) -> Unit,
    onHomeWidgetDragStarted: (widgetId: String, slotIndex: Int, bounds: Rect) -> Unit,
    onHomeWidgetDragged: (Offset) -> Unit,
    onHomeWidgetDragStopped: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CampusCard(
                balance = balance,
                transitionBalance = transitionBalance,
                onRefreshBalance = onRefreshBalance,
                navController = navController,
                enabled = !isEditing
            )

            val showTopColumn = isEditing || slots.getOrNull(0) != null || slots.getOrNull(1) != null
            if (showTopColumn) {
                Column(
                    modifier = Modifier.width(132.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HomeWidgetSlot(
                        slotIndex = 1,
                        widgetId = slots.getOrNull(0),
                        isEditing = isEditing,
                        isHighlighted = highlightedSlot == 1,
                        isDragging = draggingWidgetId == slots.getOrNull(0),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(66.dp),
                        onEnterEdit = onEnterEdit,
                        onNavigate = { navController.navigate(it) },
                        onClick = onHomeWidgetClick,
                        onSlotPositioned = onSlotPositioned,
                        onDragStarted = onHomeWidgetDragStarted,
                        onDragged = onHomeWidgetDragged,
                        onDragStopped = onHomeWidgetDragStopped
                    )
                    HomeWidgetSlot(
                        slotIndex = 2,
                        widgetId = slots.getOrNull(1),
                        isEditing = isEditing,
                        isHighlighted = highlightedSlot == 2,
                        isDragging = draggingWidgetId == slots.getOrNull(1),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(66.dp),
                        onEnterEdit = onEnterEdit,
                        onNavigate = { navController.navigate(it) },
                        onClick = onHomeWidgetClick,
                        onSlotPositioned = onSlotPositioned,
                        onDragStarted = onHomeWidgetDragStarted,
                        onDragged = onHomeWidgetDragged,
                        onDragStopped = onHomeWidgetDragStopped
                    )
                }
            }
        }

        listOf(3 to 4, 5 to 6, 7 to 8).forEach { (leftSlot, rightSlot) ->
            val hasRowContent = slots.getOrNull(leftSlot - 1) != null || slots.getOrNull(rightSlot - 1) != null
            if (isEditing || hasRowContent) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HomeWidgetSlot(
                        slotIndex = leftSlot,
                        widgetId = slots.getOrNull(leftSlot - 1),
                        isEditing = isEditing,
                        isHighlighted = highlightedSlot == leftSlot,
                        isDragging = draggingWidgetId == slots.getOrNull(leftSlot - 1),
                        modifier = Modifier
                            .weight(1f)
                            .height(76.dp),
                        onEnterEdit = onEnterEdit,
                        onNavigate = { navController.navigate(it) },
                        onClick = onHomeWidgetClick,
                        onSlotPositioned = onSlotPositioned,
                        onDragStarted = onHomeWidgetDragStarted,
                        onDragged = onHomeWidgetDragged,
                        onDragStopped = onHomeWidgetDragStopped
                    )
                    HomeWidgetSlot(
                        slotIndex = rightSlot,
                        widgetId = slots.getOrNull(rightSlot - 1),
                        isEditing = isEditing,
                        isHighlighted = highlightedSlot == rightSlot,
                        isDragging = draggingWidgetId == slots.getOrNull(rightSlot - 1),
                        modifier = Modifier
                            .weight(1f)
                            .height(76.dp),
                        onEnterEdit = onEnterEdit,
                        onNavigate = { navController.navigate(it) },
                        onClick = onHomeWidgetClick,
                        onSlotPositioned = onSlotPositioned,
                        onDragStarted = onHomeWidgetDragStarted,
                        onDragged = onHomeWidgetDragged,
                        onDragStopped = onHomeWidgetDragStopped
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeWidgetSlot(
    slotIndex: Int,
    widgetId: String?,
    isEditing: Boolean,
    isHighlighted: Boolean,
    isDragging: Boolean,
    modifier: Modifier,
    onEnterEdit: () -> Unit,
    onNavigate: (route: String) -> Unit,
    onClick: (slotIndex: Int) -> Unit,
    onSlotPositioned: (slotIndex: Int, bounds: Rect) -> Unit,
    onDragStarted: (widgetId: String, slotIndex: Int, bounds: Rect) -> Unit,
    onDragged: (Offset) -> Unit,
    onDragStopped: () -> Unit
) {
    val spec = widgetId?.let { HomeWidgetRegistry.widgetById[it] }
    var bounds by remember { mutableStateOf<Rect?>(null) }
    val slotModifier = modifier.onGloballyPositioned {
        bounds = it.boundsInRoot()
        onSlotPositioned(slotIndex, it.boundsInRoot())
    }

    if (spec == null) {
        if (isEditing) {
            EmptyHomeWidgetSlot(
                isHighlighted = isHighlighted,
                modifier = slotModifier
            )
        } else {
            Spacer(modifier = modifier)
        }
        return
    }

    val dragModifier = if (isEditing) {
        Modifier
            .combinedClickable(
                onClick = { onClick(slotIndex) },
                onLongClick = {}
            )
            .pointerInput(spec.id, slotIndex) {
                var dragStarted = false
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        val itemBounds = bounds
                        if (itemBounds != null) {
                            dragStarted = true
                            onDragStarted(spec.id, slotIndex, itemBounds)
                        }
                    },
                    onDragEnd = {
                        if (dragStarted) {
                            onDragStopped()
                            dragStarted = false
                        }
                    },
                    onDragCancel = {
                        if (dragStarted) {
                            onDragStopped()
                            dragStarted = false
                        }
                    },
                    onDrag = { change, dragAmount ->
                        if (dragStarted) {
                            change.consume()
                            onDragged(dragAmount)
                        }
                    }
                )
            }
    } else {
        Modifier.combinedClickable(
            onClick = { onNavigate(spec.route) },
            onLongClick = onEnterEdit
        )
    }

    TextHomeWidgetCard(
        title = spec.title,
        isEditing = isEditing,
        isHighlighted = isHighlighted,
        modifier = slotModifier
            .alpha(if (isDragging) 0.35f else 1f),
        interactionModifier = dragModifier
    )
}

@Composable
private fun TextHomeWidgetCard(
    title: String,
    isEditing: Boolean,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier,
    interactionModifier: Modifier = Modifier
) {
    val shape = SmoothRoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .editModeMotion(isEditing)
            .clip(shape)
            .background(
                when {
                    isHighlighted -> 90.a1 withNight 35.a1
                    else -> 100.n1 withNight 20.n1
                }
            )
            .then(interactionModifier)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun EmptyHomeWidgetSlot(
    isHighlighted: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isHighlighted) 70.a1 withNight 80.a1 else 60.n1 withNight 45.n1
    Box(
        modifier = modifier
            .clip(SmoothRoundedCornerShape(24.dp))
            .background(if (isHighlighted) 95.a1 withNight 25.a1 else Color.Transparent)
            .dashedBorder(borderColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "拖到这里",
            color = 45.n1 withNight 70.n1,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun HomeWidgetLibrarySheet(
    visible: Boolean,
    hiddenDuringLibraryDrag: Boolean,
    modifier: Modifier = Modifier,
    availableWidgets: List<HomeWidgetSpec>,
    onDismiss: () -> Unit,
    onBoundsChanged: (Rect?) -> Unit,
    onLibraryWidgetClick: (widgetId: String) -> Unit,
    onLibraryWidgetDragStarted: (widgetId: String, bounds: Rect) -> Unit,
    onLibraryWidgetDragged: (Offset) -> Unit,
    onLibraryWidgetDragStopped: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(120)) + slideInVertically(animationSpec = tween(120)) { it },
        exit = fadeOut(animationSpec = tween(80)) + slideOutVertically(animationSpec = tween(80)) { it },
        modifier = modifier
            .fillMaxWidth()
            .zIndex(1f)
    ) {
        var sheetOffset by remember { mutableFloatStateOf(0f) }
        var sheetBounds by remember { mutableStateOf<Rect?>(null) }
        val currentSheetBounds by rememberUpdatedState(sheetBounds)
        val itemBounds = remember { mutableStateMapOf<String, Rect>() }
        LaunchedEffect(visible) {
            if (visible) sheetOffset = 0f
        }
        LaunchedEffect(availableWidgets) {
            itemBounds.keys.retainAll(availableWidgets.map { it.id }.toSet())
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp)
                .padding(bottom = 88.dp)
                .offset { IntOffset(0, sheetOffset.roundToInt()) }
                .graphicsLayer {
                    alpha = if (hiddenDuringLibraryDrag) 0f else 1f
                }
                .onGloballyPositioned {
                    sheetBounds = it.boundsInRoot()
                    onBoundsChanged(it.boundsInRoot())
                }
                .pointerInput(availableWidgets.map { it.id }) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val bounds = currentSheetBounds ?: return@awaitEachGesture
                        val downInRoot = bounds.topLeft + down.position
                        if (itemBounds.values.any { it.contains(downInRoot) }) {
                            return@awaitEachGesture
                        }

                        var dragging = true
                        while (dragging) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id }
                            if (change == null || !change.pressed) {
                                dragging = false
                            } else {
                                val dragAmount = change.positionChange()
                                if (dragAmount.y > 0f || sheetOffset > 0f) {
                                    change.consume()
                                    sheetOffset = (sheetOffset + dragAmount.y).coerceAtLeast(0f)
                                }
                            }
                        }

                        if (sheetOffset > 48f) {
                            onDismiss()
                        } else {
                            sheetOffset = 0f
                        }
                    }
                }
                .clip(SmoothRoundedCornerShape(32.dp))
                .background(100.n1 withNight 18.n1)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 42.dp, height = 4.dp)
                        .clip(SmoothRoundedCornerShape(4.dp))
                        .background(70.n1 withNight 55.n1)
                )

                Text(
                    text = "添加小工具",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "此操作仅隐藏图标，您随时可以从小工具中重新添加。",
                    color = 45.n1 withNight 75.n1,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Column(
                modifier = Modifier
                    .heightIn(max = 280.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (availableWidgets.isEmpty()) {
                    Text(
                        text = "所有小工具都已添加到首页",
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = 45.n1 withNight 75.n1,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableWidgets.forEach { spec ->
                            LibraryWidgetItem(
                                spec = spec,
                                onClick = { onLibraryWidgetClick(spec.id) },
                                onPositioned = { id, bounds -> itemBounds[id] = bounds },
                                onDragStarted = onLibraryWidgetDragStarted,
                                onDragged = onLibraryWidgetDragged,
                                onDragStopped = onLibraryWidgetDragStopped
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryWidgetItem(
    spec: HomeWidgetSpec,
    onClick: () -> Unit,
    onPositioned: (widgetId: String, bounds: Rect) -> Unit,
    onDragStarted: (widgetId: String, bounds: Rect) -> Unit,
    onDragged: (Offset) -> Unit,
    onDragStopped: () -> Unit
) {
    var bounds by remember { mutableStateOf<Rect?>(null) }
    Column(
        modifier = Modifier
            .width(88.dp)
            .clip(SmoothRoundedCornerShape(18.dp))
            .background(96.n1 withNight 28.n1)
            .onGloballyPositioned {
                bounds = it.boundsInRoot()
                onPositioned(spec.id, it.boundsInRoot())
            }
            .pointerInput(spec.id) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    val upBeforeLongPress = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                        waitForUpOrCancellation()
                    }

                    if (upBeforeLongPress != null) {
                        upBeforeLongPress.consume()
                        onClick()
                        return@awaitEachGesture
                    }

                    val itemBounds = bounds ?: return@awaitEachGesture
                    onDragStarted(spec.id, itemBounds)

                    var dragging = true
                    while (dragging) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id }
                        if (change == null || !change.pressed) {
                            dragging = false
                        } else {
                            val dragAmount = change.positionChange()
                            if (dragAmount != Offset.Zero) {
                                change.consume()
                                onDragged(dragAmount)
                            }
                        }
                    }
                    onDragStopped()
                }
            }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = spec.iconId),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = spec.tint
        )
        Text(
            text = spec.title,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun HomeWidgetDragOverlay(
    spec: HomeWidgetSpec,
    topLeft: Offset,
    size: IntSize,
    rootTopLeft: Offset
) {
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (topLeft.x - rootTopLeft.x).roundToInt(),
                    y = (topLeft.y - rootTopLeft.y).roundToInt()
                )
            }
            .size(
                width = with(density) { size.width.toDp() },
                height = with(density) { size.height.toDp() }
            )
            .graphicsLayer {
                shadowElevation = 18.dp.toPx()
                scaleX = 1.04f
                scaleY = 1.04f
            }
            .zIndex(2f)
    ) {
        TextHomeWidgetCard(
            title = spec.title,
            isEditing = false,
            isHighlighted = false,
            modifier = Modifier.matchParentSize()
        )
    }
}

@Composable
private fun Modifier.editModeMotion(isEditing: Boolean): Modifier {
    if (!isEditing) return this
    val transition = rememberInfiniteTransition(label = "home-widget-edit")
    val rotation by transition.animateFloat(
        initialValue = -0.8f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 120),
            repeatMode = RepeatMode.Reverse
        ),
        label = "home-widget-rotation"
    )
    return this.graphicsLayer {
        rotationZ = rotation
    }
}

private fun Modifier.dashedBorder(color: Color): Modifier {
    return drawBehind {
        drawRoundRect(
            color = color,
            cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
            style = Stroke(
                width = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    intervals = floatArrayOf(8.dp.toPx(), 6.dp.toPx())
                )
            )
        )
    }
}
