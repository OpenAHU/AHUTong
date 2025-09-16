package com.ahu.ahutong.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahu.ahutong.R
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.PreferencesViewModel
import com.kyant.monet.a1
import com.kyant.monet.a2
import com.kyant.monet.n1
import com.kyant.monet.n2
import com.kyant.monet.withNight
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.format.TextStyle


@Composable
fun Preferences() {

    val preferencesViewModel: PreferencesViewModel = hiltViewModel()

    val showQRCode by preferencesViewModel.showQRCode.collectAsState()

    val isShowAllCourse by preferencesViewModel.isShowAllCourse.collectAsState()

    val switchColor = SwitchDefaults.colors(
        checkedThumbColor = 80.a1 withNight 80.n1,
        uncheckedThumbColor = 80.a2 withNight 40.n2,
        checkedTrackColor = (80.a1 withNight 80.n1).copy(alpha = 0.5f),
        uncheckedTrackColor = (80.a2 withNight 40.n2).copy(alpha = 0.5f),
    )

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
                    .background(100.n1 withNight 20.n1)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "主页",style = MaterialTheme.typography.headlineSmall)
            Row(
                modifier = Modifier
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    {
                        preferencesViewModel.setShowQRCode(!preferencesViewModel.showQRCode.value)
                    }
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "主页默认显示支付二维码")
                Switch(
                    checked = showQRCode,
                    onCheckedChange = null,
                    colors = switchColor
                )
            }



        }

        Column(
            modifier =
                Modifier
                    .clip(SmoothRoundedCornerShape(16.dp))
                    .background(100.n1 withNight 20.n1)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "课表", style = MaterialTheme.typography.headlineSmall)
            Row(
                modifier = Modifier
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    {
                        preferencesViewModel.setIsShowAllCourse(!preferencesViewModel.isShowAllCourse.value)
                    }
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "显示非本周课程")
                Switch(
                    checked = isShowAllCourse,
                    onCheckedChange = null,
                    colors = switchColor
                )
            }
        }
    }
}