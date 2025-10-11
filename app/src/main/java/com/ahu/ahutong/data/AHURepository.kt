package com.ahu.ahutong.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import arch.sink.utils.Utils
import com.ahu.ahutong.data.api.AHUService
import com.ahu.ahutong.data.base.BaseDataSource
import com.ahu.ahutong.data.crawler.CrawlerDataSource
import com.ahu.ahutong.data.crawler.api.adwmh.AdwmhApi
import com.ahu.ahutong.data.crawler.api.jwxt.JwxtApi
import com.ahu.ahutong.data.crawler.api.ycard.YcardApi
import com.ahu.ahutong.data.crawler.configs.Constants
import com.ahu.ahutong.data.crawler.model.adwnh.Info
import com.ahu.ahutong.data.crawler.model.jwxt.ExamInfo
import com.ahu.ahutong.data.crawler.model.ycard.BathroomInfo
import com.ahu.ahutong.data.crawler.model.ycard.CardInfo
import com.ahu.ahutong.data.crawler.model.ycard.Request
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.BathroomTelInfo
import com.ahu.ahutong.data.model.Exam
import com.ahu.ahutong.data.model.User
import com.ahu.ahutong.data.reptile.utils.DES
import com.ahu.ahutong.ext.isTerm
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Response
import java.util.regex.Matcher
import kotlin.coroutines.CoroutineContext.Element


/**
 * @Author: SinkDev
 * @Date: 2021/7/31-下午9:12
 * @Email: 468766131@qq.com
 */
object AHURepository {

    val TAG = this::class.java.simpleName

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
                val response = JwxtApi.API.getExamInfo()
                val doc = Jsoup.parse(response.body()!!.string())
//                val infos: Elements? = doc.select("tr.unfinished, tr.finished");
                val exams = mutableListOf<Exam>()
//                if (infos != null) {
//                    for (row in infos) {
//                        val time: String? = row.selectFirst("div.time")!!.text()
//                        val spans: Elements = row.select("td").get(0).select("span")
//                        val campus = spans.get(0).text()
//                        val building = spans.get(1).text()
//                        val room = spans.get(2).text()
//                        val seat = spans.get(3).text()
//
//                        val course: String? = row.select("td").get(1).selectFirst("span")!!.text()
//
//                        val examType: String? =
//                            row.select("td").get(1).selectFirst("span.tag-span")!!.text()
//
//
//                        val exam = Exam()
//                        exam.course = course
//                        exam.location = "$campus-$room"
//                        exam.seatNum = seat
//                        exam.time = time
//
//                        exams.add(exam)
//                    }
//                }

                val scripts = doc.select("script")

                val regex =
                    Regex("""studentExamInfoVms\s*=\s*(\[.*?]);""", RegexOption.DOT_MATCHES_ALL)
                val gson = Gson()

                var foundList: ExamInfo? = null

                for (script in scripts) {
                    val data = script.data()
                    val match = regex.find(data)
                    if (match != null) {
                        val rawJson = match.groups[1]?.value ?: continue
                        val fixedJson = rawJson.replace("'", "\"")
                        try {
                            foundList = gson.fromJson(fixedJson, ExamInfo::class.java)
                            break
                        } catch (e: Exception) {
                            println("解析JSON失败: ${e.message}")
                        }
                    }
                }

                if (foundList != null) {
                    foundList.forEach {
                        val exam = Exam()
                        exam.course = "${it.course.nameZh}(${it.examType.nameZh})"
                        exam.time = it.examTime
                        exam.seatNum = it.seatNo.toString()
                        exam.location = "${it.requiredCampus.nameZh}-${it.room}"
                        exam.finished = it.finished
                        exams.add(exam)
                    }
                } else {
                    println("未找到 studentExamInfoVms 数据")
                }

                AHUCache.saveExamInfo(exams.toList())
                Result.success(exams.toList())

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

    suspend fun loginWithCrawler(username: String, password: String): AHUResponse<User> =
        withContext(Dispatchers.IO) {
            val adwmhLogin = async(Dispatchers.IO) {

                var failedTimes = 0
                var info: Info? = null
                // 二维码可能识别失败，尝试5次呢
                while (failedTimes < 5) {
                    val captchaBytes = AdwmhApi.API.getAuthCode().bytes()
                    val captchaPart = MultipartBody.Part.createFormData(
                        "captcha", "img.jpg",
                        captchaBytes.toRequestBody("image/jpg".toMediaType())
                    )

                    val captcha = AdwmhApi.API
                        .getCaptchaResult("http://47.236.115.210:8000/captcha", captchaPart)
                        .result

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
