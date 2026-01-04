package com.ahu.ahutong.ui.component

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight

@Composable
fun HotUpdateDialog(
    onConfirm: () -> Unit
) {
    val contentColor = 10.n1 withNight 90.n1
    val containerColor = 100.n1 withNight 20.n1

    AlertDialog(
        onDismissRequest = {

        },
        properties = DialogProperties(
            dismissOnBackPress = false,      // 禁止物理返回键关闭
            dismissOnClickOutside = false,   // 禁止点击弹窗外部关闭
        ),
        icon = {
            Icon(
                imageVector = Icons.Filled.SystemUpdate,
                contentDescription = "Update Icon",
                tint = contentColor
            )
        },
        title = {
            Text(
                text = "更新完成",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall,
                color = contentColor
            )
        },
        text = {
            Text(
                text = "已完成热更新。为防止数据异常，请立即重启应用以应用新版本。",
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
        },
        shape = SmoothRoundedCornerShape(32.dp),
        containerColor = containerColor,
        confirmButton = {
            FilledTonalButton(
                onClick = onConfirm,
                modifier = Modifier.height(56.dp),
                shape = SmoothRoundedCornerShape(16.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = 90.a1 withNight 85.a1,
                    contentColor = 0.n1
                )
            ) {
                Text("立即重启")
            }
        }
    )
}