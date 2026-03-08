package com.ahu.ahutong.data

import android.util.Log
import com.ahu.ahutong.data.base.BaseDataSource
import com.ahu.ahutong.data.crawler.CrawlerDataSource
import com.ahu.ahutong.data.crawler.api.adwmh.AdwmhApi
import com.ahu.ahutong.data.crawler.api.jwxt.JwxtApi
import com.ahu.ahutong.data.crawler.configs.Constants
import com.ahu.ahutong.data.crawler.model.adwnh.Info
import com.ahu.ahutong.data.crawler.model.ycard.CardInfo
import com.ahu.ahutong.data.crawler.model.ycard.RequestBody
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.BathroomTelInfo
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.User
import com.ahu.ahutong.data.mock.MockDataSource
import com.ahu.ahutong.data.server.AhuTong
import com.ahu.ahutong.sdk.LocalServiceClient
import com.ahu.ahutong.sdk.RustSDK
import com.ahu.ahutong.utils.DES
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
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

    private var dataSource: BaseDataSource = CrawlerDataSource()
    fun initializeDataSource(useMock: Boolean = AHUCache.getMockData()) {
        dataSource = if (useMock) MockDataSource() else CrawlerDataSource()
    }
    
    /**
     * 获取 HTTP 客户端
     */
    private fun getHttpClient(): LocalServiceClient? = LocalServiceClient.getInstance()

    /**
     * 通过semesterId获取课程表
     * @param isRefresh 是否强制刷新
     * @param isRetry 是否为重试（静默重登录后），防止无限循环
     */
    suspend fun getSchedule(isRefresh: Boolean = false): Result<List<Course>> = withContext(Dispatchers.IO) {

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
                val response = dataSource.getExamInfo(studentID, studentName)
                if (response.isSuccessful) {
                    val exams = response.data ?: emptyList()
                    AHUCache.saveExamInfo(exams)
                    Result.success(exams)
                } else {
                    Result.failure(Throwable(response.msg ?: "获取考试信息失败"))
                }
            } catch (e: Exception) {
                Result.failure(Throwable("请求错误 $e"))
            }
        }

    /**
     *  获取余额
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
            val adwmhLogin = async(Dispatchers.IO) {

                var failedTimes = 0
                var info: Info? = null
                // 二维码可能识别失败，尝试5次呢
                while (failedTimes < 5) {
                    Log.e(TAG, "loginWithCrawler: ${failedTimes+1} 登录", )
                    val captchaBytes = AdwmhApi.API.getAuthCode().bytes()
                    Log.e(TAG, "loginWithCrawler: ${captchaBytes}", )
                    val captchaPart = MultipartBody.Part.createFormData(
                        "captcha", "img.jpg",
                        captchaBytes.toRequestBody("image/jpg".toMediaType())
                    )
                    val captcha = AhuTong.API
                        .getCaptchaResult(captchaPart)
                        .result


                    Log.e(TAG, "loginWithCrawler: ${captcha}", )
                    info = AdwmhApi.API.loginWithCaptcha(
                        username,
                        password,
                        0,
                        captcha
                    )

                    if (info.code == 10000) {
                        Log.e(TAG, "loginWithCrawler: $info")
                        return@async info
                    }
                    failedTimes++
                }

                return@async info

            }

            val jwxtLogin = async {
                val loginPage = JwxtApi.API.fetchLoginInfo()

                val document = Jsoup.parse(loginPage.body()!!.string())
                val lt = document.selectFirst("input[name=lt]")?.attr("value")

                lt?.let {
                    val cipher = DES().strEnc(username + password + lt, "1", "2", "3")

                    val res = JwxtApi.API.device(
                        "https://one.ahu.edu.cn/cas/device",
                        username.length,
                        password.length,
                        cipher
                    )
                    Log.e(TAG, "loginWithCrawler: $res")

                    val jwxtLoginUrl = "https://one.ahu.edu.cn/cas/login" +
                            "?service=https%3A%2F%2Fjw.ahu.edu.cn%2Fstudent%2Fsso%2Flogin"

                    val jwxtResponse = JwxtApi.API.login(
                        jwxtLoginUrl,
                        cipher,
                        username.length,
                        password.length,
                        lt
                    )

                    if (jwxtResponse.raw().request.url.toString().endsWith(Constants.JWXT_HOME)) {
                        return@async true
                    }

                } ?: run {
                    if (loginPage.raw().request.url.toString().endsWith(Constants.JWXT_HOME)) {
                        return@async true
                    } else {
                        return@async false
                    }
                }

                return@async false
            }

            val crawlerResult = adwmhLogin.await()
            val jwxtLoginSuccess: Boolean = jwxtLogin.await()

            val result = AHUResponse<User>()


            crawlerResult?.let {
                if (it.code == 10000 && jwxtLoginSuccess) {
                    result.code = 0
                    result.data = User(it.`object`.user.userName, it.`object`.user.idNumber)
                    result.msg = "登录成功"
                    return@withContext result
                }
            }
            result.code = -1;
            result.msg = "登录失败"
            return@withContext result
        }

    private fun syncCookies() {
        try {
            // 优先使用 HTTP 客户端
            val httpClient = getHttpClient()
            val json = if (httpClient != null) {
                Log.d("LocalServiceClient", "[syncCookies] Using HTTP client")
                // 使用协程同步获取
                kotlinx.coroutines.runBlocking {
                    httpClient.getCookiesList().getOrDefault("[]")
                }
            } else {
                Log.d("LocalServiceClient", "[syncCookies] Fallback to JNI")
                RustSDK.getCookiesListSafe()
            }
            
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
        withContext(Dispatchers.IO) {
            dataSource.getBathroomTelInfo(bathroom = bathroom, tel = tel)
        }


    suspend fun getCardInfo(): AHUResponse<CardInfo> =
        withContext(Dispatchers.IO) {
            dataSource.getCardInfo()
        }


    suspend fun getOrderThirdData(request: RequestBody): AHUResponse<Response<ResponseBody>> =
        withContext(Dispatchers.IO){
            dataSource.getOrderThirdData(request)
        }

    suspend fun pay(request: RequestBody):AHUResponse<Response<ResponseBody>> =
        withContext(Dispatchers.IO){
            dataSource.pay(request)
        }


    suspend fun getSchoolCalendar(): AHUResponse<Response<ResponseBody>> =
        withContext(Dispatchers.IO) {
            dataSource.getSchoolCalendar()
        }

}
