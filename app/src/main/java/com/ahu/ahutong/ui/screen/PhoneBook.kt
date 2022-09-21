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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ahu.ahutong.R
import com.ahu.ahutong.ui.page.state.TelDirectoryViewModel
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight

// TODO: implement search query & inclusive data
@Composable
fun PhoneBook() {
    var selectedCategory by rememberSaveable { mutableStateOf("师生综合服务大厅") }
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
        Categories(
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it }
        )
        Telephones(selectedCategory = selectedCategory)
    }
}

@Composable
private fun Categories(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = TelDirectoryViewModel.TelBook.keys.toList(),
            key = { it }
        ) { name ->
            val isSelected = selectedCategory == name
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
                        .clickable { onCategorySelected(name) }
                        .padding(16.dp, 8.dp),
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

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
private fun Telephones(selectedCategory: String) {
    val context = LocalContext.current
    val density = LocalDensity.current
    AnimatedContent(
        targetState = selectedCategory,
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
    ) { category ->
        Column(
            modifier = Modifier
                .padding(16.dp, 24.dp)
                .clip(RoundedCornerShape(32.dp)),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            TelDirectoryViewModel.TelBook.getValue(category).forEach {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(100.n1 withNight 20.n1)
                        .combinedClickable( // TODO: add user tip
                            onDoubleClick = {
                                val dialIntent =
                                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:0551-${it.tel2}")) // 跳转到拨号界面，同时传递电话号码
                                context.startActivity(dialIntent)
                            }
                        ) {
                            val dialIntent =
                                Intent(Intent.ACTION_DIAL, Uri.parse("tel:0551-${it.tel}")) // 跳转到拨号界面，同时传递电话号码
                            context.startActivity(dialIntent)
                        }
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = it.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when {
                            it.tel != null && it.tel2 != null && it.tel == it.tel2 -> {
                                Tel(tel = it.tel)
                            }
                            it.tel != null && it.tel2 == null -> {
                                Tel(tel = it.tel, campus = "磬苑")
                            }
                            it.tel == null && it.tel2 != null -> {
                                Tel(tel = it.tel2, campus = "龙河")
                            }
                            it.tel != null && it.tel2 != null && it.tel != it.tel2 -> {
                                Tel(tel = it.tel, campus = "磬苑")
                                Tel(tel = it.tel2, campus = "龙河")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Tel(
    tel: String,
    campus: String? = null
) {
    campus?.let {
        Text(
            text = it,
            modifier = Modifier
                .padding(4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(90.a1 withNight 30.n1)
                .padding(8.dp, 2.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
    Text(
        text = tel,
        color = 50.n1 withNight 80.n1,
        style = MaterialTheme.typography.bodyLarge
    )
}
