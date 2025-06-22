package com.ahu.ahutong.data

import arch.sink.utils.Utils
import com.ahu.ahutong.data.api.AHUService
import com.ahu.ahutong.data.base.BaseDataSource
import com.ahu.ahutong.data.crawler.CrawlerDataSource
import com.ahu.ahutong.data.crawler.api.adwmh.AdwmhApi
import com.ahu.ahutong.data.crawler.api.jwxt.JwxtApi
import com.ahu.ahutong.data.crawler.configs.Constants
import com.ahu.ahutong.data.crawler.model.adwnh.Info
import com.ahu.ahutong.data.crawler.model.jwxt.ExamInfo
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.Exam
import com.ahu.ahutong.data.model.User
import com.ahu.ahutong.data.reptile.utils.DES
import com.ahu.ahutong.ext.isTerm
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup
import java.util.regex.Matcher
import kotlin.coroutines.CoroutineContext.Element


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
    suspend fun getSchedule(isRefresh: Boolean = false) = withContext(Dispatchers.IO) {

        if (!isRefresh) {
            //本地优先
        }

        try {
            val response = dataSource.getSchedule()

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

                val regex = Regex("""studentExamInfoVms\s*=\s*(\[.*?]);""", RegexOption.DOT_MATCHES_ALL)
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
            val result = AHUResponse<User>()
            try {
                var failedTimes = 0
                var info: Info? = null

                while (failedTimes < 5) {
                    try {
                        val captchaBytes = AdwmhApi.API.getAuthCode().bytes()
                        val captchaPart = MultipartBody.Part.createFormData(
                            "captcha", "img.jpg",
                            captchaBytes.toRequestBody("image/jpg".toMediaType())
                        )

                        val captcha = AdwmhApi.API
                            .getCaptchaResult("http://120.26.208.230:8000/captcha", captchaPart)
                            .result

                        info = AdwmhApi.API.loginWithCaptcha(
                            username,
                            password,
                            0,
                            captcha
                        )

                        if (info.code == 10000) {
                            break
                        }
                    } catch (e: Exception) {
                        result.code = -1
                        result.msg = e.toString()
                        return@withContext result
                    }

                    failedTimes++
                }

                info?.let{
                    val loginPage = JwxtApi.API.fetchLoginInfo()

                    val document = Jsoup.parse(loginPage.body()!!.string())
                    val lt = document.selectFirst("input[name=lt]")?.attr("value")

                    if (lt == null) {
                        if (loginPage.raw().request.url.toString().endsWith(Constants.JWXT_HOME)) {
                            result.code = 0
                            result.data = User(info.`object`.user.userName, info.`object`.user.idNumber)
                            return@withContext result
                        } else {
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
                        DES().strEnc(username + password + lt, "1", "2", "3"),
                        username.length,
                        password.length,
                        lt.toString()
                    )
                    if (jwxtResponse.raw().request.url.toString().endsWith(Constants.JWXT_HOME)) {
                        result.code = 0
                        result.data = User(info.`object`.user.userName, info.`object`.user.idNumber)
                        return@withContext result
                    }
                }

                result.msg = "登录失败: 未知原因"
                result.code = -1
                result
            } catch (e: Throwable) {
                result.msg = "登录失败：${e.toString()}"
                result.code = -1
                result
            }
        }
}
