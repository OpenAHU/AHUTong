package com.ahu.ahutong.ui.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight

@Composable
fun Splash(navController: NavController) {
    var showAgreementDialog by remember { mutableStateOf(!AHUCache.isAgreementAccepted()) }
    var showPrivacyDialog by remember { mutableStateOf(!AHUCache.isPrivacyAccepted()) }
    var showBusinessDialog by remember { mutableStateOf(!AHUCache.isBusinessAccepted()) }


    val activity = LocalActivity.current



    LaunchedEffect(showAgreementDialog) {
        if (AHUCache.isAgreementAccepted()) {
            if (AHUCache.isLogin()) {
                navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }
            } else {
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
    }


    if (showAgreementDialog) {
        AgreementDialog(
            onAgree = {
                AHUCache.setAgreementAccepted()
                showAgreementDialog = false
            },
            onDisagree = {
                activity?.finish()
            }
        )
    }
    if (showPrivacyDialog) {
        PrivacyDialog(
            onAgree = {
                AHUCache.setPrivacyAccepted()
                showPrivacyDialog = false
            },
            onDisagree = {
                activity?.finish()
            }
        )
    }
    if (showBusinessDialog) {
        BusinessDialog(
            onAgree = {
                AHUCache.setBusinessAccepted()
                showBusinessDialog = false
            },
            onDisagree = {
                activity?.finish()
            }
        )
    }
}


@Composable
fun AgreementDialog(
    onAgree: () -> Unit,
    onDisagree: () -> Unit
) {

    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                text = "温馨提示与免责声明",
                style = MaterialTheme.typography.headlineSmall,
                color = 10.n1 withNight 90.n1
            )
        },
        text = {
            val disclaimerText = """
                1. 本项目完全开源，任何人均可基于本项目进行二次开发或分发。
                2. 由于开源特性，非官方渠道下载或安装的应用可能存在安全风险，请务必确保应用来源可信。
                3. 本项目不会收集、存储或泄露用户的任何个人信息，也不会侵犯用户的合法权利。
                4. 用户在使用本项目或其二次开发版本时，应自行判断安全性并承担相应风险。因非官方或非正版应用造成的财产损失，开发者不承担任何责任。
                5. 使用本应用即表示您已阅读并理解本免责声明，并同意自行承担使用风险。
            """.trimIndent()

            Box(
                modifier = Modifier
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = disclaimerText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = 10.n1 withNight 90.n1
                )
            }
        },
        shape = SmoothRoundedCornerShape(32.dp),
        confirmButton = {
            FilledTonalButton(
                onClick = onAgree,
                modifier = Modifier.size(88.dp, 56.dp),
                shape = SmoothRoundedCornerShape(16.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = 90.a1 withNight 85.a1,
                    contentColor = 0.n1
                )
            ) {
                Text("同意")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDisagree,
                modifier = Modifier.size(88.dp, 56.dp),
                shape = SmoothRoundedCornerShape(16.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = 90.a1 withNight 85.a1,
                    contentColor = 0.n1
                )
            ) {
                Text("拒绝")
            }
        },
        containerColor = 100.n1 withNight 20.n1
    )
}

@Composable
fun PrivacyDialog(
    onAgree: () -> Unit,
    onDisagree: () -> Unit
) {

    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                text = "隐私政策",
                style = MaterialTheme.typography.headlineSmall,
                color = 10.n1 withNight 90.n1
            )
        },
        text = {
            val disclaimerText = """
                1. 安大通不会将您的用户数据上传到云服务器。
                2. 安大通会记录运行时的软件内（仅限安大通）页面信息，用于分析用户群体的使用习惯，并及时做功能调整。
                3. 安大通记录的页面信息中，不包括您的个人数据。
                4. 一切您的个人数据，不会被分享至第三方（学校属于两方平台）。
                
                截止2025/11/09，安大通并未实现上传数据等相关功能。目前该功能处于试验阶段，记录到的数据仅存储在本地，依赖安卓的存储隔离保障安全性。
            """.trimIndent()

            Box(
                modifier = Modifier
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = disclaimerText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = 10.n1 withNight 90.n1
                )
            }
        },
        shape = SmoothRoundedCornerShape(32.dp),
        confirmButton = {
            FilledTonalButton(
                onClick = onAgree,
                modifier = Modifier.size(88.dp, 56.dp),
                shape = SmoothRoundedCornerShape(16.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = 90.a1 withNight 85.a1,
                    contentColor = 0.n1
                )
            ) {
                Text("同意")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDisagree,
                modifier = Modifier.size(88.dp, 56.dp),
                shape = SmoothRoundedCornerShape(16.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = 90.a1 withNight 85.a1,
                    contentColor = 0.n1
                )
            ) {
                Text("拒绝")
            }
        },
        containerColor = 100.n1 withNight 20.n1
    )
}

@Composable
fun BusinessDialog(
    onAgree: () -> Unit,
    onDisagree: () -> Unit
) {

    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                text = "商业合作",
                style = MaterialTheme.typography.headlineSmall,
                color = 10.n1 withNight 90.n1
            )
        },
        text = {
            val disclaimerText = """
                目前安大通的商业价值处于探索阶段，为了持久化发展、优化广大同学的体验，急需几名大一/大二的同学做发展规划。
                如果您有兴趣，欢迎联系我们！QQ群1006203134
                另外，如果您对安大通有任何想法或建议，也欢迎加群反馈！
            """.trimIndent()

            Box(
                modifier = Modifier
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = disclaimerText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = 10.n1 withNight 90.n1
                )
            }
        },
        shape = SmoothRoundedCornerShape(32.dp),
        confirmButton = {
            FilledTonalButton(
                onClick = onAgree,
                modifier = Modifier.size(88.dp, 56.dp),
                shape = SmoothRoundedCornerShape(16.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = 90.a1 withNight 85.a1,
                    contentColor = 0.n1
                )
            ) {
                Text("同意")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDisagree,
                modifier = Modifier.size(88.dp, 56.dp),
                shape = SmoothRoundedCornerShape(16.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = 90.a1 withNight 85.a1,
                    contentColor = 0.n1
                )
            ) {
                Text("拒绝")
            }
        },
        containerColor = 100.n1 withNight 20.n1
    )
}