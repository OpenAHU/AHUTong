package com.ahu.ahutong

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.ahu.ahutong.appwidget.WidgetUpdateScheduler
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.ext.launchSafe
import com.ahu.ahutong.sdk.LocalServiceClient
import com.ahu.ahutong.sdk.RustSDK
import com.ahu.ahutong.ui.component.ApkUpdateDialog
import com.ahu.ahutong.ui.screen.Main
import com.ahu.ahutong.ui.state.AboutViewModel
import com.ahu.ahutong.ui.state.DiscoveryViewModel
import com.ahu.ahutong.ui.state.LoginViewModel
import com.ahu.ahutong.ui.state.MainViewModel
import com.ahu.ahutong.ui.state.ScheduleViewModel
import com.ahu.ahutong.ui.theme.AHUTheme
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.security.MessageDigest

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val TAG = "MainActivity"

    private val mainViewModel: MainViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private val discoveryViewModel: DiscoveryViewModel by viewModels()
    private val scheduleViewModel: ScheduleViewModel by viewModels()
    private val aboutViewModel: AboutViewModel by viewModels()


    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initializeActivityResultLauncher()
        init()

        setContent {
            AHUTheme {
                val navController = rememberNavController()
                var isReLoginDialogShown by rememberSaveable { mutableStateOf(false) }

//                if (showHotUpdateDialog) {
//                    HotUpdateDialog(
//                        isDownloading = isHotUpdateDownloading,
//                        onConfirm = {
//                            // 调用 SDK 的重启逻辑
//                            RustSDK.restartApp(this)
//                        }
//                    )
//                }
                if (mainViewModel.showApkUpdateDialog.value && mainViewModel.apkUpdateInfo.value != null) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    ApkUpdateDialog(
                        info = mainViewModel.apkUpdateInfo.value!!,
                        downloading = mainViewModel.apkDownloading.value,
                        progress = mainViewModel.apkProgress.value,
                        errorText = mainViewModel.apkErrorText.value,
                        apkLocalReady = mainViewModel.apkLocalReady.value,
                        onConfirm = {
                            mainViewModel.startApkDownload(
                                this@MainActivity,
                                installAfterDownload = true
                            )
                        },
                        onInstallLocal = {
                            mainViewModel.installLocalApk(this@MainActivity)
                        },
                        onRedownload = {
                            mainViewModel.startApkDownload(this@MainActivity, forceRedownload = true)
                        },
                        onDismiss = {
                            mainViewModel.showApkUpdateDialog.value = false
                        },
                        onCancel = {
                            mainViewModel.continueApkDownloadInBackground()
                            Toast.makeText(this@MainActivity, "已转到后台下载", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                val downloaded = mainViewModel.downloadedApkFile.value
                if (downloaded != null) {
                    androidx.compose.runtime.LaunchedEffect(downloaded) {
                        ensureInstallPermissionThen {
                            installApk(downloaded)
                        }
                        mainViewModel.markInstallHandled()
                    }
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

    private fun init() {
        lifecycleScope.launchSafe {
            mainViewModel.checkApkUpdate(this@MainActivity)
        }
        WidgetUpdateScheduler.scheduleNext(this@MainActivity)

        RustSDK.loadLibrary(context = applicationContext)

        // 在 native library 加载后启动本地 HTTP 服务
        startLocalService()



        if (AHUCache.isLogin()) {
//            val user = AHUCache.getCurrentUser()
//            val pwd = AHUCache.getWisdomPassword()


            discoveryViewModel.loadActivityBean()
            scheduleViewModel.loadConfig()
            scheduleViewModel.refreshSchedule()


        }
    }
//    private fun checkApkUpdateOnStartup() {
//        Log.i("ApkUpdate", "start checkApkUpdateOnStartup")
//
//        lifecycleScope.launch(Dispatchers.IO) {
//            if (!RustSDK.isNativeLoaded()) {
//                Log.w("ApkUpdate", "native not loaded, skip apk update check")
//                return@launch
//            }
//
//            val result = RustSDK.checkApkUpdateSafe(this@MainActivity)
//            result.onSuccess { info ->
//                Log.i(
//                    "ApkUpdate",
//                    "check result: update=${info.update}, force=${info.force}, " +
//                            "remoteVersionCode=${info.versionCode}, versionName=${info.versionName}"
//                )
//                if (info.update) {
//                    withContext(Dispatchers.Main) {
//                        apkUpdateInfo = info
//                        apkErrorText = null
//                        showApkUpdateDialog = true
//                    }
//                }
//            }.onFailure { e ->
//                Log.w("ApkUpdate", "checkApkUpdateSafe failed: ${e.message}", e)
//            }
//        }
//    }
//

    private lateinit var requestInstallPermissionLauncher: ActivityResultLauncher<Intent>

    private fun initializeActivityResultLauncher() {
        requestInstallPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                // Settings 页面返回的 resultCode 在不同 ROM 上不可靠，按真实权限状态判断
                Log.i("ApkUpdate", "permission resultCode=${result.resultCode}")
                val canInstall = packageManager.canRequestPackageInstalls()
                Log.i("ApkUpdate", "canRequestPackageInstalls after permission=$canInstall")
                if (canInstall) {
                    pendingInstallAction?.invoke()
                    pendingInstallAction = null
                } else {
                    Toast.makeText(this, "未授权安装权限，安装失败", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private var pendingInstallAction: (() -> Unit)? = null

    // 3. 处理安装权限请求
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

        // 保存待执行的action
        pendingInstallAction = action

        // 请求权限
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
            data = Uri.parse("package:$packageName")
        }

        // 使用 ActivityResultLauncher 启动设置页面
        requestInstallPermissionLauncher.launch(intent)
    }

    // Update download logic moved into MainViewModel

    private fun installApk(apkFile: File) {
        Log.i("ApkUpdate", "installApk called, file=${apkFile.absolutePath}")

        validateApkBeforeInstall(apkFile)?.let { error ->
            Log.w("ApkUpdate", "blocked APK install: $error")
            mainViewModel.reportApkInstallError(error)
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            apkFile.delete()
            return
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

    private fun validateApkBeforeInstall(apkFile: File): String? {
        if (!apkFile.exists() || apkFile.length() <= 0L) {
            return "安装包不存在或为空"
        }

        val canonicalApk = runCatching { apkFile.canonicalFile }.getOrElse {
            return "安装包路径无效"
        }
        val trustedDirs = listOfNotNull(getExternalFilesDir(null), filesDir).mapNotNull {
            runCatching { it.canonicalFile }.getOrNull()
        }
        val isInTrustedDir = trustedDirs.any { dir ->
            canonicalApk.path == dir.path || canonicalApk.path.startsWith(dir.path + File.separator)
        }
        if (!isInTrustedDir) {
            return "安装包位置不可信"
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            @Suppress("DEPRECATION")
            PackageManager.GET_SIGNATURES
        }

        val archiveInfo = packageManager.getPackageArchiveInfo(canonicalApk.absolutePath, flags)
            ?: return "安装包解析失败"
        if (archiveInfo.packageName != packageName) {
            return "安装包包名不匹配"
        }

        if (versionCodeOf(archiveInfo) <= currentVersionCode()) {
            return "安装包版本不高于当前版本"
        }

        if (!hasMatchingSigningCertificate(archiveInfo, flags)) {
            return "安装包签名与当前应用不一致"
        }

        return null
    }

    private fun hasMatchingSigningCertificate(archiveInfo: PackageInfo, flags: Int): Boolean {
        val installedInfo = try {
            packageManager.getPackageInfo(packageName, flags)
        } catch (e: Exception) {
            Log.w("ApkUpdate", "failed to read installed package signatures", e)
            return false
        }

        val archiveSigners = signatureDigests(archiveInfo, includeHistory = false)
        val installedSigners = signatureDigests(installedInfo, includeHistory = false)
        if (archiveSigners.isEmpty() || installedSigners.isEmpty()) return false

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return archiveSigners == installedSigners
        }

        val archiveSigningInfo = archiveInfo.signingInfo ?: return false
        val installedSigningInfo = installedInfo.signingInfo ?: return false
        if (archiveSigningInfo.hasMultipleSigners() || installedSigningInfo.hasMultipleSigners()) {
            return archiveSigners == installedSigners
        }

        val archiveHistory = signatureDigests(archiveInfo, includeHistory = true)
        val installedHistory = signatureDigests(installedInfo, includeHistory = true)
        return archiveSigners.any { it in installedHistory } ||
            installedSigners.any { it in archiveHistory }
    }

    private fun signatureDigests(info: PackageInfo, includeHistory: Boolean): Set<String> {
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val signingInfo = info.signingInfo ?: return emptySet()
            if (includeHistory && !signingInfo.hasMultipleSigners()) {
                signingInfo.signingCertificateHistory ?: signingInfo.apkContentsSigners
            } else {
                signingInfo.apkContentsSigners
            }
        } else {
            @Suppress("DEPRECATION")
            info.signatures
        } ?: return emptySet()

        return signatures.map { signature ->
            MessageDigest.getInstance("SHA-256")
                .digest(signature.toByteArray())
                .joinToString("") { "%02x".format(it) }
        }.toSet()
    }

    private fun currentVersionCode(): Long {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        return versionCodeOf(packageInfo)
    }

    private fun versionCodeOf(info: PackageInfo): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong()
        }
    }

    /**
     * 启动 Rust 本地 HTTP 服务
     */
    private fun startLocalService() {
        if (!RustSDK.isNativeLoaded()) {
            Log.w("MainActivity", "Native library not loaded, skipping local service start")
            return
        }

        try {
            val result = RustSDK.startServer(0)
            Log.i("MainActivity", "startServer result: $result")

            if (result.contains("\"error\"")) {
                Log.e("MainActivity", "Failed to start local server: $result")
                return
            }

            val json = org.json.JSONObject(result)
            val port = json.getInt("port")
            val token = json.getString("token")

            LocalServiceClient.initialize(port, token)
            Log.i("MainActivity", "Local service started on port: $port")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to start local service", e)
        }
    }

    // update check moved into MainViewModel


}
