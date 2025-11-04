package com.ahu.ahutong.ui.screen.main

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahu.ahutong.data.crawler.PayState
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.ElectricityDepositViewModel
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ElectricityDeposit(
    viewModel: ElectricityDepositViewModel = viewModel()
) {
    val payState = viewModel.payState.collectAsState()
    LaunchedEffect(payState.value) {
        when (payState.value) {
            is PayState.Succeeded, is PayState.Failed -> {
                delay(1000)
                viewModel.resetPaymentState()
            }

            else -> {

            }
        }
    }

    val campusList by viewModel.campusList.collectAsState()
    val selectedCampus by viewModel.selectedCampus.collectAsState()

    val buildingsList by viewModel.buildingsList.collectAsState()
    val selectedBuilding by viewModel.selectedBuilding.collectAsState()

    val floorsList by viewModel.floorsList.collectAsState()
    val selectedFloor by viewModel.selectedFloor.collectAsState()

    val roomsList by viewModel.roomsList.collectAsState()
    val selectedRoom by viewModel.selectedRoom.collectAsState()

    val roomInfo by viewModel.roomInfo.collectAsState()

    var campusDropdownExpanded by remember { mutableStateOf(false) }
    var buildingsDropdownExpanded by remember { mutableStateOf(false) }
    var floorsDropdownExpanded by remember { mutableStateOf(false) }
    var roomsDropdownExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var infoClickCount by remember { mutableStateOf(0) }
    var currentToast by remember { mutableStateOf<Toast?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    var amount by remember { mutableStateOf("") }

    var showDialog by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .systemBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "电控缴费",
            modifier = Modifier.padding(24.dp, 32.dp),
            style = MaterialTheme.typography.headlineMedium
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(SmoothRoundedCornerShape(16.dp))
                .background(100.n1 withNight 20.n1)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .clickable { campusDropdownExpanded = true },

                ) {
                Text(
                    text = "选择校区",
                    style = MaterialTheme.typography.titleMedium
                )


                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { campusDropdownExpanded = true }
                ) {
                    Text(
                        text = selectedCampus?.name ?: "请选择校区"
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "展开校区列表"
                    )

                    DropdownMenu(
                        expanded = campusDropdownExpanded,
                        modifier = Modifier.heightIn(max = 350.dp),
                        onDismissRequest = { campusDropdownExpanded = false },
                    ) {
                        campusList.forEach { campus ->
                            DropdownMenuItem(
                                text = { Text(campus.name) },
                                onClick = {
                                    viewModel.onCampusSelected(campus)
                                    campusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .clickable { buildingsDropdownExpanded = true },
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = "选择楼栋", style = MaterialTheme.typography.titleMedium)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { buildingsDropdownExpanded = true }
                ) {
                    Text(
                        text = selectedBuilding?.name ?: "请选择楼栋"
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "展开楼栋列表"
                    )

                    DropdownMenu(
                        expanded = buildingsDropdownExpanded,
                        modifier = Modifier.heightIn(max = 450.dp),
                        onDismissRequest = { buildingsDropdownExpanded = false },
                    ) {
                        buildingsList.forEach { building ->
                            DropdownMenuItem(
                                text = { Text(building.name) },
                                onClick = {
                                    viewModel.onBuildingSelected(building)
                                    buildingsDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .clickable { floorsDropdownExpanded = true },
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = "选择楼层", style = MaterialTheme.typography.titleMedium)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { floorsDropdownExpanded = true }
                ) {
                    Text(
                        text = selectedFloor?.name ?: "请选择楼层"
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "展开楼层列表"
                    )

                    DropdownMenu(
                        expanded = floorsDropdownExpanded,
                        modifier = Modifier.heightIn(max = 450.dp),
                        onDismissRequest = { floorsDropdownExpanded = false },
                    ) {
                        floorsList.forEach { floor ->
                            DropdownMenuItem(
                                text = { Text(floor.name) },
                                onClick = {
                                    viewModel.onfloorSelected(floor)
                                    floorsDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .clickable { roomsDropdownExpanded = true },
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = "选择房间", style = MaterialTheme.typography.titleMedium)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { roomsDropdownExpanded = true }
                ) {
                    Text(
                        text = selectedRoom?.name ?: "请选择房间"
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "展开房间列表"
                    )

                    DropdownMenu(
                        expanded = roomsDropdownExpanded,
                        onDismissRequest = { roomsDropdownExpanded = false },
                        modifier = Modifier.heightIn(max = 500.dp)
                    ) {
                        roomsList.forEach { room ->
                            DropdownMenuItem(
                                text = { Text(room.name) },
                                onClick = {
                                    viewModel.onRoomSelected(room)
                                    roomsDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    // 4. 将 clickable 替换为 combinedClickable
                    .combinedClickable(
                        onClick = {
                            // --- 这里是之前的单击逻辑，保持不变 ---
                            infoClickCount++
                            currentToast?.cancel()
                            val message = when {
                                infoClickCount == 1 -> "点击五次查看累计充值记录，长按清空记录"
                                infoClickCount == 2 -> "再点击三次即可查看累计充值记录"
                                infoClickCount == 3 -> "再点击两次即可查看累计充值记录"
                                infoClickCount == 4 -> "再点击一次即可查看累计充值记录"
                                infoClickCount >= 5 -> {
                                    val chargeInfo = AHUCache.getElectricityChargeInfo()
                                    if (chargeInfo != null) {
                                        "从${chargeInfo.firstChargeDate}起累计电费充值金额为：${
                                            "%.2f".format(
                                                chargeInfo.totalAmount
                                            )
                                        }元"
                                    } else {
                                        "暂无充值记录"
                                    }
                                }

                                else -> null
                            }
                            if (message != null) {
                                val toastLength =
                                    if (infoClickCount >= 5) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
                                val newToast = Toast.makeText(context, message, toastLength)
                                newToast.show()
                                currentToast = newToast
                            }
                        },
                        onLongClick = {
                            showResetDialog = true
                        }
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = "信息", style = MaterialTheme.typography.titleMedium)
                Text(text = roomInfo?.replace("，", "\n") ?: "")
            }
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(SmoothRoundedCornerShape(16.dp))
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
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                placeholder = { Text("请输入金额") },
                textStyle = TextStyle(fontSize = 16.sp, color = 10.n1 withNight 90.n1)
            )
        }
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
                when (payState.value) {
                    is PayState.Idle -> {
                        Text(
                            text = "确认",
                            modifier = Modifier
                                .clickable(
                                    role = Role.Button,
                                    onClick = {
                                        if (selectedRoom != null && amount.isNotEmpty()) {
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

                    is PayState.InProgress -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = 100.n1,
                                strokeWidth = 6.dp
                            )
                            Text(
                                text = "支付中...",
                                modifier = Modifier.padding(4.dp),
                                color = 100.n1,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    is PayState.Succeeded -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = 100.n1
                            )
                            Text(
                                text = "支付成功！",
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

                    is PayState.Failed -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = 100.n1
                            )
                            Text(
                                text = "支付失败！",
                                modifier = Modifier.padding(4.dp),
                                color = 100.n1,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }
                }
            }
        }
        if (showDialog) {
            AlertDialog(
                containerColor = 100.n1 withNight 20.n1,
                onDismissRequest = { showDialog = false },
                title = { Text("请输入校园卡密码", color = 10.n1 withNight 90.n1) },
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
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (password.length == 6) {
                            showDialog = false
                            // 调用 ViewModel 中的 pay 函数
                            viewModel.pay(amount, password)
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
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("确认操作") },
                text = { Text("您确定要将累计充值金额清零吗？此操作不可撤销。") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            AHUCache.clearElectricityChargeInfo()
                            Toast.makeText(context, "累计记录已清零", Toast.LENGTH_SHORT).show()
                            showResetDialog = false
                        }
                    ) {
                        Text("确认")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showResetDialog = false }
                    ) {
                        Text("取消")
                    }
                }
            )
        }
    }
}