package com.ahu.ahutong.ui.screen.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahu.ahutong.ui.component.LoadingIndicator
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.CardBalanceDepositViewModel
import com.ahu.ahutong.ui.state.PaymentState
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardBalanceDeposit(
    viewModel: CardBalanceDepositViewModel = viewModel()
) {

    var amount by remember { mutableStateOf("") }

    val cardInfo = viewModel.cardInfo.collectAsState()

    val paymentState by viewModel.paymentState.collectAsState()

    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .systemBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        Text(
            text = "校园卡充值",
            modifier = Modifier.padding(24.dp, 32.dp),
            style = MaterialTheme.typography.headlineMedium
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(100.n1 withNight 20.n1)
        ) {
            val balance = 10
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = "校园卡账户",
                    style = MaterialTheme.typography.titleMedium
                )

                val accountInfo = cardInfo.value?.data?.card?.getOrNull(0)?.accinfo
                    ?.getOrNull(0)


                val account = accountInfo?.let {
                    "${it.name} ${it.type}"
                } ?: "--"

                Text(
                    text = account
                )
            }
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = "账户余额", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = cardInfo.value?.data?.card?.getOrNull(0)
                        ?.accinfo?.getOrNull(0)?.balance?.let { String.format("￥%.2f", it / 100.0) }
                        ?: "￥--",
                    style = MaterialTheme.typography.titleMedium
                )
            }

        }

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(100.n1 withNight 20.n1),
        ) {

            Text(
                text = "充值金额",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium

            )

            TextField(
                value = amount,
                onValueChange = { newText ->
                    if (newText.isEmpty()) {
                        amount = newText
                        return@TextField
                    }

                    val regex = Regex("^\\d*\\.?\\d{0,2}$")
                    if (regex.matches(newText)) {
                        amount = newText
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    ),
                placeholder = { Text("请输入金额",color = 30.n1 withNight 70.n1) },
                textStyle = TextStyle(fontSize = 16.sp,color = 10.n1 withNight 90.n1)
            )
        }


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End) {
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .clip(SmoothRoundedCornerShape(32.dp))
                    .background(
                        animateColorAsState(
                            targetValue = when (paymentState) {
                                PaymentState.Idle -> 90.a1 withNight 85.a1
                                PaymentState.Loading -> 70.a1 withNight 60.a1
                                is PaymentState.Error -> Color.Red
                                is PaymentState.Success -> 70.a1 withNight 60.a1
                            }
                        ).value
                    )
                    .animateContentSize(spring(stiffness = Spring.StiffnessLow))
            ) {
                when (val state = paymentState) {
                    PaymentState.Idle -> {
                        CompositionLocalProvider(LocalIndication provides rememberRipple(color = 0.n1)) {
                            Text(
                                text = "确认",
                                modifier = Modifier
                                    .clickable(
                                        role = Role.Button,
                                        onClick = {
                                            if (amount.isNotEmpty()) {
                                                showConfirmDialog = true // 点击显示弹窗
                                            }
                                        }
                                    )
                                    .padding(24.dp, 16.dp),
                                color = 0.n1,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }


                    PaymentState.Loading -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LoadingIndicator(
                                modifier = Modifier.size(56.dp),
                                color = 100.n1,
                                strokeWidth = 6.dp
                            )
                            Text(
                                text = "支付中",
                                modifier = Modifier.padding(4.dp),
                                color = 100.n1,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }

                    is PaymentState.Error -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = 100.n1
                            )
                            Text(
                                text = "支付失败！错误信息：${state.message}",
                                modifier = Modifier.padding(4.dp),
                                color = 100.n1,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }

                    is PaymentState.Success -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {


                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = 100.n1
                            )
                            Text(
                                text = "支付成功！订单号：${state.orderId}",
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clickable {
                                        viewModel.resetPaymentState()
                                    },
                                color = 100.n1,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }

                }
            }
        }


        if (showConfirmDialog) {
            AlertDialog(

                containerColor = 100.n1 withNight 20.n1,
                titleContentColor = 10.n1 withNight 90.n1,
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("确认支付") },
                text = { Text("您即将从绑定的银行卡扣除￥$amount 元，并充值到校园卡余额中。请确认金额无误后继续操作。", color = 40.n1 withNight 60.n1) },
                confirmButton = {
                    Text(
                        text = "支付",
                        modifier = Modifier
                            .clickable {
                                viewModel.charge(amount)
                                showConfirmDialog = false
                            }
                            .padding(8.dp),
                        color = 10.n1 withNight 90.n1
                    )
                },
                dismissButton = {
                    Text(
                        text = "取消",
                        modifier = Modifier
                            .clickable { showConfirmDialog = false }
                            .padding(8.dp),
                        color = 10.n1 withNight 90.n1
                    )
                }
            )
        }

    }

}
