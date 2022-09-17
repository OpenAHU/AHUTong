package com.ahu.ahutong.ui.screen.component

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
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
import com.ahu.ahutong.R
import com.kyant.monet.n1
import com.kyant.monet.withNight

@Composable
fun CampusCard(
    balance: Double
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .width(IntrinsicSize.Max)
            .clip(RoundedCornerShape(16.dp))
            .background(100.n1 withNight 30.n1)
    ) {
        Column(
            modifier = Modifier.padding(24.dp, 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.card_money),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = "¥ $balance",
                style = MaterialTheme.typography.titleLarge
            )
        }
        Divider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    try {
                        val i1 = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("alipays://platformapi/startapp?appId=2019090967125695&page=pages%2Findex%2Findex&enbsv=0.3.2106171038.6&chInfo=ch_share__chsub_CopyLink")
                        )
                        i1.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        context.startActivity(i1)
                    } catch (e: Exception) {
                        Toast.makeText(context, "手机未安装支付宝 App", Toast.LENGTH_SHORT).show()
                    }
                }
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.MonetizationOn,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(id = R.string.recharge),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
