package com.ahu.ahutong.data

import arch.sink.utils.Utils
import com.ahu.ahutong.data.api.AHUService
import com.ahu.ahutong.data.api.APIDataSource
import com.ahu.ahutong.data.base.BaseDataSource
import com.ahu.ahutong.data.crawler.CrawlerDataSource
import com.ahu.ahutong.data.crawler.api.adwmh.AdwmhApi
import com.ahu.ahutong.data.crawler.api.jwxt.JwxtApi
import com.ahu.ahutong.data.crawler.configs.Constants
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.Exam
import com.ahu.ahutong.data.model.User
import com.ahu.ahutong.data.reptile.utils.DES
import com.ahu.ahutong.data.reptile.utils.JsoupProxy
import com.ahu.ahutong.ext.await
import com.ahu.ahutong.ext.awaitString
import com.ahu.ahutong.ext.isTerm
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

/**
 * @Author: SinkDev
 * @Date: 2021/7/31-下午9:12
 * @Email: 468766131@qq.com
 */
object AHURepository {
    var dataSource: BaseDataSource = CrawlerDataSource()

    /**
     * 获取课程表 本地优先
     * @param schoolYear String
     * @param schoolTerm String
     * @param isRefresh Boolean 是否直接获取服务器上的
     * @return Result<List<Course>>
     */
    suspend fun getSchedule(schoolYear: String, schoolTerm: String, isRefresh: Boolean = false) =
        withContext(Dispatchers.IO) {
            if (!schoolTerm.isTerm()) {
                throw IllegalArgumentException("schoolTerm must be 1 or 2")
            }
            // 本地优先
            if (!isRefresh) {
                val localData = AHUCache.getSchedule(schoolYear, schoolTerm).orEmpty()
                if (localData.isNotEmpty()) {
                    return@withContext Result.success(localData)
                }
            }
            // 从网络上获取
            try {
                val response = dataSource.getSchedule(schoolYear, schoolTerm)
                if (response.isSuccessful) {
                    // 缓存
                    AHUCache.saveSchedule(schoolYear, schoolTerm, response.data)
                    Result.success(response.data)
                } else {
                    Result.failure(Throwable(response.msg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * 通过semesterId获取课程表
     */
    suspend fun getSchedule(isRefresh: Boolean = false)=withContext(Dispatchers.IO){

        if(!isRefresh){
            //本地优先
        }

        try{
            val response = dataSource.getSchedule()

            if(response.isSuccessful){
                Result.success(response.data)
            }else{
                Result.failure(Throwable(response.msg))
            }
        }catch (e: Throwable){
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
                val client = OkHttpClient.Builder()
                    .readTimeout(5, TimeUnit.SECONDS)
                    .writeTimeout(5, TimeUnit.SECONDS)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build()
                val request = Request.Builder()
                    .url("http://kskw.ahu.edu.cn/bkcx.asp?xh=$studentID")
                    .build()
                val response = client.newCall(request).await()
                if (response.isSuccessful) {
                    val body = response.body?.awaitString()
                    if (body.toString().contains("暂无您的查询信息")) {
                        Result.success(arrayListOf())
                    } else {
                        val bodyLines =
                            body!!.replace("<br/?>".toRegex(), "<br>${System.lineSeparator()}")
                                .split(System.lineSeparator()).stream()
                                .filter { it.contains("<br>") }.iterator()
                        val exams = mutableListOf<Exam>()
                        while (bodyLines.hasNext()) {
                            var informationLine = bodyLines.next()
                            if (informationLine.contains("年")) {
                                val current = Exam()
                                val split1 = informationLine.split("/").toTypedArray()
                                current.time = split1[0].substring(1 + split1[0].indexOf(":"))
                                    .replace("<br/?>".toRegex(), "")
                                current.course = split1[1].replace("<br/?>".toRegex(), "")
                                informationLine = bodyLines.next()
                                if (informationLine.contains("座")) {
                                    val split2 = informationLine.split("/").toTypedArray()
                                    current.seatNum =
                                        split2[1].substring(1 + split2[1].indexOf("："))
                                            .replace("<br/?>".toRegex(), "")
                                    current.location = split2[2].replace(studentName, "")
                                        .replace("<br/?>".toRegex(), "")
                                    exams.add(current)
                                }
                            }
                        }
                        AHUCache.saveExamInfo(exams)
                        Result.success(exams)
                    }
                } else {
                    Result.failure(Throwable("请求错误，代码 ${response.code}"))
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

    suspend fun getBanner() = withContext(Dispatchers.IO) {
        try {
            val response = AHUService.API.getBanner()
            if (response.isSuccessful) {
                val data = response.data.filter { it.isLegal }
                AHUCache.saveBanner(data)
                Result.success(data)
            } else {
                throw IllegalStateException(response.msg)
            }
        } catch (e: Exception) {
            val cache = AHUCache.getBanner()
            if (cache != null) {
                Result.success(cache)
            } else {
                Result.failure(Throwable(e.message))
            }
        }
    }

    /**
     * 爬虫登录
     */

    suspend fun loginWithCrawler(username: String, password: String): AHUResponse<User> = withContext(Dispatchers.IO) {
        val result = AHUResponse<User>()
        try {

            val captchaBytes = AdwmhApi.API.getAuthCode().bytes()
            val captchaPart = MultipartBody.Part.createFormData(
                "captcha", "img.jpg",
                captchaBytes.toRequestBody("image/jpg".toMediaType())
            )
            val captcha = AdwmhApi.API
                .getCaptchaResult("http://120.26.208.230:8000/captcha", captchaPart)
                .result

            val info = try {
                AdwmhApi.API.loginWithCaptcha(
                    username,
                    password,
                    0,
                    captcha
                )
            } catch (e: Exception) {
                result.code = -1
                result.msg = e.toString()
                return@withContext result
            }
            if (info.code != 10000) {
                result.code = -1
                result.msg = "登录失败：(${info.msg})"
                return@withContext result
            }

            val loginPage = JwxtApi.API.fetchLoginInfo()

            val document = Jsoup.parse(loginPage.body()!!.string())
            val lt = document.selectFirst("input[name=lt]")?.attr("value")

            if (lt == null) {
                if (loginPage.raw().request.url.toString().endsWith(Constants.JWXT_HOME)){
                    result.code = 0
                    result.data = User(info.`object`.user.userName,info.`object`.user.idNumber)
                    return@withContext result
                }else{
                    result.code = -1
                    result.msg = "登陆失败：获取登录页数据错误"
                    return@withContext result
                }
            }


            val jsession = SharedPrefsCookiePersistor(Utils.getApp()).loadAll()
                .firstOrNull {
                    it.name == "JSESSIONID"
                }?.value ?: return@withContext result

            val jwxtLoginUrl = "https://one.ahu.edu.cn/cas/login;jsessionid=$jsession" +
                    "?service=https%3A%2F%2Fjw.ahu.edu.cn%2Fstudent%2Fsso%2Flogin"

            val jwxtResponse = JwxtApi.API.login(
                jwxtLoginUrl,
                DES().strEnc(username+password+lt,"1","2","3"),
                username.length,
                password.length,
                lt.toString()
            )
            if(jwxtResponse.raw().request.url.toString().endsWith(Constants.JWXT_HOME)){
                result.code = 0
                result.data = User(info.`object`.user.userName,info.`object`.user.idNumber)
                return@withContext result
            }


            result.msg = "登录失败: 未知原因"
            result.code = -1
            result
        }catch (e : Throwable){
            result.msg = "登录失败：${e.toString()}"
            result.code = -1
            result
        }
    }
}
