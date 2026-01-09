package com.ahu.ahutong

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.sdk.RustSDK
import com.ahu.ahutong.ui.screen.Main
import com.ahu.ahutong.ui.state.AboutViewModel
import com.ahu.ahutong.ui.state.DiscoveryViewModel
import com.ahu.ahutong.ui.state.LoginViewModel
import com.ahu.ahutong.ui.state.MainViewModel
import com.ahu.ahutong.ui.state.ScheduleViewModel
import com.ahu.ahutong.ui.theme.AHUTheme
import dagger.hilt.android.AndroidEntryPoint
import com.ahu.ahutong.ui.component.HotUpdateDialog

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.Intent
import androidx.core.content.FileProvider
import android.util.Log
import com.ahu.ahutong.sdk.ApkUpdateInfo
import com.ahu.ahutong.ui.component.ApkUpdateDialog
import java.io.File
import android.net.Uri
import android.os.Build
import android.provider.Settings

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private val discoveryViewModel: DiscoveryViewModel by viewModels()
    private val scheduleViewModel: ScheduleViewModel by viewModels()
    private val aboutViewModel: AboutViewModel by viewModels()

    private var showHotUpdateDialog by mutableStateOf(false)
    private var isHotUpdateDownloading by mutableStateOf(false)
    private var showApkUpdateDialog by mutableStateOf(false)
    private var apkUpdateInfo by mutableStateOf<ApkUpdateInfo?>(null)
    private var apkDownloading by mutableStateOf(false)
    private var apkErrorText by mutableStateOf<String?>(null)

    private var pendingApkUpdateInfo: ApkUpdateInfo? = null
    private var apkProgress by mutableStateOf<Float?>(null)

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        init()

        setContent {
            AHUTheme {
                val navController = rememberNavController()
                var isReLoginDialogShown by rememberSaveable { mutableStateOf(false) }

                if (showHotUpdateDialog) {
                    HotUpdateDialog(
                        isDownloading = isHotUpdateDownloading,
                        onConfirm = {
                            // 调用 SDK 的重启逻辑
                            RustSDK.restartApp(this)
                        }
                    )
                }
                if (showApkUpdateDialog && apkUpdateInfo != null) {
                    ApkUpdateDialog(
                        info = apkUpdateInfo!!,
                        downloading = apkDownloading,
                        progress = apkProgress,
                        errorText = apkErrorText,
                        onConfirm = {
                            ensureInstallPermissionThen {
                                startDownloadAndInstallApk(apkUpdateInfo!!)
                            }
                        },
                        onDismiss = {
                            showApkUpdateDialog = false
                        }
                    )
                }


                Main(
                    navController = navController,
                    loginViewModel = loginViewModel,
                    discoveryViewModel = discoveryViewModel,
                    scheduleViewModel = scheduleViewModel,
                    aboutViewModel = aboutViewModel,
                    isReLoginShown = isReLoginDialogShown,
                    onReLoginDismiss = { isReLoginDialogShown = false }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            packageManager.canRequestPackageInstalls()
        ) {
            pendingApkUpdateInfo?.let { info ->
                pendingApkUpdateInfo = null
                startDownloadAndInstallApk(info)
            }
        }
    }

    private fun init() {
        var hotUpdateApplied = java.util.concurrent.atomic.AtomicBoolean(false)

        RustSDK.loadLibrary(
            context = this,
            onHotUpdateFound = {
                isHotUpdateDownloading = true
                showHotUpdateDialog = true
            },
            onHotUpdateSuccess = {
                hotUpdateApplied.set(true)
                isHotUpdateDownloading = false
                showHotUpdateDialog = true
            },
            onHotUpdateFinished = {
                if (!hotUpdateApplied.get()) {
                    showHotUpdateDialog = false
                    isHotUpdateDownloading = false
                    checkApkUpdateOnStartup()
                }
            }
        )

        if (AHUCache.isLogin()) {
            val user = AHUCache.getCurrentUser()
            val pwd = AHUCache.getWisdomPassword()

            discoveryViewModel.loadActivityBean()
            scheduleViewModel.loadConfig()
            scheduleViewModel.refreshSchedule()

            // 启动时自动登录 Rust SDK，以确保 Session 有效（解决覆盖安装或重启后余额不显示问题）
            lifecycleScope.launch(Dispatchers.IO) {
                if (user != null && !pwd.isNullOrEmpty()) {
                    RustSDK.loginSafe(user.name, pwd)
                }
                
                withContext(Dispatchers.Main) {
                    // 登录后再次刷新，确保获取最新数据（如果之前的请求因未登录失败）
                    discoveryViewModel.loadActivityBean()
                    scheduleViewModel.loadConfig()
                    scheduleViewModel.refreshSchedule()
                }
            }
        }
    }
    private fun checkApkUpdateOnStartup() {
        Log.i("ApkUpdate", "start checkApkUpdateOnStartup")

        lifecycleScope.launch(Dispatchers.IO) {
            if (!RustSDK.isNativeLoaded()) {
                Log.w("ApkUpdate", "native not loaded, skip apk update check")
                return@launch
            }

            val result = RustSDK.checkApkUpdateSafe(this@MainActivity)
            result.onSuccess { info ->
                Log.i(
                    "ApkUpdate",
                    "check result: update=${info.update}, force=${info.force}, " +
                            "remoteVersionCode=${info.versionCode}, versionName=${info.versionName}"
                )
                if (info.update) {
                    withContext(Dispatchers.Main) {
                        apkUpdateInfo = info
                        apkErrorText = null
                        showApkUpdateDialog = true
                    }
                }
            }.onFailure { e ->
                Log.w("ApkUpdate", "checkApkUpdateSafe failed: ${e.message}", e)
            }
        }
    }

    private fun ensureInstallPermissionThen(action: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            action()
            return
        }

        val canInstall = packageManager.canRequestPackageInstalls()
        Log.i("ApkUpdate", "canRequestPackageInstalls=$canInstall")

        if (canInstall) {
            action()
            return
        }

        pendingApkUpdateInfo = apkUpdateInfo

        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun startDownloadAndInstallApk(info: ApkUpdateInfo) {
        if (apkDownloading) {
            Log.w("ApkUpdate", "download already in progress, ignore click")
            return
        }

        Log.i(
            "ApkUpdate",
            "start download: versionCode=${info.versionCode}, url=${info.url}"
        )

        apkDownloading = true
        apkErrorText = null

        lifecycleScope.launch(Dispatchers.IO) {
            val result = RustSDK.downloadApkToFileWithProgress(this@MainActivity, info) { downloaded, total ->
                lifecycleScope.launch(Dispatchers.Main) {
                    apkProgress = if (total > 0) downloaded.toFloat() / total.toFloat() else null
                }
            }

            withContext(Dispatchers.Main) {
                apkDownloading = false
                apkProgress = null

                result.onSuccess { apkFile ->
                    Log.i(
                        "ApkUpdate",
                        "download success: path=${apkFile.absolutePath}, size=${apkFile.length()}"
                    )
                    runCatching {
                        installApk(apkFile, info)
                        Log.i("ApkUpdate", "install intent started")
                        if (!info.force) showApkUpdateDialog = false
                    }.onFailure { t ->
                        Log.e("ApkUpdate", "installApk failed", t)
                        apkErrorText = t.message ?: "安装启动失败"
                    }
                }.onFailure { e ->
                    Log.e("ApkUpdate", "download failed", e)
                    apkErrorText = e.message ?: "下载失败"
                }
            }
        }
    }

    private fun installApk(apkFile: File, info: ApkUpdateInfo) {
        Log.i("ApkUpdate", "installApk called, file=${apkFile.absolutePath}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canInstall = packageManager.canRequestPackageInstalls()
            Log.i("ApkUpdate", "canRequestPackageInstalls=$canInstall")

            if (!canInstall) {
                pendingApkUpdateInfo = info
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:$packageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
                return
            }
        }

        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            apkFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            startActivity(intent)
            Log.i("ApkUpdate", "install intent started")
        } catch (e: Exception) {
            Log.e("ApkUpdate", "start install activity failed", e)
            throw e
        }
    }

}
