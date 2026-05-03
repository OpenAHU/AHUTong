package com.ahu.ahutong.ui.state

import android.content.Context
import android.webkit.CookieManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.BuildConfig
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.server.AhuTong
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
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest

class MainViewModel : ViewModel() {

    companion object {
        private val apkDownloadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    // App update UI states
    var showApkUpdateDialog = mutableStateOf(false)
    var apkUpdateInfo = mutableStateOf<ApkUpdateInfo?>(null)
    var apkDownloading = mutableStateOf(false)
    var apkProgress = mutableStateOf<Float?>(null)
    var apkErrorText = mutableStateOf<String?>(null)
    var downloadedApkFile = mutableStateOf<File?>(null)
    /** 本地已存在目标版本 APK，可直接安装 */
    var apkLocalReady = mutableStateOf(false)

    private var apkDownloadJob: Job? = null
    private var installAfterApkDownload = false

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
        if (info.versionCode <= BuildConfig.VERSION_CODE) return@withContext

        // 3. 检查本地是否已有该版本的 APK，并校验 sha256
        val localApk = File(dir, "update-${info.versionCode}.apk")
        val localReady = if (localApk.exists() && localApk.length() > 0) {
            if (!info.sha256.isNullOrEmpty()) {
                val localHash = runCatching { sha256Of(localApk) }.getOrNull()
                val match = localHash.equals(info.sha256, ignoreCase = true)
                if (!match) {
                    Log.w("ApkUpdate", "local APK sha256 mismatch, expected=${info.sha256}, got=$localHash, deleting")
                    localApk.delete()
                }
                match
            } else true
        } else false

        withContext(Dispatchers.Main) {
            apkUpdateInfo.value = info
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
        val info = apkUpdateInfo.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val dir = context.getExternalFilesDir(null) ?: context.filesDir
            val localApk = File(dir, "update-${info.versionCode}.apk")
            if (!localApk.exists() || localApk.length() <= 0) {
                withContext(Dispatchers.Main) {
                    apkLocalReady.value = false
                    apkErrorText.value = "本地文件已丢失，请重新下载"
                }
                return@launch
            }
            // 校验 sha256
            if (!info.sha256.isNullOrEmpty()) {
                val localHash = runCatching { sha256Of(localApk) }.getOrNull()
                if (!localHash.equals(info.sha256, ignoreCase = true)) {
                    Log.w("ApkUpdate", "install: sha256 mismatch, deleting corrupt APK")
                    localApk.delete()
                    withContext(Dispatchers.Main) {
                        apkLocalReady.value = false
                        apkErrorText.value = "本地文件已损坏，请重新下载"
                    }
                    return@launch
                }
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
        val info = apkUpdateInfo.value ?: return
        if (apkDownloading.value) return
        installAfterApkDownload = installAfterDownload
        val appContext = context.applicationContext

        if (!forceRedownload) {
            // 检查本地是否已存在该版本 APK 并校验完整性（IO 安全）
            apkDownloadScope.launch {
                val dir = appContext.getExternalFilesDir(null) ?: appContext.filesDir
                val existingApk = File(dir, "update-${info.versionCode}.apk")
                if (existingApk.exists() && existingApk.length() > 0) {
                    // 校验 sha256
                    if (!info.sha256.isNullOrEmpty()) {
                        val localHash = runCatching { sha256Of(existingApk) }.getOrNull()
                        if (!localHash.equals(info.sha256, ignoreCase = true)) {
                            Log.w("ApkUpdate", "cached APK sha256 mismatch, deleting")
                            existingApk.delete()
                            withContext(Dispatchers.Main) {
                                apkLocalReady.value = false
                                doApkDownload(appContext, info)
                            }
                            return@launch
                        }
                    }
                    Log.i("ApkUpdate", "APK already exists locally: ${existingApk.absolutePath}")
                    withContext(Dispatchers.Main) {
                        apkLocalReady.value = true
                        if (installAfterApkDownload) {
                            downloadedApkFile.value = existingApk
                        }
                    }
                    return@launch
                }
                withContext(Dispatchers.Main) { doApkDownload(appContext, info) }
            }
        } else {
            // 强制重新下载：先删除本地缓存
            apkDownloadScope.launch {
                val dir = appContext.getExternalFilesDir(null) ?: appContext.filesDir
                val existingApk = File(dir, "update-${info.versionCode}.apk")
                existingApk.delete()
                withContext(Dispatchers.Main) {
                    apkLocalReady.value = false
                    doApkDownload(appContext, info)
                }
            }
        }
    }

    private fun doApkDownload(context: Context, info: ApkUpdateInfo) {
        if (info.url.isNullOrEmpty()) {
            apkErrorText.value = "下载地址为空"
            return
        }
        apkDownloading.value = true
        apkErrorText.value = null
        apkProgress.value = null

        apkDownloadJob = apkDownloadScope.launch {
            try {
                val body = AhuTong.API.downloadByUrl(info.url!!)
                val total = body.contentLength()
                withContext(Dispatchers.Main) {
                    apkProgress.value = if (total > 0) 0f else null
                }

                val dir = context.getExternalFilesDir(null) ?: context.filesDir
                val outFile = File(dir, "update-${info.versionCode}.apk")

                var completed = 0L
                var lastEmit = System.currentTimeMillis()
                body.byteStream().use { input: InputStream ->
                    FileOutputStream(outFile).use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var read = input.read(buffer)
                        while (read >= 0) {
                            output.write(buffer, 0, read)
                            completed += read
                            if (total > 0) {
                                val now = System.currentTimeMillis()
                                if (now - lastEmit >= 100 || completed == total) {
                                    val prog = (completed.toDouble() / total.toDouble()).coerceIn(0.0, 1.0)
                                    withContext(Dispatchers.Main) {
                                        apkProgress.value = prog.toFloat()
                                    }
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
                    outFile.delete()
                    withContext(Dispatchers.Main) {
                        apkDownloading.value = false
                        apkProgress.value = null
                        apkErrorText.value = "下载不完整（${completed}/${total}），请重试"
                    }
                    return@launch
                }

                // 校验 sha256
                if (!info.sha256.isNullOrEmpty()) {
                    val hash = runCatching { sha256Of(outFile) }.getOrNull()
                    if (!hash.equals(info.sha256, ignoreCase = true)) {
                        Log.w("ApkUpdate", "download sha256 mismatch: expected=${info.sha256}, got=$hash")
                        outFile.delete()
                        withContext(Dispatchers.Main) {
                            apkDownloading.value = false
                            apkProgress.value = null
                            apkErrorText.value = "文件校验失败，请重试"
                        }
                        return@launch
                    }
                }

                withContext(Dispatchers.Main) {
                    apkDownloading.value = false
                    apkProgress.value = null
                    apkLocalReady.value = true
                    if (installAfterApkDownload) {
                        downloadedApkFile.value = outFile
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    apkDownloading.value = false
                    apkProgress.value = null
                    apkErrorText.value = e.message ?: "下载失败"
                }
            }
        }
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
