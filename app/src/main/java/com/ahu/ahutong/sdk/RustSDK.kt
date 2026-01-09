package com.ahu.ahutong.sdk

import android.content.Context
import android.util.Log
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.annotation.Keep
import com.ahu.ahutong.data.model.Exam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import androidx.core.content.edit
import com.ahu.ahutong.data.crawler.model.jwxt.GradeResponse
import kotlinx.coroutines.withContext
import android.content.Intent
import android.content.ContentValues
import android.provider.MediaStore
import android.os.Build
import android.os.Environment
import java.io.FileInputStream
import kotlin.system.exitProcess
import org.conscrypt.Conscrypt
import java.security.Security


/**
 * @Author Yukon
 * @Email 605606366@qq.com
 */

/**
 * Rust SDK 的 Kotlin 封装层
 * 负责加载 native 库并提供类型安全的接口
 */
@Keep
object RustSDK {

    private const val TAG_HOTUPDATE = "HotUpdate"
    private const val LIB_NAME = "libahutong_rs.so"
    private var isLoaded = false
    private val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

    init {
        // Keep Conscrypt as well
        Security.insertProviderAt(Conscrypt.newProvider(), 1)
    }

    fun isNativeLoaded(): Boolean = isLoaded


    /**
     * 加载 Library
     * @param context 上下文
     * @param onHotUpdateFound 当发现热更新并准备下载时，回调此函数。
     * @param onHotUpdateSuccess 当热更新下载成功并覆盖后，回调此函数。可以在这里处理弹窗逻辑。
     * @param onHotUpdateFinished 当热更新检查结束后，回调此函数。在这里调用ApkUpdate。
     */
    fun loadLibrary(
        context: Context,
        onHotUpdateFound: (() -> Unit)? = null,
        onHotUpdateSuccess: (() -> Unit)? = null,
        onHotUpdateFinished: (() -> Unit)? = null
    ){
        if (isLoaded) return

        try {
            // Check for App Update to prevent using outdated hot-update lib
            val prefs = context.getSharedPreferences("rust_sdk_config", Context.MODE_PRIVATE)
            val lastVersionCode = prefs.getLong("app_version_code", Context.MODE_PRIVATE.toLong())
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val currentVersionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }

            val libDir = context.getDir("jniLibs", Context.MODE_PRIVATE)
            val customLib = File(libDir, LIB_NAME)

            Log.d(TAG_HOTUPDATE, "Checking custom lib at: ${customLib.absolutePath}, exists: ${customLib.exists()}")

            if (currentVersionCode > lastVersionCode) {
                Log.i(TAG_HOTUPDATE, "App updated ($lastVersionCode -> $currentVersionCode), clearing hotUpdate libs")
                if (customLib.exists()) {
                    customLib.delete()
                }
                prefs.edit {putLong("app_version_code", currentVersionCode)}
            }

            if (customLib.exists()) {
                try {
                    // 尝试预加载 C++ 运行时，防止热更新 SO 找不到依赖
                    try { System.loadLibrary("c++_shared") } catch (e: Throwable) { Log.w(TAG_HOTUPDATE, "Failed to load c++_shared: ${e.message}") }
                    
                    System.load(customLib.absolutePath)
                    Log.i(TAG_HOTUPDATE, "HotUpdate: already load: ${customLib.absolutePath}")
                    isLoaded = true
                    checkUpdate(context, onHotUpdateFound, onHotUpdateSuccess, onHotUpdateFinished)
                    return
                } catch (e: Throwable) {
                    customLib.delete()
                    Log.e(TAG_HOTUPDATE, "HotUpdate: failed to load .so, corrupted files have been deleted. Error: ${e.message}", e)
                }
            }

            // back to .so in release
            System.loadLibrary("ahutong_rs")
            isLoaded = true
            Log.i(TAG_HOTUPDATE, "Native: load .so success")
            checkUpdate(context, onHotUpdateFound, onHotUpdateSuccess, onHotUpdateFinished)
        } catch (e: Throwable) {
            Log.e(TAG_HOTUPDATE, "Native: load .so failed", e)
            onHotUpdateFinished?.invoke()
        }
    }

    private fun getConscryptSocketFactory(): javax.net.ssl.SSLSocketFactory {
        val context = javax.net.ssl.SSLContext.getInstance("TLS", "Conscrypt")
        context.init(null, null, null) // 使用默认的 TrustManager，Conscrypt 会处理根证书
        return context.socketFactory
    }

    /**
     * 后台检查并下载更新.so文件
     */
    private fun checkUpdate(
        context: Context,
        onFound: (() -> Unit)? = null,
        onSuccess: (() -> Unit)? = null,
        onFinished: (() -> Unit)? = null
    ) {
        if (!android.os.Process.is64Bit()) {
            Log.w(TAG_HOTUPDATE, "current device is not a 64-bit environment, skip the hotUpdate")
            onFinished?.invoke()
            return
        }

        scope.launch(Dispatchers.IO) {
            try {
                val prefs = context.getSharedPreferences("rust_sdk_config", Context.MODE_PRIVATE)
                val currentVersion = prefs.getInt("so_version", 299)

                // Get original URL and Host from SDK
                val originalConfigUrl = getUpdateConfigUrl()
                val originalHost = try { URL(originalConfigUrl).host } catch (e: Exception) {
                    Log.w(TAG_HOTUPDATE, "Failed to parse host from config url", e)
                    ""
                }
                val serverIp = getApiServerIp()

                // Construct IP-based URL by replacing host
                val configUrl = originalConfigUrl.replace(originalHost, serverIp)

                Log.i(
                    TAG_HOTUPDATE,
                    "checkUpdate start. currentVersion=$currentVersion, url=$configUrl, " +
                            "is64Bit=${android.os.Process.is64Bit()}, thread=${Thread.currentThread().name}"
                )

                // 2) 发起网络请求（带完整日志）
                val startMs = System.currentTimeMillis()
                val jsonStr: String = try {
                    val url = URL(configUrl)
                    val conn = (url.openConnection() as java.net.HttpURLConnection).apply {

                        instanceFollowRedirects = true
                        connectTimeout = 5000
                        readTimeout = 5000
                        requestMethod = "GET"
                        useCaches = false

                        // 建议加上这些头，很多网关/防火墙对“空 UA”会更敏感
                        setRequestProperty("User-Agent", "RustSdkHotUpdate/1.0 (Android)")
                        setRequestProperty("Accept", "application/json")
                        setRequestProperty("Connection", "close")

                        if (this is javax.net.ssl.HttpsURLConnection) {
                            try {
                                this.sslSocketFactory = getConscryptSocketFactory()
                                this.hostnameVerifier = javax.net.ssl.HostnameVerifier { hostname, session ->
                                    if (hostname == serverIp) true else javax.net.ssl.HttpsURLConnection.getDefaultHostnameVerifier().verify(hostname, session)
                                }
                            } catch (e: Exception) {
                                Log.w(TAG_HOTUPDATE, "Failed to set Conscrypt factory", e)
                            }
                        }
                    }

                    // 触发真正连接/请求
                    val code = conn.responseCode
                    val finalUrl = runCatching { conn.url?.toString() }.getOrNull()

                    val headers = buildString {
                        for ((k, v) in conn.headerFields) {
                            if (k == null) continue
                            append(k).append(": ").append(v?.joinToString(";") ?: "").append("\n")
                        }
                    }

                    val stream =
                        if (code in 200..299) conn.inputStream
                        else conn.errorStream

                    val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                        ?: ""

                    val cost = System.currentTimeMillis() - startMs
                    Log.i(
                        TAG_HOTUPDATE,
                        "checkUpdate http done. code=$code cost=${cost}ms " +
                                "originUrl=$configUrl finalUrl=$finalUrl " +
                                "contentLength=${conn.contentLengthLong} " +
                                "contentType=${conn.contentType}"
                    )
                    Log.d(TAG_HOTUPDATE, "checkUpdate response headers:\n$headers")

                    // 只打印前 400 字符，避免日志爆炸
                    Log.d(
                        TAG_HOTUPDATE,
                        "checkUpdate response body (first 400 chars): ${body.take(400)}"
                    )

                    if (code !in 200..299) {
                        // 非 2xx 直接认为失败（避免后面 Gson 解析异常掩盖真实问题）
                        Log.w(
                            TAG_HOTUPDATE,
                            "checkUpdate non-2xx response. code=$code, body(first200)=${body.take(200)}"
                        )
                        return@launch
                    }

                    body
                } catch (e: Exception) {
                    val cost = System.currentTimeMillis() - startMs
                    // 这里用带堆栈的日志，定位 Connection reset / handshake / dns 会更清楚
                    Log.w(
                        TAG_HOTUPDATE,
                        "check update network error after ${cost}ms. url=$configUrl, ex=${e.javaClass.name}: ${e.message}",
                        e
                    )
                    return@launch
                }

                // 3) 解析 JSON（带保护日志）
                val config: UpdateConfig = try {
                    Gson().fromJson(jsonStr, UpdateConfig::class.java).let {
                        // Replace domain with IP for download url
                        it.copy(url = it.url.replace(originalHost, serverIp))
                    }.also {
                        Log.i(
                            TAG_HOTUPDATE,
                            "checkUpdate parsed config ok. remoteVersion=${it.version}, soUrl=${it.url.take(200)}"
                        )
                    }
                } catch (e: Exception) {
                    Log.e(
                        TAG_HOTUPDATE,
                        "checkUpdate parse json failed. url=$configUrl, json(first300)=${jsonStr.take(300)}",
                        e
                    )
                    return@launch
                }

                // 4) 版本判断 & 下载
                try {
                    if (config.version > currentVersion) {
                        Log.i(
                            TAG_HOTUPDATE,
                            "new version found. remote=${config.version} > local=$currentVersion. start download..."
                        )

                        withContext(Dispatchers.Main) {
                            onFound?.invoke()
                        }

                        val success = downloadAndSave(context, config.url, config.sha256, config.signature)

                        if (success) {
                            prefs.edit().putInt("so_version", config.version).apply()
                            Log.i(TAG_HOTUPDATE, "hotUpdate download success. saved version=${config.version}")

                            withContext(Dispatchers.Main) {
                                onSuccess?.invoke()
                            }
                        } else {
                            Log.w(TAG_HOTUPDATE, "hotUpdate downloadAndSave failed. remote=${config.version}")
                        }
                    } else {
                        Log.d(
                            TAG_HOTUPDATE,
                            "no update needed. local=$currentVersion, remote=${config.version}"
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG_HOTUPDATE, "checkUpdate post-process failed", e)
                }
            } finally {
                withContext(Dispatchers.Main) { onFinished?.invoke() }
            }
        }
    }


    /**
     * 下载并覆盖旧文件
     */
    private fun downloadAndSave(
        context: Context,
        urlStr: String,
        expectSha256Hex: String,
        signatureBase64: String
    ): Boolean {
        // 使用与 loadLibrary 一致的路径获取方式
        val libDir = context.getDir("jniLibs", Context.MODE_PRIVATE)
        val tempFile = File(libDir, "$LIB_NAME.tmp")
        val targetFile = File(libDir, LIB_NAME)

        try {
            if (tempFile.exists()) tempFile.delete()

            Log.i(TAG_HOTUPDATE, "native downloadUpdate: $urlStr -> ${tempFile.absolutePath}")

            val ok = try {
                downloadUpdate(urlStr, tempFile.absolutePath, expectSha256Hex, signatureBase64)
            } catch (t: Throwable) {
                Log.e(TAG_HOTUPDATE, "downloadUpdate native call failed", t)
                false
            }

            if (!ok || !tempFile.exists() || tempFile.length() <= 0L) {
                Log.e(TAG_HOTUPDATE, "downloadUpdate failed or temp file missing/empty")
                tempFile.delete()
                return false
            }

            if (targetFile.exists() && !targetFile.delete()) {
                Log.w(TAG_HOTUPDATE, "Failed to delete existing target file: ${targetFile.absolutePath}")
            }

            var moveSuccess = false
            if(tempFile.renameTo(targetFile)) {
                moveSuccess = true
            } else {
                Log.w(TAG_HOTUPDATE, "renameTo failed, trying copy...")
                try {
                    tempFile.inputStream().use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    tempFile.delete()
                    moveSuccess = true
                } catch (e: Exception) {
                    Log.e(TAG_HOTUPDATE, "Copy failed", e)
                }
            }

            if (!moveSuccess) {
                Log.e(TAG_HOTUPDATE, "Failed to move/copy file to target: ${targetFile.absolutePath}")
                return false
            }

            targetFile.setReadable(true, false)
            targetFile.setExecutable(true, false)

            Log.i(TAG_HOTUPDATE, "hotUpdate success! Saved to: ${targetFile.absolutePath}. take effect on the next startup")
            return true
        } catch (e: Exception) {
            Log.e(TAG_HOTUPDATE, "downloadAndSave failed", e)
            tempFile.delete()
            return false
        }
    }

    /**
     * 初始化 SDK
     * @param cookiesJson 之前保存的 Cookie JSON 字符串，如果没有则传空字符串
     */
    external fun init(cookiesJson: String)

    /**
     * 导出当前的 Cookies
     * @return JSON 格式的 Cookie 字符串
     */
    external fun dumpCookies(): String?

    /**
     * 登录 (底层 Rust 实现)
     * @return 返回 JSON 字符串：成功返回 User 对象 JSON，失败返回 {"error": "..."}
     */
    private external fun login(username: String, password: String): String

    /**
     * 获取课表 (底层 Rust 实现)
     * @return 返回 JSON 字符串：成功返回 List<Course> JSON，失败返回 {"error": "..."}
     */
    private external fun getSchedule(): String

    /**
     * 刷新 Token (底层 Rust 实现)
     * @return 成功返回 token 字符串，失败返回 "ERROR: ..."
     */
    external fun refreshToken(): String

    /**
     * 获取扁平化的 Cookie 列表 JSON (用于同步给 Android OkHttp)
     * @return JSON 数组字符串 [{"name": "...", ...}]
     */
    external fun getCookiesList(): String

    external fun getQrcode(): String?

    /**
     * 获取校园卡余额
     * @return JSON 字符串
     */
    external fun getBalance(): String?

    /**
     * 获取考试信息
     * @return JSON 字符串
     */
    external fun getExamInfo(): String

    /**
     * 获取成绩信息 (原始 JSON)
     * @return JSON 字符串
     */
    external fun getGrade(): String

    /**
     * 下载校历图片到指定路径
     * @param savePath 保存路径
     * @return 是否成功
     */
    external fun downloadSchoolCalendar(savePath: String): Boolean

    /**
     * 获取更新日志
     * @return String
     */
    @JvmStatic external fun getUpdateLog(): String

    /**
     * 获取版本号
     * @return String
     */
    @JvmStatic external fun getVersionName(): String

    /**
     * 获取热更新链接
     * @return String
     */
    @JvmStatic external fun getUpdateConfigUrl(): String

    /**
     * 获取服务端 ip
     * @return String
     */
    @JvmStatic external fun getApiServerIp(): String

    /**
     * 检查 APK 更新
     * @return String
     */
    @JvmStatic external fun checkApkUpdate(currentVersionCode: Long): String

    private external fun downloadUpdate(
        url: String,
        savePath: String,
        expectedSha256: String,
        signature: String
    ): Boolean

    private external fun downloadApkUpdate(
        url: String,
        savePath: String,
        expectedSha256: String,
        signature: String
    ): Boolean

    private external fun downloadApkUpdateWithProgress(
        url: String,
        savePath: String,
        expectedSha256: String,
        signature: String,
        callback: ProgressCallback
    ): Boolean

    /**
     * 获取已缓存的校历文件
     */
    fun getCachedSchoolCalendar(context: Context): File? {
        val dir = File(context.filesDir, "images")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "xiaoli.jpg")
        return if (file.exists()) file else null
    }

    suspend fun fetchSchoolCalendar(context: Context, onProgress: (Float) -> Unit): File? {
        return withContext(Dispatchers.IO) {
            try {
                // 优先使用 filesDir 保证持久化
                val dir = File(context.filesDir, "images")
                if (!dir.exists()) dir.mkdirs()
                val saveFile = File(dir, "xiaoli.jpg")

                if (saveFile.exists()) {
                    Log.d("RustSDK", "Found cached calendar: ${saveFile.absolutePath}")
                    onProgress(1.0f)
                    return@withContext saveFile
                }

                Log.d("RustSDK", "Downloading calendar to file: ${saveFile.absolutePath}")
                // Use Kotlin implementation to bypass SNI block
                val success = downloadSchoolCalendarKotlin(saveFile.absolutePath, onProgress)
                Log.d("RustSDK", "Download result: $success")
                if (success && saveFile.exists()) {
                    saveFile
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG_HOTUPDATE, "Failed to fetch calendar", e)
                null
            }
        }
    }

    private fun downloadSchoolCalendarKotlin(savePath: String, onProgress: (Float) -> Unit): Boolean {
        val serverIp = getApiServerIp()
        val urlStr = "https://$serverIp/download/xiaoli.jpg"
        return try {
            val conn = URL(urlStr).openConnection()
            if (conn is javax.net.ssl.HttpsURLConnection) {
                conn.hostnameVerifier = javax.net.ssl.HostnameVerifier { hostname, session ->
                    if (hostname == serverIp) true else javax.net.ssl.HttpsURLConnection.getDefaultHostnameVerifier().verify(hostname, session)
                }
            }
            val totalBytes = conn.contentLength
            var downloadedBytes = 0
            
            conn.getInputStream().use { input ->
                FileOutputStream(File(savePath)).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytes = input.read(buffer)
                    while (bytes >= 0) {
                        output.write(buffer, 0, bytes)
                        downloadedBytes += bytes
                        if (totalBytes > 0) {
                            onProgress(downloadedBytes.toFloat() / totalBytes)
                        }
                        bytes = input.read(buffer)
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG_HOTUPDATE, "Failed to download calendar (Kotlin fallback)", e)
            false
        }
    }

    fun saveImageToGallery(context: Context, imageFile: File) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "AHU_Calendar_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AHUTong")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val uri = context.contentResolver.insert(collection, values)
        uri?.let {
            context.contentResolver.openOutputStream(it).use { outputStream ->
                FileInputStream(imageFile).use { inputStream ->
                    inputStream.copyTo(outputStream!!)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(it, values, null, null)
            }
        }
    }

    fun downloadApkToFileWithProgress(
        context: Context,
        info: ApkUpdateInfo,
        onProgress: (downloaded: Long, total: Long) -> Unit
    ): Result<File> {
        if (!isLoaded) return Result.failure(IllegalStateException("Native library not loaded"))
        if (!info.update) return Result.failure(IllegalArgumentException("No update available"))
        if (info.url.isNullOrEmpty() || info.sha256.isNullOrEmpty() || info.signature.isNullOrEmpty()) {
            return Result.failure(IllegalArgumentException("Missing url/sha256/signature"))
        }

        val dir = context.getExternalFilesDir(null)
            ?: return Result.failure(Exception("External storage not available"))

        val outFile = File(dir, "update-${info.versionCode}.apk")

        val ok = try {
            downloadApkUpdateWithProgress(
                info.url!!,
                outFile.absolutePath,
                info.sha256!!,
                info.signature!!,
                object : ProgressCallback {
                    override fun onProgress(downloaded: Long, total: Long) {
                        onProgress(downloaded, total)
                    }
                }
            )
        } catch (t: Throwable) {
            return Result.failure(t)
        }

        return if (ok && outFile.exists() && outFile.length() > 0L) {
            Result.success(outFile)
        } else {
            Result.failure(Exception("downloadApkUpdateWithProgress failed"))
        }
    }



    // --- 高级封装接口 (解析 JSON 为对象) ---

    fun checkApkUpdateSafe(context: Context): Result<ApkUpdateInfo> {
        if (!isLoaded) return Result.failure(IllegalStateException("Native library not loaded"))

        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val currentVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            packageInfo.versionCode.toLong()
        }

        val json = try {
            checkApkUpdate(currentVersionCode)
        } catch (t: Throwable) {
            return Result.failure(t)
        }

        if (json.contains("\"error\"")) {
            return Result.failure(Exception(json))
        }

        return try {
            val info = Gson().fromJson(json, ApkUpdateInfo::class.java)
            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun downloadApkToFile(context: Context, info: ApkUpdateInfo): Result<File> {
        if (!isLoaded) return Result.failure(IllegalStateException("Native library not loaded"))
        if (!info.update) return Result.failure(IllegalArgumentException("No update available"))
        if (info.url.isNullOrEmpty() || info.sha256.isNullOrEmpty() || info.signature.isNullOrEmpty()) {
            return Result.failure(IllegalArgumentException("Missing url/sha256/signature"))
        }

        val dir = context.getExternalFilesDir(null)
            ?: return Result.failure(Exception("External storage not available"))

        // Cleanup old APKs
        try {
            dir.listFiles()?.forEach { file ->
                if (file.isFile && file.name.startsWith("update-") && file.name.endsWith(".apk")) {
                    val currentName = "update-${info.versionCode}.apk"
                    if (file.name != currentName) {
                        file.delete()
                        Log.i(TAG_HOTUPDATE, "Deleted old apk: ${file.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG_HOTUPDATE, "Failed to clean old apks", e)
        }

        val outFile = File(dir, "update-${info.versionCode}.apk")
        val ok = try {
            downloadApkUpdate(info.url, outFile.absolutePath, info.sha256!!, info.signature!!)
        } catch (t: Throwable) {
            return Result.failure(t)
        }

        return if (ok && outFile.exists() && outFile.length() > 0L) {
            Result.success(outFile)
        } else {
            Result.failure(Exception("downloadApkUpdate failed"))
        }
    }

    fun loginSafe(username: String, password: String): Result<User> {
        if (!isLoaded) return Result.failure(IllegalStateException("Native library not loaded"))
        Log.d("RustSDK", "Calling native login with username: $username, password length: ${password.length}")
        val json = try {
            login(username, password)
        } catch (t: Throwable) {
            return Result.failure(t)
        }
        Log.d("RustSDK", "Native login returned: $json")
        return if (json.contains("\"error\"")) {
            Result.failure(Exception(json)) // 简单处理，实际可解析 error 字段
        } else {
            try {
                val user = Gson().fromJson(json, User::class.java)
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun getScheduleSafe(): Result<List<Course>> {
        if (!isLoaded) {
            Log.e(TAG_HOTUPDATE, "getScheduleSafe: Native library not loaded")
            return Result.failure(IllegalStateException("Native library not loaded"))
        }
        val json = try {
            getSchedule()
        } catch (t: Throwable) {
            Log.e(TAG_HOTUPDATE, "getScheduleSafe: Native call failed", t)
            return Result.failure(t)
        }
        
        if (json.contains("\"error\"")) {
            Log.e(TAG_HOTUPDATE, "getScheduleSafe: Server returned error: $json")
            return Result.failure(Exception(json))
        } else {
            return try {
                val type = object : TypeToken<List<Course>>() {}.type
                val courses: List<Course> = Gson().fromJson(json, type)
                Log.d(TAG_HOTUPDATE, "getScheduleSafe: Success, parsed ${courses.size} courses")
                Result.success(courses)
            } catch (e: Exception) {
                Log.e(TAG_HOTUPDATE, "getScheduleSafe: JSON parse failed. JSON: $json", e)
                Result.failure(e)
            }
        }
    }

    fun getExamInfoSafe(): Result<List<Exam>> {
        if (!isLoaded) {
            Log.e(TAG_HOTUPDATE, "getExamInfoSafe: Native library not loaded")
            return Result.failure(IllegalStateException("Native library not loaded"))
        }
        val json = try {
            getExamInfo()
        } catch (t: Throwable) {
            Log.e(TAG_HOTUPDATE, "getExamInfoSafe: Native call failed", t)
            return Result.failure(t)
        }
        
        if (json.contains("\"error\"")) {
            Log.e(TAG_HOTUPDATE, "getExamInfoSafe: Server returned error: $json")
            return Result.failure(Exception(json))
        } else {
            return try {
                val type = object : TypeToken<List<Exam>>() {}.type
                val exams: List<Exam> = Gson().fromJson(json, type)
                Log.d(TAG_HOTUPDATE, "getExamInfoSafe: Success, parsed ${exams.size} exams")
                Result.success(exams)
            } catch (e: Exception) {
                Log.e(TAG_HOTUPDATE, "getExamInfoSafe: JSON parse failed. JSON: $json", e)
                Result.failure(e)
            }
        }
    }

    fun getGradeSafe(): Result<GradeResponse> {
        if (!isLoaded) {
            Log.e(TAG_HOTUPDATE, "getGradeSafe: Native library not loaded")
            return Result.failure(IllegalStateException("Native library not loaded"))
        }
        val json = try {
            getGrade()
        } catch (t: Throwable) {
            Log.e(TAG_HOTUPDATE, "getGradeSafe: Native call failed", t)
            return Result.failure(t)
        }
        
        if (json.contains("\"error\"")) {
            Log.e(TAG_HOTUPDATE, "getGradeSafe: Server returned error: $json")
            return Result.failure(Exception(json))
        } else {
            return try {
                val response = Gson().fromJson(json, GradeResponse::class.java)
                Log.d(TAG_HOTUPDATE, "getGradeSafe: Success, parsed data. semester map size: ${response.semesterId2studentGrades?.size}")
                Result.success(response)
            } catch (e: Exception) {
                Log.e(TAG_HOTUPDATE, "getGradeSafe: JSON parse failed. JSON: $json", e)
                Result.failure(e)
            }
        }
    }

    fun initSafe(cookiesJson: String) {
        if (!isLoaded) return
        try {
            init(cookiesJson)
        } catch (t: Throwable) {
            Log.e(TAG_HOTUPDATE, "Native init failed", t)
        }
    }

    fun getCookiesListSafe(): String {
        if (!isLoaded) return "[]"
        return try {
            getCookiesList()
        } catch (t: Throwable) {
            "[]"
        }
    }

    fun restartApp(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent?.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)

        // 杀掉当前进程
        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(0)
    }
}

@Keep
data class UpdateConfig(
    val version: Int,
    val url: String,
    val sha256: String,
    val signature: String,
    val alg: String
)

@Keep
data class ApkUpdateInfo(
    val update: Boolean,
    val force: Boolean,
    val versionCode: Long,
    val versionName: String,
    val changelog: String,
    val url: String? = null,
    val sha256: String? = null,
    val signature: String? = null
)
