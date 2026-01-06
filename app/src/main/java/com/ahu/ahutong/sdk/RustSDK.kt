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
import android.util.Base64
import java.io.FileInputStream
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import kotlin.system.exitProcess
import org.conscrypt.Conscrypt
import java.security.Security


/**
 * @Author Yukon
 * @Email 605606366@qq.com
 */
// TODO: 迁移到 rustsdk 中
/**
 * Rust SDK 的 Kotlin 封装层
 * 负责加载 native 库并提供类型安全的接口
 */
@Keep
object RustSDK {

    private const val TAG_HOTUPDATE = "HotUpdate"
    private const val LIB_NAME = "libahutong_rs.so"
    // Fallback IP to bypass SNI blocking of openahu.org
    private const val SERVER_IP = "118.25.8.226" 
    private var isLoaded = false
    private val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO + kotlinx.coroutines.SupervisorJob())
    private val bcProvider = org.bouncycastle.jce.provider.BouncyCastleProvider()

    init {
        // Keep Conscrypt as well
        Security.insertProviderAt(Conscrypt.newProvider(), 1)
    }

    fun isNativeLoaded(): Boolean = isLoaded


    /**
     * 加载 Library
     * @param context 上下文
     * @param onHotUpdateSuccess 当热更新下载成功并覆盖后，回调此函数。可以在这里处理弹窗逻辑。
     */
    fun loadLibrary(context: Context, onHotUpdateSuccess: (() -> Unit)? = null){
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
                    checkUpdate(context, onHotUpdateSuccess)
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
            checkUpdate(context, onHotUpdateSuccess)
        } catch (e: Throwable) {
            Log.e(TAG_HOTUPDATE, "Native: load .so failed", e)
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
    private fun checkUpdate(context: Context, onSuccess: (() -> Unit)? = null) {
        if (!android.os.Process.is64Bit()) {
            Log.w(TAG_HOTUPDATE, "current device is not a 64-bit environment, skip the hotUpdate")
            return
        }

        scope.launch(Dispatchers.IO) {
            val prefs = context.getSharedPreferences("rust_sdk_config", Context.MODE_PRIVATE)
            val currentVersion = prefs.getInt("so_version", 299)

            // Force using IP address directly and ignore native return value to ensure SNI bypass
            // val configUrl = runCatching { getUpdateConfigUrl() } ...
            val configUrl = "https://$SERVER_IP/api/check_update"

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
                                if (hostname == SERVER_IP) true else javax.net.ssl.HttpsURLConnection.getDefaultHostnameVerifier().verify(hostname, session)
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
                    it.copy(url = it.url.replace("openahu.org", SERVER_IP))
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
            Log.i(TAG_HOTUPDATE, "start downloading the new version .so: $urlStr to ${tempFile.absolutePath}")
            val conn = URL(urlStr).openConnection()
            if (conn is javax.net.ssl.HttpsURLConnection) {
                try {
                    conn.sslSocketFactory = getConscryptSocketFactory()
                    conn.hostnameVerifier = javax.net.ssl.HostnameVerifier { hostname, session ->
                        if (hostname == SERVER_IP) true else javax.net.ssl.HttpsURLConnection.getDefaultHostnameVerifier().verify(hostname, session)
                    }
                } catch (e: Exception) {
                    Log.w(TAG_HOTUPDATE, "Failed to set Conscrypt factory in download", e)
                }
            }

            conn.getInputStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            val localSha256 = sha256(tempFile).joinToString("") { "%02x".format(it) }
            if (!localSha256.equals(expectSha256Hex, ignoreCase = true)) {
                Log.e(TAG_HOTUPDATE, "SHA-256 mismatch")
                tempFile.delete()
                return false
            }

            if (!verifyEd25519(tempFile, signatureBase64)) {
                Log.e(TAG_HOTUPDATE, "Ed25519 signature verify failed")
                tempFile.delete()
                return false
            }

            if (targetFile.exists()) {
                if (!targetFile.delete()) {
                    Log.e(TAG_HOTUPDATE, "Failed to delete existing target file")
                }
            }
            
            var moveSuccess = false
            if (tempFile.renameTo(targetFile)) {
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
            Log.e(TAG_HOTUPDATE, "download failed", e)
            tempFile.delete()
            return false
        }
    }

    private fun getFileMD5(file: File): String {
        val md = java.security.MessageDigest.getInstance("MD5")
        file.inputStream().use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it} != -1) {
                md.update(buffer, 0, bytesRead)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
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



    suspend fun fetchSchoolCalendar(context: Context): File? {
        return withContext(Dispatchers.IO) {
            try {
                val cacheFile = File(context.cacheDir, "xiaoli_${System.currentTimeMillis()}.jpg")
                Log.d("RustSDK", "Downloading calendar to temp file: ${cacheFile.absolutePath}")
                // Use Kotlin implementation to bypass SNI block
                val success = downloadSchoolCalendarKotlin(cacheFile.absolutePath)
                Log.d("RustSDK", "Download result: $success")
                if (success && cacheFile.exists()) {
                    cacheFile
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG_HOTUPDATE, "Failed to fetch calendar", e)
                null
            }
        }
    }

    private fun downloadSchoolCalendarKotlin(savePath: String): Boolean {
        val urlStr = "https://$SERVER_IP/download/xiaoli.jpg"
        return try {
            val conn = URL(urlStr).openConnection()
            if (conn is javax.net.ssl.HttpsURLConnection) {
                conn.hostnameVerifier = javax.net.ssl.HostnameVerifier { hostname, session ->
                    if (hostname == SERVER_IP) true else javax.net.ssl.HttpsURLConnection.getDefaultHostnameVerifier().verify(hostname, session)
                }
            }
            conn.getInputStream().use { input ->
                FileOutputStream(File(savePath)).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG_HOTUPDATE, "Failed to download calendar (Kotlin fallback)", e)
            false
        }
    }

    suspend fun downloadSchoolCalendarToAlbum(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            val file = fetchSchoolCalendar(context)
            if (file != null) {
                saveImageToGallery(context, file)
                true
            } else {
                false
            }
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


    // --- 高级封装接口 (解析 JSON 为对象) ---

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
        if (!isLoaded) return Result.failure(IllegalStateException("Native library not loaded"))
        val json = try {
            getSchedule()
        } catch (t: Throwable) {
            return Result.failure(t)
        }
        return if (json.contains("\"error\"")) {
            Result.failure(Exception(json))
        } else {
            try {
                val type = object : TypeToken<List<Course>>() {}.type
                val courses: List<Course> = Gson().fromJson(json, type)
                Result.success(courses)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun getExamInfoSafe(): Result<List<Exam>> {
        if (!isLoaded) return Result.failure(IllegalStateException("Native library not loaded"))
        val json = try {
            getExamInfo()
        } catch (t: Throwable) {
            return Result.failure(t)
        }
        return if (json.contains("\"error\"")) {
            Result.failure(Exception(json))
        } else {
            try {
                val type = object : TypeToken<List<Exam>>() {}.type
                val exams: List<Exam> = Gson().fromJson(json, type)
                Result.success(exams)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun getGradeSafe(): Result<GradeResponse> {
        if (!isLoaded) return Result.failure(IllegalStateException("Native library not loaded"))
        val json = try {
            getGrade()
        } catch (t: Throwable) {
            return Result.failure(t)
        }
        return if (json.contains("\"error\"")) {
            Result.failure(Exception(json))
        } else {
            try {
                val response = Gson().fromJson(json, GradeResponse::class.java)
                Result.success(response)
            } catch (e: Exception) {
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

    private const val ED25519_PUBLIC_KEY_BASE64 = "MCowBQYDK2VwAyEAsQ2Fz04RzJgfvt/dsExlo44l3RFQ4JAMHGRrAn9IXNk="

    private fun sha256(file: File): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { fis ->
            val buf = ByteArray(8192)
            var len: Int
            while (fis.read(buf).also { len = it } > 0) {
                md.update(buf, 0, len)
            }
        }
        return md.digest()
    }

    private fun verifyEd25519(
        file: File,
        signatureBase64: String
    ): Boolean {
        return try {
            val publicKeyBytes = Base64.decode(
                ED25519_PUBLIC_KEY_BASE64,
                Base64.DEFAULT
            )

            val keySpec = X509EncodedKeySpec(publicKeyBytes)
            // Explicitly use Bouncy Castle for Ed25519
            val keyFactory = try {
                Log.d(TAG_HOTUPDATE, "Attempting to get KeyFactory for Ed25519 with BC provider instance")
                KeyFactory.getInstance("Ed25519", bcProvider)
            } catch (e: Exception) {
                Log.w(TAG_HOTUPDATE, "Ed25519 not found in BC, trying EdDSA", e)
                // Fallback if BC fails (unlikely if dependency is correct)
                KeyFactory.getInstance("EdDSA", bcProvider)
            }
            
            val publicKey = keyFactory.generatePublic(keySpec)

            val sig = try {
                Log.d(TAG_HOTUPDATE, "Attempting to get Signature for Ed25519 with BC provider instance")
                Signature.getInstance("Ed25519", bcProvider)
            } catch (e: Exception) {
                Log.w(TAG_HOTUPDATE, "Signature Ed25519 not found in BC, trying EdDSA", e)
                Signature.getInstance("EdDSA", bcProvider)
            }
            
            sig.initVerify(publicKey)

            sig.update(sha256(file))

            val signatureBytes = Base64.decode(signatureBase64, Base64.DEFAULT)
            sig.verify(signatureBytes)
        } catch (e: Throwable) {
            Log.e(TAG_HOTUPDATE, "Ed25519 verify failed", e)
            false
        }
    }

}

data class UpdateConfig(
    val version: Int,
    val url: String,
    val sha256: String,
    val signature: String,
    val alg: String
)