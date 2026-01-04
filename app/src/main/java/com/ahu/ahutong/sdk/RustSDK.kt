package com.ahu.ahutong.sdk

import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.annotation.Keep
import com.ahu.ahutong.data.model.Exam

/**
 * Rust SDK 的 Kotlin 封装层
 * 负责加载 native 库并提供类型安全的接口
 */
@Keep
object RustSDK {

    init {
        // 加载名为 "ahutong_rs" 的动态库 (libahutong_rs.so)
        // 这里的库名需要与 Rust Cargo.toml 中定义的 [lib] name 保持一致
        System.loadLibrary("ahutong_rs")
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


    // --- 高级封装接口 (解析 JSON 为对象) ---

    fun loginSafe(username: String, password: String): Result<User> {
        val json = login(username, password)
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
        val json = getSchedule()
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
        val json = getExamInfo()
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
}