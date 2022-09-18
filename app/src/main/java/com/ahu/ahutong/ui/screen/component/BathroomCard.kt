package com.ahu.ahutong.ui.screen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahu.ahutong.R
import com.ahu.ahutong.ui.page.state.DiscoveryViewModel
import com.google.accompanist.flowlayout.FlowRow
import com.kyant.monet.n1
import com.kyant.monet.withNight

@Composable
fun BathroomCard(
    discoveryViewModel: DiscoveryViewModel = viewModel()
) {
    val lastBathroomName = discoveryViewModel.bathroom.toList().lastOrNull()?.first
    Column(
        modifier = Modifier
            .width(IntrinsicSize.Max)
            .clip(RoundedCornerShape(16.dp))
            .background(100.n1 withNight 30.n1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp, 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.bathroom),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )
        }
        Divider()
        FlowRow(modifier = Modifier.padding(horizontal = 8.dp)) {
            discoveryViewModel.bathroom.forEach { (name, openFor) ->
                Row(modifier = Modifier.height(IntrinsicSize.Max)) {
                    Column(
                        modifier = Modifier.padding(8.dp, 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = buildAnnotatedString {
                                openFor.forEach {
                                    append(
                                        when (it) {
                                            'w' -> "♀️"
                                            'm' -> "♂️"
                                            else -> it.toString()
                                        }
                                    )
                                }
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    if (name != lastBathroomName) {
                        Divider(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .width(DividerDefaults.Thickness)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}
