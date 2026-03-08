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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.mutableStateOf
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MainViewModel : ViewModel() {

    // App update UI states
    var showApkUpdateDialog = mutableStateOf(false)
    var apkUpdateInfo = mutableStateOf<ApkUpdateInfo?>(null)
    var apkDownloading = mutableStateOf(false)
    var apkProgress = mutableStateOf<Float?>(null)
    var apkErrorText = mutableStateOf<String?>(null)
    var downloadedApkFile = mutableStateOf<File?>(null)

    private var apkDownloadJob: Job? = null

    fun checkApkUpdate() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { AhuTong.API.getApkUpdateInfo() }
                .onSuccess { info ->
                    if (info.versionCode != BuildConfig.VERSION_CODE) {
                        withContext(Dispatchers.Main) {
                            apkUpdateInfo.value = info
                            apkErrorText.value = null
                            showApkUpdateDialog.value = true
                        }
                    }
                }
        }
    }

    fun startApkDownload(context: Context) {
        val info = apkUpdateInfo.value ?: return
        if (apkDownloading.value) return
        if (info.url.isNullOrEmpty()) {
            apkErrorText.value = "下载地址为空"
            return
        }
        apkDownloading.value = true
        apkErrorText.value = null
        apkProgress.value = null

        apkDownloadJob = viewModelScope.launch(Dispatchers.IO) {
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

                withContext(Dispatchers.Main) {
                    apkDownloading.value = false
                    apkProgress.value = null
                    downloadedApkFile.value = outFile
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
