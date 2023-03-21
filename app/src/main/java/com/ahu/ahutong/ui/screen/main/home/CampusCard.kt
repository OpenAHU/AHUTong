package com.ahu.ahutong.ui.screen.main.home

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.kyant.monet.n1
import com.kyant.monet.withNight

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RowScope.CampusCard(
    balance: Double,
    transitionBalance: Double
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .weight(1f)
            .height(IntrinsicSize.Min)
            .clip(SmoothRoundedCornerShape(24.dp))
            .background(100.n1 withNight 20.n1),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(24.dp, 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.card_money),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            AnimatedContent(targetState = balance to transitionBalance) { (balance, transitionBalance) ->
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            MaterialTheme.typography.titleLarge.toSpanStyle()
                                .copy(fontWeight = FontWeight.Bold)
                        ) {
                            append("¥ $balance")
                        }
                        withStyle(
                            MaterialTheme.typography.titleSmall.toSpanStyle().copy(
                                color = 50.n1 withNight 80.n1
                            )
                        ) {
                            append(" + ¥ $transitionBalance")
                        }
                    }
                )
            }
        }
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(96.n1 withNight 10.n1)
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .clickable {
                    try {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                    "alipays://platformapi/startapp?appId=2019090967125695&page=pages%2Findex%2Findex&enbsv=0.3.2106171038.6&chInfo=ch_share__chsub_CopyLink"
                                )
                            ).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                        )
                    } catch (e: Exception) {
                        Toast.makeText(context, "请安装支付宝", Toast.LENGTH_SHORT).show()
                    }
                }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "充\n值",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
