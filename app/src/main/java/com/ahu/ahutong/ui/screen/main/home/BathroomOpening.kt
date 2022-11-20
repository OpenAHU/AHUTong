package com.ahu.ahutong.ui.screen.main.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.DiscoveryViewModel
import com.kyant.monet.n1
import com.kyant.monet.withNight

@Composable
fun BathroomOpening(
    discoveryViewModel: DiscoveryViewModel = viewModel()
) {
    Column(
        modifier = Modifier
            .clip(SmoothRoundedCornerShape(24.dp))
            .background(100.n1 withNight 20.n1)
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "桔园浴室",
            modifier = Modifier.padding(horizontal = 24.dp),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        discoveryViewModel.bathroom["桔园浴室"]?.let {
            Text(
                text = buildAnnotatedString {
                    it.forEach {
                        append(if (it == 'w') "♀️" else "♂️")
                    }
                },
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
