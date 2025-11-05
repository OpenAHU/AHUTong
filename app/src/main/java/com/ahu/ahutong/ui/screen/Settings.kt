package com.ahu.ahutong.ui.screen

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ahu.ahutong.AHUApplication
import com.ahu.ahutong.Constants
import com.ahu.ahutong.R
import com.ahu.ahutong.data.api.AHUCookieJar
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.AboutViewModel
import com.ahu.ahutong.ui.state.MainViewModel
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.kyant.capsule.ContinuousCapsule
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight

@SuppressLint("ContextCastToActivity")
@Composable
fun Settings(
    navController: NavHostController,
    mainViewModel: MainViewModel = viewModel(),
    aboutViewModel: AboutViewModel = viewModel()
) {
    val context = LocalContext.current as ComponentActivity
    var isClearCacheDialogShown by rememberSaveable { mutableStateOf(false) }
    var isUpdateLogDialogShown by rememberSaveable { mutableStateOf(false) }
    val tip by remember { aboutViewModel.tipState }

    LaunchedEffect(tip) {
        tip?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            aboutViewModel.tipState.value = null;
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .systemBarsPadding()
            .padding(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.setting),
            modifier = Modifier.padding(24.dp, 32.dp),
            style = MaterialTheme.typography.headlineLarge
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(SmoothRoundedCornerShape(32.dp))
                .background(90.a1 withNight 20.n1)
                .padding(24.dp, 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            var count by remember { mutableStateOf(0) }
            var lastClickTime by remember { mutableStateOf(0L) }
            val clickTimes: Int = 8
            val interval: Long = 1000

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    val now = System.currentTimeMillis()
                    if (now - lastClickTime > interval) {
                        count = 1
                    } else {
                        count++
                    }
                    lastClickTime = now

                    if (count >= clickTimes) {
                        count = 0
                        navController.navigate("debug")
                    }
                }
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier
                        .clip(ContinuousCapsule)
                        .background(100.n1)
                        .padding(4.dp)
                        .size(64.dp)
                        .clip(ContinuousCapsule)
                        .scale(1.75f)
                )
                Column {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = aboutViewModel.versionName!!,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
        Text(
            text = "账户信息",
            modifier = Modifier.padding(horizontal = 24.dp),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        AHUCache.getCurrentUser()?.let { user ->
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(SmoothRoundedCornerShape(32.dp))
                    .background(100.n1 withNight 20.n1)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp, 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    AHUCache.getSchoolTerm()?.let {
                        val data = it.split('-')  //2025-2026-1
                        if (data.size == 3) {
                            Text(
                                text = "第${data.get(0)}-${data.get(1)}学年 第${data.get(2)}学期",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
//                    Row(
//                        modifier = Modifier
//                            .weight(1f)
//                            .clip(ContinuousCapsule)
//                            .background(100.n1 withNight 30.n1)
//                            .clickable { navController.navigate("info") }
//                            .padding(12.dp, 8.dp),
//                        horizontalArrangement = Arrangement.spacedBy(
//                            8.dp,
//                            Alignment.CenterHorizontally
//                        ),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Icon(
//                            imageVector = Icons.Outlined.Edit,
//                            contentDescription = null,
//                            modifier = Modifier.size(20.dp)
//                        )
//                        Text(
//                            text = "修改信息",
//                            style = MaterialTheme.typography.titleMedium
//                        )
//                    }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(ContinuousCapsule)
                            .background(100.n1 withNight 30.n1)
                            .clickable { navController.navigate("login") }
                            .padding(12.dp, 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(
                            8.dp,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Login,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "重新登录",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clip(SmoothRoundedCornerShape(32.dp)),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            SettingItem(
                label = stringResource(id = R.string.preferences),
                icon = Icons.Outlined.Tune,
                onClick = { navController.navigate("preferences") }
            )

        }

        Text(
            text = "关于",
            modifier = Modifier.padding(horizontal = 24.dp),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clip(SmoothRoundedCornerShape(32.dp)),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            SettingItem(
                label = stringResource(id = R.string.license),
                icon = Icons.Outlined.Article,
                onClick = { navController.navigate("settings__license") }
            )
            SettingItem(
                label = stringResource(id = R.string.contributors),
                icon = Icons.Outlined.PeopleOutline,
                onClick = { navController.navigate("settings__contributors") }
            )
            SettingItem(
                label = stringResource(id = R.string.mine_tv_feedback),
                icon = Icons.Outlined.Feedback,
                onClick = {
//                    try {
//                        context.startActivity(
//                            Intent(
//                                Intent.ACTION_VIEW,
//                                Uri.parse(
//                                    "mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3DL3WKrBqXGuSoqrpbm4zVqHWN-WyB-Y29"
//                                )
//                            ).apply {
//                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//                            }
//                        )
//                    } catch (e: Exception) {
//                        Toast
//                            .makeText(context, "请安装 QQ 或 Tim", Toast.LENGTH_SHORT)
//                            .show()
//                    }

                    Toast.makeText(context, "暂未开放", Toast.LENGTH_SHORT).show()

                }
            )
            SettingItem(
                label = stringResource(id = R.string.setting_clear),
                icon = Icons.Outlined.ClearAll,
                onClick = { isClearCacheDialogShown = true }
            )
            SettingItem(
                label = stringResource(id = R.string.update_intro),
                icon = Icons.Outlined.Article,
                onClick = { isUpdateLogDialogShown = true }
            )
//            SettingItem(
//                label = stringResource(id = R.string.check_update),
//                icon = Icons.Outlined.Update,
//                onClick = { aboutViewModel.checkForUpdates() }
//            )
        }
    }
    if (isClearCacheDialogShown) {
        Dialog(onDismissRequest = { isClearCacheDialogShown = false }) {
            Column(
                modifier = Modifier
                    .clip(SmoothRoundedCornerShape(32.dp))
                    .background(96.n1 withNight 10.n1)
                    .padding(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                val app = (LocalContext.current as ComponentActivity).application

                Text(
                    text = "您的登录状态、课表等信息将会被永久清除",
                    modifier = Modifier.padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "清除",
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(ContinuousCapsule)
                        .background(90.a1 withNight 30.n1)
                        .clickable {
                            mainViewModel.logout()
                            AHUCache.clearAll()

                            val cookieJar = AHUCookieJar(
                                SetCookieCache(),
                                SharedPrefsCookiePersistor(app)
                            )

                            cookieJar.clear()
                            cookieJar.clearSession()

                            AHUApplication.sessionExpired = true


                            Toast
                                .makeText(context, "已清除所有数据", Toast.LENGTH_SHORT)
                                .show()

                            navController.navigate("login")
                        }
                        .padding(16.dp, 8.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
    if (isUpdateLogDialogShown) {
        Dialog(onDismissRequest = { isUpdateLogDialogShown = false }) {
            Column(
                modifier = Modifier
                    .clip(SmoothRoundedCornerShape(32.dp))
                    .background(96.n1 withNight 10.n1)
                    .padding(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.update_intro),
                    modifier = Modifier.padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = Constants.UPDATE_LOG,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun SettingItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SmoothRoundedCornerShape(4.dp))
            .background(100.n1 withNight 20.n1)
            .clickable(onClick = onClick)
            .padding(24.dp, 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
