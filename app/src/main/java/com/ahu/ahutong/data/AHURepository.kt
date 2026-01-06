package com.ahu.ahutong.data

import android.util.Log
import com.ahu.ahutong.data.base.BaseDataSource
import com.ahu.ahutong.data.crawler.CrawlerDataSource
import com.ahu.ahutong.data.crawler.api.adwmh.AdwmhApi
import com.ahu.ahutong.data.crawler.api.jwxt.JwxtApi
import com.ahu.ahutong.data.crawler.configs.Constants
import com.ahu.ahutong.data.crawler.manager.CookieManager
import com.ahu.ahutong.data.crawler.manager.TokenManager
import com.ahu.ahutong.data.crawler.model.adwnh.Info
import com.ahu.ahutong.data.crawler.model.jwxt.ExamInfo
import com.ahu.ahutong.data.crawler.model.ycard.CardInfo
import com.ahu.ahutong.data.crawler.model.ycard.Request
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.BathroomTelInfo
import com.ahu.ahutong.data.model.Exam
import com.ahu.ahutong.data.model.User
import com.ahu.ahutong.sdk.RustSDK
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Response


/**
 * @Author: SinkDev
 * @Date: 2021/7/31-下午9:12
 * @Email: 468766131@qq.com
 */
object AHURepository {

    val TAG = this::class.java.simpleName

    var dataSource: BaseDataSource = CrawlerDataSource()

    /**
     * 通过semesterId获取课程表
     */
    suspend fun getSchedule(isRefresh: Boolean = false) = withContext(Dispatchers.IO) {

        if (!isRefresh) {
            AHUCache.getSchoolTerm()?.let{
                AHUCache.getSchedule(it)?.let{
                    Log.e(TAG, "getSchedule: 本地获取", )
                    return@withContext Result.success(it)
                }
            }
        }

        try {
            val response = dataSource.getSchedule()

            AHUCache.getSchoolTerm()?.let{
                AHUCache.saveSchedule(it,response.data)
            }

            if (response.isSuccessful) {
                Result.success(response.data)

            } else {
                Result.failure(Throwable(response.msg))
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }


    }


    /**
     * 查询成绩 本地优先
     * @param isRefresh Boolean 是否直接获取服务器上的
     * @return Result<List<News>>
     */
    suspend fun getGrade(isRefresh: Boolean = false) = withContext(Dispatchers.IO) {
        if (!isRefresh) {
            val localData = AHUCache.getGrade()
            if (localData != null) {
                return@withContext Result.success(localData)
            }
        }
        try {
            val response = dataSource.getGrade()
            if (response.isSuccessful) {
                // 保存数据
                AHUCache.saveGrade(response.data)
                Result.success(response.data)
            } else {
                Result.failure(Throwable(response.msg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     *  获取考试信息
     * @param isRefresh Boolean 是否获取缓存
     * @param studentID String
     * @param studentName String
     * @return Result<(kotlin.collections.List<com.ahu.ahutong.data.model.Exam>..kotlin.collections.List<com.ahu.ahutong.data.model.Exam>?)>
     */
    suspend fun getExamInfo(isRefresh: Boolean = false, studentID: String, studentName: String) =
        withContext(Dispatchers.IO) {
            if (!isRefresh) {
                val localData = AHUCache.getExamInfo().orEmpty()
                if (localData.isNotEmpty()) {
                    return@withContext Result.success(localData)
                }
            }
            try {
                val result = RustSDK.getExamInfoSafe()
                if (result.isSuccess) {
                    val exams = result.getOrDefault(emptyList())
                    AHUCache.saveExamInfo(exams)
                    Result.success(exams)
                } else {
                    Result.failure(result.exceptionOrNull() ?: Throwable("RustSDK Error"))
                }
            } catch (e: Exception) {
                Result.failure(Throwable("请求错误 $e"))
            }
        }

    /**
     *  获取余额
     * @return Result<(kotlin.String..kotlin.String?)>
     */
    suspend fun getCardMoney() = withContext(Dispatchers.IO) {
        try {
            val response = dataSource.getCardMoney()
            if (response.isSuccessful) {
                Result.success(response.data)
            } else {
                Result.failure(Throwable(response.msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBathRooms() = withContext(Dispatchers.IO) {
        try {
            val response = dataSource.getBathRooms()
            if (response.isSuccessful) {
                Result.success(response.data)
            } else {
                Result.failure(Throwable(response.msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    /**
     * 爬虫登录
     */

    suspend fun loginWithCrawler(username: String, password: String): AHUResponse<User> =
        withContext(Dispatchers.IO) {
            CookieManager.cookieJar.clear()
            TokenManager.clear()
            val result = RustSDK.loginSafe(username, password)

            val response = AHUResponse<User>()
            if (result.isSuccess) {
                response.code = 0
                response.data = result.getOrNull()
                response.msg = "登录成功"

                syncCookies()
            } else {
                response.code = -1
                response.msg = result.exceptionOrNull()?.message ?: "登录失败"
            }
            return@withContext response
        }

    private fun syncCookies() {
        try {
            val json = RustSDK.getCookiesListSafe()
            // 解析 JSON 数组
            val listType = object : com.google.gson.reflect.TypeToken<List<Map<String, Any>>>() {}.type
            val cookies: List<Map<String, Any>> = Gson().fromJson(json, listType)

            cookies.forEach {
                val builder = okhttp3.Cookie.Builder()
                    .name(it["name"] as String)
                    .value(it["value"] as String)
                val domainObj = it["domain"]
                val path = it["path"] as String

                val domain = if (domainObj != null) {
                    domainObj as String
                } else {
                    // 如果 domain 为空，根据 path 进行简单的推断 (host-only cookie 处理)
                    if (path.contains("/cas")) "one.ahu.edu.cn" else "jw.ahu.edu.cn"
                }

                builder.domain(domain)
                    .path(path)

                if (it["secure"] == true) builder.secure()
                if (it["http_only"] == true) builder.httpOnly()

                val cookie = builder.build()
                com.ahu.ahutong.data.crawler.manager.CookieManager.cookieJar.addCookie(cookie)
            }
            Log.d(TAG, "Cookies synced from Rust SDK: " + cookies.size)
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }


    suspend fun getBathroomInfo(bathroom: String, tel: String): AHUResponse<BathroomTelInfo> =
        withContext(
            Dispatchers.IO
        ) {
            dataSource.getBathroomTelInfo(bathroom = bathroom, tel = tel)
        }


    suspend fun getCardInfo(): AHUResponse<CardInfo> =
        withContext(
            Dispatchers.IO
        ) {
            dataSource.getCardInfo()
        }


    suspend fun getOrderThirdData(request: Request): AHUResponse<Response<ResponseBody>> =
        withContext(
            Dispatchers.IO
        ){
            dataSource.getOrderThirdData(request)
        }

    suspend fun pay(request:Request):AHUResponse<Response<ResponseBody>> =
        withContext(
            Dispatchers.IO
        ){
            dataSource.pay(request)
        }

}
