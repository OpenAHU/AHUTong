package com.ahu.ahutong.ui.screen.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <S> AnimatedListContent(
    targetState: S,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.(targetState: S) -> Unit
) {
    val density = LocalDensity.current
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            with(density) {
                slideInVertically(
                    animationSpec = spring(
                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow,
                        visibilityThreshold = androidx.compose.ui.unit.IntOffset.VisibilityThreshold
                    )
                ) { 48.dp.roundToPx() } +
                    scaleIn(initialScale = 0.92f) +
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) with
                    slideOutVertically(
                    animationSpec = spring(
                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow,
                        visibilityThreshold = androidx.compose.ui.unit.IntOffset.VisibilityThreshold
                    )
                ) { 48.dp.roundToPx() } + scaleOut(targetScale = 0.92f) +
                    fadeOut(animationSpec = tween(90))
            }
        },
        modifier = modifier,
        content = content
    )
}
