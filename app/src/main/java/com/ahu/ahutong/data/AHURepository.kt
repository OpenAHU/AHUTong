package com.ahu.ahutong.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.ahu.ahutong.data.api.APIDataSource
import com.ahu.ahutong.data.base.BaseDataSource
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.*
import com.ahu.ahutong.data.reptile.ReptileDataSource
import com.ahu.ahutong.data.reptile.ReptileUser
import com.ahu.ahutong.ext.isCampus
import com.ahu.ahutong.ext.isEmptyRoomTime
import com.ahu.ahutong.ext.isTerm
import com.ahu.ahutong.ext.isWeekday
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.sink.library.log.SinkLog
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
    var dataSource: BaseDataSource

    init {
        dataSource = if (AHUCache.isLogin() && AHUCache.getLoginType() == User.UserType.AHU_LOCAL) {
            val user = AHUCache.getCurrentUser()!! // 加!!因为可以保证，已经登录
            val password = AHUCache.getCurrentUserPassword()!!
            ReptileDataSource(ReptileUser(user.name, password))
        } else {
            APIDataSource()
        }
    }


    /**
     * 搜索垃圾
     * @param keyword String
     * @return LiveData<Result<List<Rubbish>>>
     */
    fun searchRubbish(keyword: String): LiveData<Result<List<Rubbish>>> = liveData(Dispatchers.IO) {
        val result = try {
            val client = OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()
            val request = Request.Builder()
                .url("https://api.tianapi.com/txapi/lajifenlei/?key=367f6d1bd8e7cacbb14485af77f1ed6b&word=$keyword")
                .get()
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body
                val jsonElement =
                    JsonParser.parseString(body?.string() ?: "").asJsonObject["newslist"]
                if (jsonElement == null) {
                    Result.failure<List<Rubbish>>(Throwable("返回结果为空"))
                } else {
                    Result.success(
                        Gson().fromJson(
                            jsonElement,
                            object : TypeToken<List<Rubbish>>() {}.type
                        )
                    )
                }
            } else {
                Result.failure(Throwable("response status is ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
        //发射结果
        emit(result)
    }


    /**
     * 获取课程表 本地优先
     * @param schoolYear String
     * @param schoolTerm String
     * @param isRefresh Boolean 是否直接获取服务器上的
     * @return Result<List<Course>>
     */
    suspend fun getSchedule(schoolYear: String, schoolTerm: String, isRefresh: Boolean = false) =
        withContext(Dispatchers.IO) {
            SinkLog.i("check argument start")
            if (!schoolTerm.isTerm()) {
                throw IllegalArgumentException("schoolTerm must be 1 or 2")
            }
            SinkLog.i("start get Schedule")

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
                    Result.failure<List<Course>>(Throwable(response.msg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * 查询空教室
     * @param campus String
     * @param weekday String
     * @param weekNum String
     * @param time String
     * @return LiveData<Result<List<Room>>>
     */
    suspend fun getEmptyRoom(
        campus: String,
        weekday: String,
        weekNum: String,
        time: String
    ): Result<List<Room>> {
        checkRoomArgs(campus, weekday, time)
        return withContext(Dispatchers.IO) {
            try {
                SinkLog.i("start get emptyRoom")
                val response = dataSource.getEmptyRoom(campus, weekday, weekNum, time)
                if (response.isSuccessful) {
                    SinkLog.i("get emptyRoom success")
                    Result.success(response.data)
                } else {
                    SinkLog.e(response)
                    Result.failure(Throwable(response.msg))
                }
            } catch (e: Exception) {
                SinkLog.e(e)
                Result.failure(e)
            }
        }
    }


    /**
     * 获取新闻 本地优先
     * @param isRefresh Boolean 是否直接获取服务器上的
     * @return Result<List<News>>
     */
    suspend fun getNews(isRefresh: Boolean = false): Result<List<News>> =
        withContext(Dispatchers.IO) {
            //本地优先
            if (!isRefresh) {
                val localData = AHUCache.getNews().orEmpty()
                if (localData.isNotEmpty()) {
                    return@withContext Result.success(localData)
                }
            }
            //获取网络上的数据
            try {
                val response = dataSource.getNews()
                if (response.isSuccessful) {
                    AHUCache.saveNews(response.data)
                    Result.success(response.data)
                } else {
                    SinkLog.e(response)
                    Result.failure(Throwable(response.msg))
                }
            } catch (e: Exception) {
                SinkLog.e(e)
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
            SinkLog.i("Get grade start")
            val response = dataSource.getGrade()
            if (response.isSuccessful) {
                SinkLog.i("Get grade success")
                //保存数据
                AHUCache.saveGrade(response.data)
                Result.success(response.data)
            } else {
                SinkLog.e(response)
                Result.failure(Throwable(response.msg))
            }
        } catch (e: Exception) {
            SinkLog.e(e)
            Result.failure(e)
        }
    }


    /**
     *  获取考试信息
     * @param schoolYear String
     * @param schoolTerm String
     * @param isRefresh Boolean
     * @return Result<(kotlin.collections.List<com.ahu.ahutong.data.model.Exam>..kotlin.collections.List<com.ahu.ahutong.data.model.Exam>?)>
     */
    suspend fun getExamInfo(schoolYear: String, schoolTerm: String, isRefresh: Boolean = false) =
        withContext(Dispatchers.IO) {
            SinkLog.i("check argument start")
            if (!schoolTerm.isTerm()) {
                throw IllegalArgumentException("schoolTerm must be 1 or 2")
            }
            if (!isRefresh) {
                val localData = AHUCache.getExamInfo().orEmpty()
                if (localData.isNotEmpty()) {
                    return@withContext Result.success(localData)
                }
            }
            //从网络上获取数据
            try {
                SinkLog.i("Get exam info start")
                val response = dataSource.getExamInfo(schoolYear, schoolTerm)
                if (response.isSuccessful) {
                    SinkLog.i("Get exam info success")
                    //保存数据
                    AHUCache.saveExamInfo(response.data)
                    Result.success(response.data)
                } else {
                    SinkLog.e("Get exam info fail, $response")
                    Result.failure(Throwable(response.msg))
                }
            } catch (e: Exception) {
                SinkLog.e("Get exam info fail, $e")
                Result.failure(e)
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
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    /**
     * 检查Room参数
     * @param campus String
     * @param weekday String
     * @param time String
     */
    private fun checkRoomArgs(campus: String, weekday: String, time: String) {

        SinkLog.i("check argument start $time")
        if (!campus.isCampus()) {
            throw IllegalArgumentException("campus must be 1 or 2")
        }
        if (!weekday.isWeekday()) {
            throw IllegalArgumentException("weekday must be 1-7")
        }
        if (!time.isEmptyRoomTime()) {
            throw IllegalArgumentException("time must be 1-10")
        }
    }


}