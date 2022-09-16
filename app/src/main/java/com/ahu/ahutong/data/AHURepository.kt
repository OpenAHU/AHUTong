package com.ahu.ahutong.data

import com.ahu.ahutong.data.api.AHUService
import com.ahu.ahutong.data.api.APIDataSource
import com.ahu.ahutong.data.base.BaseDataSource
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.*
import com.ahu.ahutong.ext.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit


/**
 * @Author: SinkDev
 * @Date: 2021/7/31-下午9:12
 * @Email: 468766131@qq.com
 */
object AHURepository {
    var dataSource: BaseDataSource = APIDataSource()

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
                    //缓存
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
                //保存数据
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
                    if (body.toString().contains("暂无您的查询信息"))
                        Result.success(arrayListOf())
                    else {
                        val bodyLines = body!!.split(System.lineSeparator()).stream()
                            .filter { it.contains("<br>") }.iterator()
                        val exams = mutableListOf<Exam>()
                        while (bodyLines.hasNext()) {
                            var theme = bodyLines.next()
                            if (theme.contains("年")) {
                                val current = Exam()
                                val split1 = theme.split("/").toTypedArray()
                                current.time = split1[0].substring(1 + split1[0].indexOf(":"))
                                current.course = split1[1]
                                theme = bodyLines.next()
                                if (theme.contains("座")) {
                                    val split2 = theme.split("/").toTypedArray()
                                    current.seatNum =
                                        split2[1].substring(1 + split2[1].indexOf("："))
                                    current.location = split2[2].replace(studentName, "")
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
}