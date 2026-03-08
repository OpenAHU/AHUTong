package com.ahu.ahutong

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import com.ahu.ahutong.ui.component.ApkUpdateDialog
import java.io.File
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.ahu.ahutong.data.mock_server.MockServer
import com.ahu.ahutong.data.server.AhuTong
import com.ahu.ahutong.data.server.model.ApkUpdateInfo
import com.ahu.ahutong.ext.launchSafe
import com.ahu.ahutong.sdk.LocalServiceClient
import okio.buffer
import okio.sink
import java.io.FileOutputStream
import java.io.InputStream
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val TAG = "MainActivity"

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
    private var apkDownloadJob: Job? = null

    private var pendingApkUpdateInfo: ApkUpdateInfo? = null
    private var apkProgress by mutableStateOf<Float?>(null)

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
                if (showApkUpdateDialog && apkUpdateInfo != null) {
                    ApkUpdateDialog(
                        info = apkUpdateInfo!!,
                        downloading = apkDownloading,
                        progress = apkProgress,
                        errorText = apkErrorText,
                        onConfirm = {
                            startDownloadAndInstallApk(apkUpdateInfo!!)
                        },
                        onDismiss = {
                            showApkUpdateDialog = false
                        },
                        onCancel = { cancelApkDownload() }
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

    private fun init() {
        lifecycleScope.launchSafe {
            checkApkUpdate()
        }


        var hotUpdateApplied = java.util.concurrent.atomic.AtomicBoolean(false)

//        RustSDK.loadLibrary(
//            context = this,
//            onHotUpdateFound = {
//                isHotUpdateDownloading = true
//                showHotUpdateDialog = true
//            },
//            onHotUpdateSuccess = {
//                hotUpdateApplied.set(true)
//                isHotUpdateDownloading = false
//                showHotUpdateDialog = true
//            },
//            onHotUpdateFinished = {
//                if (!hotUpdateApplied.get()) {
//                    showHotUpdateDialog = false
//                    isHotUpdateDownloading = false
//                    checkApkUpdateOnStartup()
//                }
//            }
//        )

        // 在 native library 加载后启动本地 HTTP 服务
//        startLocalService()



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
                // 权限请求结果回调
                Log.i("ApkUpdate", "permission=${result.resultCode}")
                if (result.resultCode == RESULT_OK) {
                    val canInstall = packageManager.canRequestPackageInstalls()
                    Log.i("ApkUpdate", "canRequestPackageInstalls after permission=$canInstall")
                    if (canInstall) {
                        // 执行待执行的action
                        pendingInstallAction?.invoke()
                        pendingInstallAction = null
                    }
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

    private fun startDownloadAndInstallApk(info: ApkUpdateInfo) {
        if (apkDownloading) {
            Log.w("ApkUpdate", "download already in progress, ignore click")
            return
        }

        if (info.url.isNullOrEmpty()) {
            apkErrorText = "下载地址为空"
            return
        }

        Log.i("ApkUpdate", "start download: versionCode=${info.versionCode}, url=${info.url}")

        apkDownloading = true
        apkErrorText = null

        apkDownloadJob = lifecycleScope.launch(Dispatchers.IO) {
            try {
                val body = AhuTong.API.downloadByUrl(info.url!!)
                val total = body.contentLength()
                val dir = getExternalFilesDir(null) ?: filesDir
                val outFile = File(dir, "update-${info.versionCode}.apk")

                var completed = 0L
                body.byteStream().use { input: InputStream ->
                    FileOutputStream(outFile).use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var read = input.read(buffer)
                        while (read >= 0 && this.isActive) {
                            output.write(buffer, 0, read)
                            completed += read
                            if (total > 0) {
                                withContext(Dispatchers.Main) {
                                    apkProgress = completed.toFloat() / total.toFloat()
                                }
                            }
                            read = input.read(buffer)
                        }
                        output.flush()
                    }
                }

                withContext(Dispatchers.Main) {
                    apkDownloading = false
                    apkProgress = null
                    if (!this@launch.isActive) {
                        apkErrorText = null
                        return@withContext
                    }
                    runCatching {
                        ensureInstallPermissionThen {
                            installApk(outFile)
                        }
                        if (!info.force) showApkUpdateDialog = false
                    }.onFailure { t ->
                        Log.e("ApkUpdate", "installApk failed", t)
                        apkErrorText = t.message ?: "安装启动失败"
                    }
                }
            } catch (e: Exception) {
                Log.e("ApkUpdate", "download failed", e)
                withContext(Dispatchers.Main) {
                    apkDownloading = false
                    apkProgress = null
                    apkErrorText = e.message ?: "下载失败"
                }
            }
        }
    }

    private fun cancelApkDownload() {
        if (apkDownloading) {
            apkDownloadJob?.cancel()
            apkDownloadJob = null
            apkDownloading = false
            apkProgress = null
            apkErrorText = null
        }
    }

    private fun installApk(apkFile: File) {
        Log.i("ApkUpdate", "installApk called, file=${apkFile.absolutePath}")

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

    suspend private fun checkApkUpdate() {
        try {
            val info = withContext(Dispatchers.IO) { AhuTong.API.getApkUpdateInfo() }
            Log.i(TAG, "checkApkUpdate: server=${info.versionCode}, local=${BuildConfig.VERSION_CODE}")
            if (info.versionCode != BuildConfig.VERSION_CODE) {
                withContext(Dispatchers.Main) {
                    apkUpdateInfo = info
                    apkErrorText = null
                    showApkUpdateDialog = true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkApkUpdate failed", e)
        }
    }


}
