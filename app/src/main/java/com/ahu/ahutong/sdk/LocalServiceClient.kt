package com.ahu.ahutong.sdk

import android.util.Log
import com.ahu.ahutong.data.crawler.model.jwxt.GradeResponse
import com.ahu.ahutong.data.model.Card
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.Exam
import com.ahu.ahutong.data.model.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * @Author Yukon
 * @Email 605606366@qq.com
 */

/**
 * 本地 HTTP 服务客户端
 * 通过 HTTP 调用 Rust 本地服务，替代直接 JNI 调用
 */
class LocalServiceClient(
    private val baseUrl: String,
    private val token: String
) {
    companion object {
        private const val TAG = "LocalServiceClient"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        
        @Volatile
        private var instance: LocalServiceClient? = null
        
        /**
         * 获取单例实例
         */
        fun getInstance(): LocalServiceClient? = instance
        
        /**
         * 初始化客户端（在服务启动后调用）
         */
        fun initialize(port: Int, token: String) {
            instance = LocalServiceClient("http://127.0.0.1:$port", token)
            Log.i(TAG, "LocalServiceClient initialized: port=$port")
        }
        
        /**
         * 销毁实例
         */
        fun destroy() {
            instance = null
            Log.i(TAG, "LocalServiceClient destroyed")
        }
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    
    private fun extractErrorMessage(json: String): String? {
        return try {
            val map = gson.fromJson(json, Map::class.java) as? Map<*, *>
            map?.get("error") as? String
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * 健康检查
     */
    suspend fun health(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/health")
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e(TAG, "Health check failed", e)
            false
        }
    }
    
    /**
     * 初始化 Cookies
     */
    suspend fun init(cookiesJson: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val body = gson.toJson(mapOf("cookies_json" to cookiesJson))
                .toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url("$baseUrl/init")
                .header("X-AHUTONG-TOKEN", token)
                .post(body)
                .build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Init failed: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Init failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * 导出 Cookies
     */
    suspend fun dumpCookies(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/cookies/dump")
                .header("X-AHUTONG-TOKEN", token)
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = response.body?.string() ?: "{}"
                    val map = gson.fromJson(json, Map::class.java) as? Map<String, String>
                    Result.success(map?.get("cookies") ?: "")
                } else {
                    Result.failure(Exception("Dump cookies failed: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Dump cookies failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * 登录
     */
    suspend fun login(username: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val body = gson.toJson(mapOf("username" to username, "password" to password))
                .toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url("$baseUrl/login")
                .header("X-AHUTONG-TOKEN", token)
                .post(body)
                .build()
            client.newCall(request).execute().use { response ->
                val json = response.body?.string() ?: "{}"
                if (json.contains("\"error\"")) {
                    Result.failure(Exception(extractErrorMessage(json) ?: json))
                } else {
                    val user = gson.fromJson(json, User::class.java)
                    Result.success(user)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * 获取课表
     */
    suspend fun getSchedule(): Result<List<Course>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/schedule")
                .header("X-AHUTONG-TOKEN", token)
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                val json = response.body?.string() ?: "[]"
                if (json.contains("\"error\"")) {
                    Result.failure(Exception(extractErrorMessage(json) ?: json))
                } else {
                    val type = object : TypeToken<List<Course>>() {}.type
                    val courses: List<Course> = gson.fromJson(json, type)
                    Result.success(courses)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get schedule failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * 获取考试信息
     */
    suspend fun getExamInfo(): Result<List<Exam>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/exam")
                .header("X-AHUTONG-TOKEN", token)
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                val json = response.body?.string() ?: "[]"
                if (json.contains("\"error\"")) {
                    Result.failure(Exception(extractErrorMessage(json) ?: json))
                } else {
                    val type = object : TypeToken<List<Exam>>() {}.type
                    val exams: List<Exam> = gson.fromJson(json, type)
                    Result.success(exams)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get exam info failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * 获取成绩
     */
    suspend fun getGrade(studentId: String? = null): Result<GradeResponse> = withContext(Dispatchers.IO) {
        try {
            val url = if (studentId.isNullOrEmpty()) {
                "$baseUrl/grade"
            } else {
                "$baseUrl/grade?student_id=$studentId"
            }
            val request = Request.Builder()
                .url(url)
                .header("X-AHUTONG-TOKEN", token)
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                val json = response.body?.string() ?: "{}"
                if (json.contains("\"error\"")) {
                    Result.failure(Exception(extractErrorMessage(json) ?: json))
                } else {
                    val gradeResponse = gson.fromJson(json, GradeResponse::class.java)
                    Result.success(gradeResponse)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get grade failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * 获取校园卡余额
     */
    suspend fun getBalance(): Result<Card> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/ycard/balance")
                .header("X-AHUTONG-TOKEN", token)
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                val json = response.body?.string() ?: "{}"
                if (json.contains("\"error\"")) {
                    Result.failure(Exception(extractErrorMessage(json) ?: json))
                } else {
                    val card = gson.fromJson(json, Card::class.java)
                    Result.success(card)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get balance failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * 获取二维码
     */
    suspend fun getQrcode(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/ycard/qrcode")
                .header("X-AHUTONG-TOKEN", token)
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                val json = response.body?.string() ?: "{}"
                if (json.contains("\"error\"")) {
                    Result.failure(Exception(extractErrorMessage(json) ?: json))
                } else {
                    Result.success(json)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get QR code failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * 刷新 Token
     */
    suspend fun refreshToken(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/ycard/refresh_token")
                .header("X-AHUTONG-TOKEN", token)
                .post("".toRequestBody(null))
                .build()
            client.newCall(request).execute().use { response ->
                val json = response.body?.string() ?: "{}"
                if (json.contains("\"error\"")) {
                    Result.failure(Exception(extractErrorMessage(json) ?: json))
                } else {
                    val map = gson.fromJson(json, Map::class.java) as? Map<String, String>
                    Result.success(map?.get("access_token") ?: "")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Refresh token failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * 获取 Cookies 列表（扁平化）
     */
    suspend fun getCookiesList(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/cookies/flat")
                .header("X-AHUTONG-TOKEN", token)
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(response.body?.string() ?: "[]")
                } else {
                    Result.failure(Exception("Get cookies list failed: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get cookies list failed", e)
            Result.failure(e)
        }
    }
}
