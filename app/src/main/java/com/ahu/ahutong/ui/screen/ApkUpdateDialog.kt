package com.ahu.ahutong.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.ahu.ahutong.data.server.model.ApkUpdateInfo
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight

@Composable
fun ApkUpdateDialog(
    info: ApkUpdateInfo,
    downloading: Boolean,
    progress: Float? = null,
    errorText: String? = null,
    apkLocalReady: Boolean = false,
    onConfirm: () -> Unit,
    onInstallLocal: () -> Unit = {},
    onRedownload: () -> Unit = {},
    onDismiss: () -> Unit,
    onCancel: () -> Unit = {},
) {
    val contentColor = 10.n1 withNight 90.n1
    val containerColor = 100.n1 withNight 20.n1

    AlertDialog(
        onDismissRequest = {
            if (!info.force && !downloading) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = !info.force && !downloading,
            dismissOnClickOutside = !info.force && !downloading,
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
                text = "发现新版本 ${info.versionName}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall,
                color = contentColor
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "版本号：${info.versionCode}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "更新内容：",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = info.changelog.ifBlank { "暂无更新说明" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor
                )

                if (downloading) {
                    Spacer(Modifier.height(12.dp))

                    if (progress == null) {
                        androidx.compose.material3.LinearProgressIndicator()
                    } else {
                        androidx.compose.material3.LinearProgressIndicator(progress = progress)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "下载进度：${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor
                        )
                    }
                }

                if (!errorText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "错误：",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor
                    )
                }
            }
        },
        shape = SmoothRoundedCornerShape(32.dp),
        containerColor = containerColor,
        confirmButton = {
            FilledTonalButton(
                onClick = if (apkLocalReady && !downloading) onInstallLocal else onConfirm,
                enabled = !downloading,
                modifier = Modifier.height(56.dp),
                shape = SmoothRoundedCornerShape(16.dp),
                colors = if (downloading) {
                    ButtonDefaults.filledTonalButtonColors(
                        containerColor = 95.a1 withNight 30.a1,
                        contentColor = 40.n1 withNight 70.n1
                    )
                } else {
                    ButtonDefaults.filledTonalButtonColors(
                        containerColor = 90.a1 withNight 85.a1,
                        contentColor = 0.n1
                    )
                }
            ) {
                Text(
                    text = when {
                        downloading -> "下载中…"
                        apkLocalReady -> "安装"
                        else -> "下载并安装"
                    },
                    style = if (downloading) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                    fontWeight = if (downloading) FontWeight.Normal else FontWeight.Medium
                )
            }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (apkLocalReady && !downloading) {
                    TextButton(
                        onClick = onRedownload,
                        modifier = Modifier.height(56.dp),
                    ) {
                        Text("重新下载", color = contentColor)
                    }
                    Spacer(Modifier.width(4.dp))
                }
                if (downloading && !info.force) {
                    TextButton(
                        onClick = onCancel,
                        enabled = true,
                        modifier = Modifier.height(56.dp),
                    ) {
                        Text("后台下载", color = contentColor)
                    }
                } else if (!downloading && !info.force) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = true,
                        modifier = Modifier.height(56.dp),
                    ) {
                        Text("稍后更新", color = contentColor)
                    }
                }
            }
        }
    )
}
