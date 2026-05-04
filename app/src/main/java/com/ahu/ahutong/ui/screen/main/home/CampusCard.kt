package com.ahu.ahutong.ui.screen.main.home


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ahu.ahutong.R
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.dao.PreferencesManager
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.DiscoveryViewModel
import com.kyant.monet.n1
import com.kyant.monet.withNight

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RowScope.CampusCard(
    balance: Double,
    transitionBalance: Double,
    onRefreshBalance: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context = context) }

    val pref by preferencesManager.showQRCode.collectAsState(initial = false)

    var isQrcode by remember { mutableStateOf(pref) }

    LaunchedEffect(pref) {
        isQrcode = pref
    }

    LaunchedEffect(isQrcode) {
        if (AHUCache.isLogin() && isQrcode) {
            onRefreshBalance()
        }
    }


    Box(
        modifier = Modifier
            .weight(1f)
            .height(IntrinsicSize.Min)
    ) {
        AnimatedContent(
            targetState = isQrcode,
            transitionSpec = {
                if (targetState) {
                    (fadeIn(animationSpec = tween(500)) + scaleIn(
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                        initialScale = 0.5f
                    )).togetherWith(
                        fadeOut(animationSpec = tween(300)) + scaleOut(
                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                            targetScale = 0.5f
                        )
                    )
                } else {
                    (fadeIn(animationSpec = tween(500)) + scaleIn(
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                        initialScale = 0.5f
                    )).togetherWith(
                        fadeOut(animationSpec = tween(300)) + scaleOut(
                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                            targetScale = 0.5f
                        )
                    )
                }.using(
                    SizeTransform(clip = false)
                )
            }

        ) { showQr ->
            if (!showQr) {
                CardView(
                    balance = balance,
                    transitionBalance = transitionBalance,
                    onClick = {
                        isQrcode = true
                    },
                    navController
                )
            } else {
                QRcodeView(
                    balance = balance,
                    onBack = {
                        isQrcode = false
                    }
                )
            }
        }
    }


}


@Composable
private fun CardView(
    balance: Double,
    transitionBalance: Double,
    onClick: () -> Unit,
    navController: NavController
) {


    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .clip(SmoothRoundedCornerShape(24.dp))
            .background(100.n1 withNight 20.n1),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .clickable {
                    onClick()
                },
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.card_money),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 5.dp)
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
//                        withStyle(
//                            MaterialTheme.typography.titleSmall.toSpanStyle().copy(
//                                color = 50.n1 withNight 80.n1
//                            )
//                        ) {
//                            append(" + ¥ $transitionBalance")
//                        }
                        }
                    )
                }
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
//                    try {
//                        context.startActivity(
//                            Intent(
//                                Intent.ACTION_VIEW,
//                                Uri.parse(
//                                    "alipays://platformapi/startapp?appId=2019090967125695&page=pages%2Findex%2Findex&enbsv=0.3.2106171038.6&chInfo=ch_share__chsub_CopyLink"
//                                )
//                            ).apply {
//                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//                            }
//                        )
//                    } catch (e: Exception) {
//                        Toast.makeText(context, "请安装支付宝", Toast.LENGTH_SHORT).show()
//                    }

                    navController.navigate("card_balance_deposit")

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


@Composable
private fun QRcodeView(balance: Double, onBack: () -> Unit) {
    val discoveryViewModel: DiscoveryViewModel = hiltViewModel()
    val qrcodeBitmap by discoveryViewModel.qrcode.collectAsState()
    val finished by discoveryViewModel.state.collectAsState()
    var showFullScreen by remember {
        mutableStateOf(false)
    }

    val activity = androidx.activity.compose.LocalActivity.current

    if (AHUCache.isLogin()) {
        LaunchedEffect(Unit) {
            discoveryViewModel.loadQrCode()
        }
    }

    // 放大二维码时亮度拉满，关闭时恢复
    if (showFullScreen) {
        DisposableEffect(Unit) {
            val window = activity?.window
            val layoutParams = window?.attributes

            // 保存原始亮度
            val originalBrightness = layoutParams?.screenBrightness ?: -1f

            // 设置最高亮度
            layoutParams?.screenBrightness = 1f
            window?.attributes = layoutParams

            onDispose {
                // 恢复原始亮度
                layoutParams?.screenBrightness = originalBrightness
                window?.attributes = layoutParams
            }
        }
    }

    Column(
        modifier = Modifier
            .clip(SmoothRoundedCornerShape(24.dp))
            .background(100.n1 withNight 20.n1)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("¥ $balance")
        Spacer(Modifier.height(16.dp))

        if (finished) {
            qrcodeBitmap?.let {
                Box(
                    modifier = Modifier
                        .clickable {
                            discoveryViewModel.loadQrCode()
                            discoveryViewModel.refreshCardBalance()
                        }
                        .clip(SmoothRoundedCornerShape(8.dp))
                ) {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier
                            .size(200.dp)
                            .border(
                                1.dp,
                                Color.Gray,
                                SmoothRoundedCornerShape(8.dp)
                            ),
                    )
                }
            } ?: Text(
                text = "加载失败"
            )

        } else {
            CircularProgressIndicator()
        }

        Spacer(Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier.clickable {
                    discoveryViewModel.loadQrCode()
                    showFullScreen = true
                }
            ) {
                Text("放大")
            }

            Box(
                modifier = Modifier.clickable {
                    onBack()
                }
            ) {
                Text("返回")
            }
        }

        if (showFullScreen) {
            Dialog(
                onDismissRequest = {
                    showFullScreen = false
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            showFullScreen = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    qrcodeBitmap?.let {
                        Box(
                            modifier = Modifier
                                .size(360.dp)
                                .clip(SmoothRoundedCornerShape(24.dp))
                                .background(Color.White)
                                .padding(20.dp)
                        ) {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "Full QR Code",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}