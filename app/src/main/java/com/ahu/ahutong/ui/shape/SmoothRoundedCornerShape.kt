package com.ahu.ahutong.ui.shape

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousRoundedRectangle

/**
 * Author: Kyant
 */
fun SmoothRoundedCornerShape(size: Dp) = ContinuousRoundedRectangle(size)

fun SmoothRoundedCornerShape(
    corner: CornerSize,
) = ContinuousRoundedRectangle(corner, corner, corner, corner)

fun SmoothRoundedCornerShape(
    topStart: Dp = 0.dp,
    topEnd: Dp = 0.dp,
    bottomEnd: Dp = 0.dp,
    bottomStart: Dp = 0.dp
) = ContinuousRoundedRectangle(
    topStart = CornerSize(topStart),
    topEnd = CornerSize(topEnd),
    bottomEnd = CornerSize(bottomEnd),
    bottomStart = CornerSize(bottomStart)
)
