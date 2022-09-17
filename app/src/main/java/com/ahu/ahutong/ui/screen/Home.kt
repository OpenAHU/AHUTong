package com.ahu.ahutong.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahu.ahutong.R
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.ui.page.state.DiscoveryViewModel
import com.ahu.ahutong.ui.screen.component.CampusCard
import com.ahu.ahutong.ui.screen.component.CourseCard
import com.ahu.ahutong.ui.screen.component.FunctionalButton
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.kyant.monet.n1
import com.kyant.monet.withNight

@Composable
fun Home(
    discoveryViewModel: DiscoveryViewModel = viewModel()
) {
    val user = AHUCache.getCurrentUser() ?: return
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(96.n1 withNight 10.n1)
            .systemBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Hi, ${user.name}",
            modifier = Modifier.padding(24.dp, 56.dp, 24.dp, 24.dp),
            style = MaterialTheme.typography.headlineLarge
        )
        CourseCard()
        Text(
            text = "功能",
            modifier = Modifier.padding(horizontal = 24.dp),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            mainAxisAlignment = MainAxisAlignment.Center
        ) {
            listOf(
                R.string.grade to R.mipmap.score,
                R.string.phone_book to R.mipmap.telephone_directory,
                R.string.bathroom to R.mipmap.bathroom,
                R.string.exam to R.mipmap.examination_room
            ).forEach { (label, icon) ->
                FunctionalButton(
                    stringId = label,
                    iconId = icon
                )
            }
        }
        FlowRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            mainAxisSpacing = 16.dp,
            crossAxisSpacing = 16.dp
        ) {
            CampusCard(
                balance = discoveryViewModel.balance
            )
        }
    }
}
