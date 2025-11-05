package com.ahu.ahutong.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ahu.ahutong.R
import com.ahu.ahutong.ui.components.LiquidToggle
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.PreferencesViewModel
import com.kyant.backdrop.backdrops.rememberCanvasBackdrop
import com.kyant.monet.n1
import com.kyant.monet.withNight


@Composable
fun Preferences() {

    val preferencesViewModel: PreferencesViewModel = hiltViewModel()

    val showQRCode by preferencesViewModel.showQRCode.collectAsState()
    val isShowAllCourse by preferencesViewModel.isShowAllCourse.collectAsState()
    val useLiquidGlass by preferencesViewModel.useLiquidGlass.collectAsState()

    val cardColor = 100.n1 withNight 20.n1
    val backdrop = rememberCanvasBackdrop { drawRect(cardColor) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .systemBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.preferences),
            modifier = Modifier.padding(24.dp, 32.dp),
            style = MaterialTheme.typography.headlineLarge
        )
        Column(
            modifier =
                Modifier
                    .clip(SmoothRoundedCornerShape(16.dp))
                    .background(cardColor)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "主页", style = MaterialTheme.typography.headlineSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "主页默认显示支付二维码")
                LiquidToggle(
                    selected = { showQRCode },
                    onSelect = { preferencesViewModel.setShowQRCode(!preferencesViewModel.showQRCode.value) },
                    backdrop = backdrop
                )
            }
        }

        Column(
            modifier =
                Modifier
                    .clip(SmoothRoundedCornerShape(16.dp))
                    .background(cardColor)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "课表", style = MaterialTheme.typography.headlineSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "显示非本周课程")
                LiquidToggle(
                    selected = { isShowAllCourse },
                    onSelect = { preferencesViewModel.setIsShowAllCourse(!preferencesViewModel.isShowAllCourse.value) },
                    backdrop = backdrop
                )
            }
        }

        Column(
            modifier =
                Modifier
                    .clip(SmoothRoundedCornerShape(16.dp))
                    .background(cardColor)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "液态玻璃", style = MaterialTheme.typography.headlineSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "启用液态玻璃效果")
                LiquidToggle(
                    selected = { useLiquidGlass },
                    onSelect = { preferencesViewModel.setUseLiquidGlass(!preferencesViewModel.useLiquidGlass.value) },
                    backdrop = backdrop
                )
            }
        }
    }
}