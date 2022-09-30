package com.ahu.ahutong.ui.screen.component

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.ahu.ahutong.R
import com.kyant.monet.n1
import com.kyant.monet.withNight

@Composable
fun CampusCard(
    balance: Double,
    transitionBalance: Double
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(100.n1 withNight 20.n1)
            .padding(24.dp, 16.dp, 16.dp, 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = stringResource(id = R.string.card_money),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        MaterialTheme.typography.titleLarge.toSpanStyle()
                            .copy(fontWeight = FontWeight.Bold)
                    ) {
                        append("¥ $balance")
                    }
                    withStyle(
                        MaterialTheme.typography.titleSmall.toSpanStyle().copy(color = 50.n1 withNight 80.n1)
                    ) {
                        append(" + ¥ $transitionBalance")
                    }
                }
            )
        }
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .clickable {
                    try {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("alipays://platformapi/startapp?appId=2019090967125695&page=pages%2Findex%2Findex&enbsv=0.3.2106171038.6&chInfo=ch_share__chsub_CopyLink")
                            ).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                        )
                    } catch (e: Exception) {
                        Toast.makeText(context, "请安装支付宝", Toast.LENGTH_SHORT).show()
                    }
                }
                .border(
                    width = 2.dp,
                    color = 90.n1 withNight 40.n1,
                    shape = CircleShape
                )
                .padding(12.dp, 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
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
