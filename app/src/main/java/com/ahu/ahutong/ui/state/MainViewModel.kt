package com.ahu.ahutong.ui.state

import android.content.Context
import android.webkit.CookieManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.BuildConfig
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.server.AhuTong
import com.ahu.ahutong.data.server.ApkUpdatePolicy
import com.ahu.ahutong.data.server.model.ApkUpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import java.io.File
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest

class MainViewModel : ViewModel() {

    companion object {
        private val apkDownloadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private const val DOWNLOAD_BUFFER_SIZE = 64 * 1024
        private const val PROGRESS_MIN_INTERVAL_MS = 1_000L
        private const val PROGRESS_MIN_DELTA = 0.01f
    }

    // App update UI states
    var showApkUpdateDialog = mutableStateOf(false)
    var apkUpdateInfo = mutableStateOf<ApkUpdateInfo?>(null)
    var apkDownloading = mutableStateOf(false)
    var apkProgress = mutableStateOf<Float?>(null)
    var apkErrorText = mutableStateOf<String?>(null)
    var downloadedApkFile = mutableStateOf<File?>(null)
    var apkUpdateChecking = mutableStateOf(false)
    /** 本地已存在目标版本 APK，可直接安装 */
    var apkLocalReady = mutableStateOf(false)

    private var apkDownloadJob: Job? = null
    private var installAfterApkDownload = false
    private var showDialogWhenApkDownloadCompletes = false

    private val apkFileRegex = Regex("""^update-(\d+)\.apk$""")

    /** 计算文件 SHA-256，返回小写 hex，必须在 IO 线程调用 */
    private fun sha256Of(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8 * 1024)
            var read = input.read(buffer)
            while (read >= 0) {
                digest.update(buffer, 0, read)
                read = input.read(buffer)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * 启动时检查云端更新、清理残留 APK、检测本地缓存
     * 全部在 IO 线程执行，不阻塞主线程
     */
    suspend fun checkApkUpdate(context: Context) = withContext(Dispatchers.IO) {
        val dir = context.getExternalFilesDir(null) ?: context.filesDir

        // 1. 清理版本号 <= 当前版本的残留 APK（安全校验文件名）
        cleanStaleApks(dir)

        // 2. 从云端获取最新版本信息
        val info = runCatching { AhuTong.API.getApkUpdateInfo() }.getOrNull() ?: return@withContext
        val update = ApkUpdatePolicy.validate(info, BuildConfig.VERSION_CODE).getOrElse {
            Log.w("ApkUpdate", "ignore invalid APK update metadata: ${it.message}")
            return@withContext
        }

        // 3. 检查本地是否已有该版本的 APK，并校验 sha256
        val localApk = File(dir, "update-${update.info.versionCode}.apk")
        val localReady = if (localApk.exists() && localApk.length() > 0) {
            verifyCachedApk(localApk, update.sha256, "local APK")
        } else false

        withContext(Dispatchers.Main) {
            apkUpdateInfo.value = update.info
            apkErrorText.value = null
            apkLocalReady.value = localReady
            showApkUpdateDialog.value = true
            if (!localReady && !apkDownloading.value) {
                startApkDownload(context.applicationContext, installAfterDownload = false)
            }
        }
    }

    /**
     * 清理版本号 <= 当前版本的 APK，跳过不符合命名规范的文件
     */
    private fun cleanStaleApks(dir: File) {
        val files = dir.listFiles() ?: return
        for (file in files) {
            val match = apkFileRegex.matchEntire(file.name) ?: continue
            val versionCode = match.groupValues[1].toIntOrNull() ?: continue
            if (versionCode <= BuildConfig.VERSION_CODE) {
                Log.i("ApkUpdate", "deleting stale APK: ${file.name}")
                file.delete()
            }
        }
    }

    /**
     * 直接安装本地已缓存的 APK（用户点击"安装"按钮）
     */
    fun installLocalApk(context: Context) {
        val update = selectedValidatedUpdate() ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val dir = context.getExternalFilesDir(null) ?: context.filesDir
            val localApk = File(dir, "update-${update.info.versionCode}.apk")
            if (!localApk.exists() || localApk.length() <= 0) {
                withContext(Dispatchers.Main) {
                    apkLocalReady.value = false
                    apkErrorText.value = "本地文件已丢失，请重新下载"
                }
                return@launch
            }
            if (!verifyCachedApk(localApk, update.sha256, "install APK")) {
                withContext(Dispatchers.Main) {
                    apkLocalReady.value = false
                    apkErrorText.value = "本地文件已损坏，请重新下载"
                }
                return@launch
            }
            withContext(Dispatchers.Main) {
                downloadedApkFile.value = localApk
            }
        }
    }

    fun startApkDownload(
        context: Context,
        forceRedownload: Boolean = false,
        installAfterDownload: Boolean = false
    ) {
        val update = selectedValidatedUpdate() ?: return
        if (apkDownloading.value) return
        apkDownloading.value = true
        apkErrorText.value = null
        apkProgress.value = null
        installAfterApkDownload = installAfterDownload
        val appContext = context.applicationContext

        if (!forceRedownload) {
            // 检查本地是否已存在该版本 APK 并校验完整性（IO 安全）
            apkDownloadScope.launch {
                val dir = appContext.getExternalFilesDir(null) ?: appContext.filesDir
                val existingApk = File(dir, "update-${update.info.versionCode}.apk")
                if (existingApk.exists() && existingApk.length() > 0) {
                    if (!verifyCachedApk(existingApk, update.sha256, "cached APK")) {
                        withContext(Dispatchers.Main) {
                            apkLocalReady.value = false
                            doApkDownload(appContext, update)
                        }
                        return@launch
                    }
                    Log.i("ApkUpdate", "APK already exists locally: ${existingApk.absolutePath}")
                    withContext(Dispatchers.Main) {
                        apkDownloading.value = false
                        apkProgress.value = null
                        apkLocalReady.value = true
                        if (installAfterApkDownload) {
                            downloadedApkFile.value = existingApk
                        } else if (showDialogWhenApkDownloadCompletes) {
                            showDialogWhenApkDownloadCompletes = false
                            showApkUpdateDialog.value = true
                        }
                    }
                    return@launch
                }
                withContext(Dispatchers.Main) { doApkDownload(appContext, update) }
            }
        } else {
            // 强制重新下载：先删除本地缓存
            apkDownloadScope.launch {
                val dir = appContext.getExternalFilesDir(null) ?: appContext.filesDir
                val existingApk = File(dir, "update-${update.info.versionCode}.apk")
                existingApk.delete()
                withContext(Dispatchers.Main) {
                    apkLocalReady.value = false
                    doApkDownload(appContext, update)
                }
            }
        }
    }

    private fun doApkDownload(context: Context, update: ApkUpdatePolicy.ValidatedUpdate) {
        apkDownloading.value = true
        apkErrorText.value = null
        apkProgress.value = null

        apkDownloadJob = apkDownloadScope.launch {
            var tempFile: File? = null
            try {
                val response = AhuTong.API.downloadByUrl(update.downloadUrl)
                val finalUrl = response.raw().request.url.toString()
                ApkUpdatePolicy.validateDownloadUrl(finalUrl).getOrElse {
                    throw SecurityException("下载重定向到不受信任地址")
                }

                if (!response.isSuccessful) {
                    throw IOException("下载失败：HTTP ${response.code()}")
                }

                val body = response.body() ?: throw IOException("下载内容为空")
                val total = body.contentLength()
                if (total > ApkUpdatePolicy.MAX_APK_BYTES) {
                    throw IOException("安装包过大，请稍后重试")
                }

                withContext(Dispatchers.Main) {
                    apkProgress.value = if (total > 0) 0f else null
                }

                val dir = context.getExternalFilesDir(null) ?: context.filesDir
                val outFile = File(dir, "update-${update.info.versionCode}.apk")
                val partFile = File(dir, "${outFile.name}.part")
                tempFile = partFile
                partFile.delete()

                var completed = 0L
                var lastEmit = System.currentTimeMillis()
                var lastProgress = 0f
                if (total > 0) {
                    emitApkProgress(0f)
                }
                body.byteStream().use { input: InputStream ->
                    BufferedOutputStream(FileOutputStream(partFile), DOWNLOAD_BUFFER_SIZE).use { output ->
                        val buffer = ByteArray(DOWNLOAD_BUFFER_SIZE)
                        var read = input.read(buffer)
                        while (read >= 0) {
                            output.write(buffer, 0, read)
                            completed += read
                            if (completed > ApkUpdatePolicy.MAX_APK_BYTES) {
                                throw IOException("安装包超过大小限制")
                            }
                            if (total > 0) {
                                val now = System.currentTimeMillis()
                                val prog = (completed.toDouble() / total.toDouble())
                                    .coerceIn(0.0, 1.0)
                                    .toFloat()
                                if (prog - lastProgress >= PROGRESS_MIN_DELTA ||
                                    now - lastEmit >= PROGRESS_MIN_INTERVAL_MS ||
                                    completed == total
                                ) {
                                    emitApkProgress(prog)
                                    lastProgress = prog
                                    lastEmit = now
                                }
                            }
                            read = input.read(buffer)
                        }
                        output.flush()
                    }
                }

                // 校验下载完整性
                if (total > 0 && completed != total) {
                    throw IOException("下载不完整（${completed}/${total}），请重试")
                }

                val downloadedFile = partFile
                if (!downloadedFile.exists() || downloadedFile.length() <= 0L) {
                    throw IOException("下载内容为空")
                }

                // 校验 sha256
                val hash = runCatching { sha256Of(downloadedFile) }.getOrNull()
                if (!hash.equals(update.sha256, ignoreCase = true)) {
                    Log.w("ApkUpdate", "download sha256 mismatch: expected=${update.sha256}, got=$hash")
                    throw SecurityException("文件校验失败，请重试")
                }

                if (outFile.exists() && !outFile.delete()) {
                    throw IOException("无法替换旧安装包")
                }

                if (!downloadedFile.renameTo(outFile)) {
                    downloadedFile.inputStream().use { input ->
                        FileOutputStream(outFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    downloadedFile.delete()
                }

                withContext(Dispatchers.Main) {
                    apkDownloading.value = false
                    apkProgress.value = null
                    apkLocalReady.value = true
                    if (installAfterApkDownload) {
                        downloadedApkFile.value = outFile
                    } else if (showDialogWhenApkDownloadCompletes) {
                        showDialogWhenApkDownloadCompletes = false
                        showApkUpdateDialog.value = true
                    }
                }
            } catch (e: Exception) {
                tempFile?.delete()
                withContext(Dispatchers.Main) {
                    apkDownloading.value = false
                    apkProgress.value = null
                    apkErrorText.value = e.message ?: "下载失败"
                    if (showDialogWhenApkDownloadCompletes) {
                        showDialogWhenApkDownloadCompletes = false
                        showApkUpdateDialog.value = true
                    }
                }
            }
        }
    }

    private suspend fun emitApkProgress(progress: Float) {
        withContext(Dispatchers.Main.immediate) {
            apkProgress.value = progress.coerceIn(0f, 1f)
        }
    }

    fun continueApkDownloadInBackground() {
        if (apkDownloading.value) {
            showDialogWhenApkDownloadCompletes = true
        }
        showApkUpdateDialog.value = false
    }

    fun checkApkUpdateManually(
        context: Context,
        onResult: (String) -> Unit
    ) {
        if (apkUpdateChecking.value) {
            onResult("正在检查更新")
            return
        }

        apkUpdateChecking.value = true
        val appContext = context.applicationContext

        viewModelScope.launch(Dispatchers.IO) {
            val resultText = try {
                val dir = appContext.getExternalFilesDir(null) ?: appContext.filesDir
                cleanStaleApks(dir)

                val info = AhuTong.API.getApkUpdateInfo()
                val update = ApkUpdatePolicy.validate(info, BuildConfig.VERSION_CODE).getOrElse { error ->
                    return@launch finishManualUpdateCheck(
                        if (ApkUpdatePolicy.isNoUpdateFailure(error)) {
                            "已是最新版本"
                        } else {
                            "检查更新失败：${error.message ?: "更新信息无效"}"
                        },
                        onResult
                    )
                }

                val localApk = File(dir, "update-${update.info.versionCode}.apk")
                val localReady = if (localApk.exists() && localApk.length() > 0L) {
                    verifyCachedApk(localApk, update.sha256, "manual check local APK")
                } else {
                    false
                }

                withContext(Dispatchers.Main) {
                    apkUpdateInfo.value = update.info
                    apkErrorText.value = null
                    apkLocalReady.value = localReady
                    showApkUpdateDialog.value = true
                }

                "发现新版本 ${update.info.versionName}"
            } catch (e: Exception) {
                Log.w("ApkUpdate", "manual update check failed", e)
                "检查更新失败：${e.message ?: "请稍后重试"}"
            }

            finishManualUpdateCheck(resultText, onResult)
        }
    }

    private suspend fun finishManualUpdateCheck(
        resultText: String,
        onResult: (String) -> Unit
    ) {
        withContext(Dispatchers.Main) {
            apkUpdateChecking.value = false
            onResult(resultText)
        }
    }

    private fun selectedValidatedUpdate(): ApkUpdatePolicy.ValidatedUpdate? {
        val info = apkUpdateInfo.value ?: return null
        return ApkUpdatePolicy.validate(info, BuildConfig.VERSION_CODE).getOrElse {
            apkErrorText.value = it.message ?: "更新信息校验失败"
            apkLocalReady.value = false
            Log.w("ApkUpdate", "invalid APK update metadata: ${it.message}")
            null
        }
    }

    private fun verifyCachedApk(file: File, expectedSha256: String, label: String): Boolean {
        val hash = runCatching { sha256Of(file) }.getOrNull()
        val match = hash.equals(expectedSha256, ignoreCase = true)
        if (!match) {
            Log.w("ApkUpdate", "$label sha256 mismatch, expected=$expectedSha256, got=$hash, deleting")
            file.delete()
        }
        return match
    }

    fun reportApkInstallError(message: String) {
        apkLocalReady.value = false
        apkErrorText.value = message
    }

    fun markInstallHandled() {
        downloadedApkFile.value = null
    }

    fun logout() {
        AHUCache.logout()
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
    }
}
