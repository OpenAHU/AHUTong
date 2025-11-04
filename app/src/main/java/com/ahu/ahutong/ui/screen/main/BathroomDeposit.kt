package com.ahu.ahutong.ui.screen.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ripple
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahu.ahutong.data.crawler.PayState
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.BathroomDepositViewModel
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BathroomDeposit(

    viewmodel: BathroomDepositViewModel = viewModel()

) {
    val payState = viewmodel.payState.collectAsState()
    LaunchedEffect(payState.value) {
        when (payState.value) {
            is PayState.Succeeded, is PayState.Failed -> {
                delay(1000)
                viewmodel.resetPaymentState()
            }

            else -> {

            }
        }

    }
    val options = listOf("竹园/龙河", "桔园/蕙园")
    var expanded by remember { mutableStateOf(false) }
    var bathroom by remember { mutableStateOf(options[0]) }

    var amount by remember { mutableStateOf("") }
    var tel by remember { mutableStateOf("") }

    var hasFocus by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val info = viewmodel.info.collectAsState()

    var lastTel by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        lastTel = AHUCache.getPhone()
    }

    val textFieldColors = TextFieldDefaults.colors(
        unfocusedContainerColor = Color.Transparent,
        focusedContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .systemBarsPadding()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            },
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "浴室缴费",
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
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = "选择浴室",
                    style = MaterialTheme.typography.titleMedium
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = bathroom,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor()
                            .width(150.dp),
                        colors = textFieldColors,
                        textStyle = TextStyle(
                            textAlign = TextAlign.End,
                            fontSize = 16.sp,
                            color = 10.n1 withNight 90.n1
                        ),
                        singleLine = true,
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(99.n1 withNight 10.n1),
                    ) {
                        options.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption, color = 10.n1 withNight 90.n1) },
                                onClick = {
                                    bathroom = selectionOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "手机号", style = MaterialTheme.typography.titleMedium)
                TextField(
                    value = tel,
                    onValueChange = { value ->
                        tel = value
                    },
                    modifier = Modifier
                        .width(150.dp)
                        .onFocusChanged {
                            if (!it.isFocused && hasFocus && !tel.isEmpty()) {
                                viewmodel.getBathroomInfo(bathroom, tel)
                            }
                            hasFocus = it.isFocused
                        },
                    colors = textFieldColors,
                    textStyle = TextStyle(
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        color = 10.n1 withNight 90.n1
                    ),

                    singleLine = true,
                )
            }


            lastTel?.let {
                Row(horizontalArrangement = Arrangement.End) {
                    AnimatedVisibility(
                        visible = (lastTel != null && !hasFocus),
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "上次充值：$it",
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(90.a1 withNight 30.n1)
                                    .padding(8.dp)
                                    .clickable {
                                        tel = it
                                        viewmodel.getBathroomInfo(bathroom, tel)
                                        lastTel = null
                                    }


                            )
                        }
                    }
                }
            }


            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "信息", style = MaterialTheme.typography.titleMedium)

                val displayText = info.value?.let { it ->
                    when {
                        it.data.map == null -> it.data.message ?: "未知错误"
                        it.data.map!!.showData != null -> {
                            val showData = it.data.map!!.showData!!
                            "${showData.phone}\n现金金额：${showData.cashAmount}元\n赠送金额：${showData.giftAmount}元"
                        }

                        it.data.map!!.data?.message != null -> it.data.map!!.data!!.message!!
                        else -> "未知错误"
                    }
                } ?: ""

                Text(text = displayText)
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
                text = "缴费金额",
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
                colors = textFieldColors,
                placeholder = { Text("请输入金额", color = 30.n1 withNight 70.n1) },
                textStyle = TextStyle(fontSize = 16.sp, color = 10.n1 withNight 90.n1)
            )


        }

        var showDialog by remember { mutableStateOf(false) }
        var password by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf<String?>(null) }


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .clip(SmoothRoundedCornerShape(32.dp))
                    .background(
                        animateColorAsState(
                            targetValue = when (payState.value) {
                                is PayState.Idle -> 90.a1 withNight 85.a1
                                is PayState.InProgress -> 70.a1 withNight 60.a1
                                is PayState.Failed -> Color.Red
                                is PayState.Succeeded -> 70.a1 withNight 60.a1
                            }
                        ).value
                    )
                    .animateContentSize(spring(stiffness = Spring.StiffnessLow))
            ) {
                when (val state = payState.value) {
                    PayState.Idle -> {
                        CompositionLocalProvider(LocalIndication provides ripple(color = 0.n1)) {
                            Text(
                                text = "确认",
                                modifier = Modifier
                                    .clickable(
                                        role = Role.Button,
                                        onClick = {
                                            if (!amount.isEmpty() && info.value != null) {
                                                showDialog = true
                                            } else {

                                            }
                                        }
                                    )
                                    .padding(24.dp, 16.dp),
                                color = 0.n1,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    PayState.InProgress -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
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

                    is PayState.Failed -> {
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
                                text = "支付失败！ ${state.message}",
                                modifier = Modifier.padding(4.dp),
                                color = 100.n1,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }

                    is PayState.Succeeded -> {
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
                                text = "支付成功！ 订单号：${state.message}",
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clickable {

                                    },
                                color = 100.n1,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }

                }
            }


            if (showDialog) {
                AlertDialog(
                    containerColor = 100.n1 withNight 20.n1,
                    titleContentColor = 10.n1 withNight 90.n1,
                    onDismissRequest = { showDialog = false },
                    title = { Text("请输入校园卡密码") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = password,
                                onValueChange = { input ->
                                    if (input.length <= 6 && input.all { it.isDigit() }) {
                                        password = input
                                        errorMsg = null
                                    }
                                },
                                label = { Text("密码 (6位数字)", color = 40.n1 withNight 60.n1) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                visualTransformation = PasswordVisualTransformation(),
                                isError = errorMsg != null,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = 10.n1 withNight 90.n1,
                                    unfocusedTextColor = 10.n1 withNight 90.n1,
                                    focusedBorderColor = 20.n1 withNight 80.n1
                                )
                            )
                            if (errorMsg != null) {
                                Text(
                                    text = errorMsg!!,
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (password.length == 6) {
                                showDialog = false
                                viewmodel.pay(
                                    bathroom = bathroom,
                                    amount = amount,
                                    password = password
                                )
                            } else {
                                errorMsg = "密码必须是6位数字"
                            }
                        }) {
                            Text("确认", color = 10.n1 withNight 90.n1)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDialog = false
                            password = ""
                            errorMsg = null
                        }) {
                            Text("取消", color = 10.n1 withNight 90.n1)
                        }
                    }
                )


            }
        }
    }
}

