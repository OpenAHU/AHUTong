package com.ahu.ahutong.ui.screen

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ahu.ahutong.R
import com.ahu.ahutong.data.dao.AHUCache
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight
import kotlinx.coroutines.delay

@Composable
fun Splash(navController: NavController) {

    val TAG = "Splash"
    var showAgreementDialog by remember { mutableStateOf(!AHUCache.isAgreementAccepted()) }


    val activity = LocalActivity.current



    LaunchedEffect(showAgreementDialog) {
        if (AHUCache.isAgreementAccepted()) {
            if (AHUCache.isLogin()) {
                navController.navigate("home"){
                    popUpTo("splash") { inclusive = true }
                }
            } else {
                navController.navigate("login"){
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
    }


    if(showAgreementDialog){
        Log.e(TAG, "Splash: $showAgreementDialog", )
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
        confirmButton = {
            TextButton(
                onClick = onAgree,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(90.a1 withNight 85.a1)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("同意", color = 0.n1)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDisagree,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(90.a1 withNight 85.a1)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("拒绝", color = 0.n1)
            }
        },
        containerColor = 100.n1 withNight 20.n1
    )
}


@Composable
fun AnimatedSplashImage() {
    var startAnimation by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 500)
    )
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = tween(durationMillis = 500, easing = EaseOutBack)
    )
    LaunchedEffect(Unit) { startAnimation = true }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = "Logo",
            modifier = Modifier
                .size(128.dp)
                .alpha(alpha)
                .scale(scale)
        )
    }
}