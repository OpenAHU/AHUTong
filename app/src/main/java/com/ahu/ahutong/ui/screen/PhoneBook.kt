package com.ahu.ahutong.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahu.ahutong.R
import com.ahu.ahutong.ui.page.state.TelDirectoryViewModel
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight

// TODO: implement search query & inclusive data
@Composable
fun PhoneBook(telDirectoryViewModel: TelDirectoryViewModel = viewModel()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(96.n1 withNight 10.n1)
            .systemBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.phone_book),
            modifier = Modifier.padding(24.dp, 56.dp, 24.dp, 24.dp),
            style = MaterialTheme.typography.headlineLarge
        )
        Categories(telDirectoryViewModel = telDirectoryViewModel)
        Telephones(telDirectoryViewModel = telDirectoryViewModel)
    }
}

@Composable
private fun Categories(telDirectoryViewModel: TelDirectoryViewModel = viewModel()) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = TelDirectoryViewModel.TelBook.keys.toList(),
            key = { it }
        ) { name ->
            val isSelected = telDirectoryViewModel.selectedCategory == name
            CompositionLocalProvider(
                LocalIndication provides rememberRipple(
                    color = if (isSelected) 100.n1 withNight 0.n1
                    else 0.n1 withNight 100.n1
                )
            ) {
                Text(
                    text = name,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            animateColorAsState(
                                targetValue = if (isSelected) 40.a1 withNight 90.a1
                                else 92.a1 withNight 20.n1
                            ).value
                        )
                        .clickable { telDirectoryViewModel.selectedCategory = name }
                        .padding(24.dp, 16.dp),
                    color = animateColorAsState(
                        targetValue = if (isSelected) 100.n1 withNight 0.n1
                        else 0.n1 withNight 100.n1
                    ).value,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun Telephones(telDirectoryViewModel: TelDirectoryViewModel = viewModel()) {
    val context = LocalContext.current
    val density = LocalDensity.current
    AnimatedContent(
        targetState = telDirectoryViewModel.selectedCategory,
        transitionSpec = {
            with(density) {
                slideInVertically(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessLow,
                        visibilityThreshold = IntOffset.VisibilityThreshold
                    )
                ) { 48.dp.roundToPx() } +
                    scaleIn(initialScale = 0.92f) +
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) with
                    slideOutVertically(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessLow,
                        visibilityThreshold = IntOffset.VisibilityThreshold
                    )
                ) { 48.dp.roundToPx() } + scaleOut(targetScale = 0.92f) +
                    fadeOut(animationSpec = tween(90))
            }
        }
    ) { selectedCategory ->
        Column(
            modifier = Modifier
                .padding(16.dp, 24.dp)
                .clip(RoundedCornerShape(32.dp)),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            TelDirectoryViewModel.TelBook.getValue(selectedCategory).forEach {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(98.n1 withNight 20.n1)
                        .clickable {
                            val dialIntent =
                                Intent(Intent.ACTION_DIAL, Uri.parse("tel:${it.tel}")) // 跳转到拨号界面，同时传递电话号码
                            context.startActivity(dialIntent)
                        }
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = it.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = it.tel,
                        color = 50.n1 withNight 80.n1,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
