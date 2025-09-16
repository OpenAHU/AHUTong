package com.ahu.ahutong.ui.screen.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ahu.ahutong.R
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.BusinessViewModel
import com.ahu.ahutong.ui.state.DeveloperViewModel
import com.kyant.monet.n1
import com.kyant.monet.withNight

@Composable
fun Contributors(
    developerViewModel: DeveloperViewModel = viewModel(),
    businessViewModel: BusinessViewModel = viewModel()
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .systemBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.contributors),
            modifier = Modifier.padding(24.dp, 32.dp),
            style = MaterialTheme.typography.headlineLarge
        )
        mapOf(
            developerViewModel.developers to stringResource(id = R.string.mine_tv_developer),
//            businessViewModel.partner to stringResource(id = R.string.mine_tv_business)
        ).forEach { (list, name) ->
            Text(
                text = name,
                modifier = Modifier.padding(horizontal = 24.dp),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Column(
                modifier = Modifier.clip(SmoothRoundedCornerShape(32.dp)),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                list.forEach {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SmoothRoundedCornerShape(4.dp))
                            .background(100.n1 withNight 20.n1)
                            .clickable {
                                try {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(it.getURL())).apply {
                                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        }
                                    )
                                } catch (e: Exception) {
                                    Toast.makeText(context, "请安装 QQ 或 Tim", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(24.dp, 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = it.getAvatarUrl(),
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape),
                            contentDescription = null
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = it.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = it.desc,
                                color = 30.n1 withNight 90.n1,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "QQ: ${it.qq}",
                                color = 50.n1 withNight 80.n1,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
