package com.ahu.ahutong.ui.screen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.kyant.monet.n1
import com.kyant.monet.withNight

@Composable
fun BathroomOpenStates(
    discoveryViewModel: DiscoveryViewModel = viewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(100.n1 withNight 20.n1)
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.bathroom),
            modifier = Modifier.padding(horizontal = 24.dp),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        Row(modifier = Modifier.padding(horizontal = 16.dp)) {
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
                                            else -> error("Unknown gender???!!!")
                                        }
                                    )
                                }
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}
