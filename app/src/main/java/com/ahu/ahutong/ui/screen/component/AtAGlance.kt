package com.ahu.ahutong.ui.screen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.ahu.ahutong.data.model.User
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AtAGlance(
    user: User
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp, 24.dp, 0.dp, 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Hi, ",
                style = MaterialTheme.typography.headlineLarge
            )
            val strokeColor = 40.a1 withNight 80.a1
            Text(
                text = user.name,
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = strokeColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                },
                style = MaterialTheme.typography.headlineLarge
            )
        }
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50))
                .background(100.n1 withNight 20.n1)
                .padding(16.dp, 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = SimpleDateFormat("MM月dd日 EE", Locale.CHINA)
                    .format(Calendar.getInstance(Locale.CHINA).time),
                style = MaterialTheme.typography.titleMedium
            )
            // TODO: implement weather
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = 40.a1 withNight 80.a1
                )
                Text(
                    text = "24℃",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
