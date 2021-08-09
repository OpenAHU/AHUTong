package com.ahu.ahutong.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.ahu.ahutong.data.api.APIDataSource
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.*
import com.ahu.ahutong.ext.isCampus
import com.ahu.ahutong.ext.isEmptyRoomTime
import com.ahu.ahutong.ext.isWeekday
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.sink.library.log.SinkLog
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.util.concurrent.TimeUnit

/**
 * @Author: SinkDev
 * @Date: 2021/7/31-下午9:12
 * @Email: 468766131@qq.com
 */
object AHURepository {
    var dataSource = APIDataSource()

    /**
     * 搜索垃圾
     * @param keyword String
     * @return LiveData<Result<List<Rubbish>>>
     */
    fun searchRubbish(keyword: String) = liveData(Dispatchers.IO) {
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
     * @return LiveData<Result<List<Course>>>
     */
    fun getSchedule(
        schoolYear: String,
        schoolTerm: String,
        isRefresh: Boolean = false
    ): LiveData<Result<List<Course>>> {
        SinkLog.i("check argument start")
        if (!schoolTerm.isCampus()) {
            throw IllegalArgumentException("schoolTerm must be 1 or 2")
        }
        SinkLog.i("start get Schedule")

        if (isRefresh) {
            return getSchedule(schoolYear, schoolTerm)
        }
        val localData = AHUCache.getSchedule(schoolYear, schoolTerm)
        if (localData.isNullOrEmpty()) {
            return getSchedule(schoolYear, schoolTerm)
        }
        return MutableLiveData(Result.success(localData))

    }

    /**
     * 获取课程表 From Web
     * @param schoolYear String
     * @param schoolTerm String
     * @return LiveData<Result<(kotlin.collections.List<com.ahu.ahutong.data.model.Course>..kotlin.collections.List<com.ahu.ahutong.data.model.Course>?)>>
     */
    private fun getSchedule(schoolYear: String, schoolTerm: String) = liveData(Dispatchers.IO) {
        val result = try {
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
        emit(result)
    }


    /**
     * 查询空教室
     * @param campus String
     * @param weekday String
     * @param weekNum String
     * @param time String
     * @return LiveData<Result<List<Room>>>
     */
    fun getEmptyRoom(
        campus: String,
        weekday: String,
        weekNum: String,
        time: String
    ): LiveData<Result<List<Room>>> {
        SinkLog.i("check argument start")
        if (!campus.isCampus()) {
            throw IllegalArgumentException("campus must be 1 or 2")
        }
        if (!weekday.isWeekday()) {
            throw IllegalArgumentException("weekday must be 1-7")
        }
        if (!time.isEmptyRoomTime()) {
            throw IllegalArgumentException("time must be 1-10")
        }

        return liveData(Dispatchers.IO) {
            val result = try {
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
            emit(result)
        }
    }

    /**
     * 获取新闻 本地优先
     * @param isRefresh Boolean 是否直接获取服务器上的
     * @return LiveData<Result<List<News>>>
     */
    fun getNews(isRefresh: Boolean = false): LiveData<Result<List<News>>> {
        if (isRefresh) {
            //直接返回网络信息
            return getNews()
        }
        val news = AHUCache.getNews()
        if (news.isNullOrEmpty()) {
            return getNews()
        }
        return MutableLiveData(Result.success(news))
    }

    /**
     * 查询新闻 From Web
     * @return LiveData<Result<List<News>>>
     */
    private fun getNews(): LiveData<Result<List<News>>> {
        return liveData(Dispatchers.IO) {
            val result = try {
                val response = dataSource.getNews()
                if (response.isSuccessful) {
                    SinkLog.i("get news success")
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
            emit(result)
        }
    }

    /**
     * 查询成绩 本地优先
     * @param isRefresh Boolean 是否直接获取服务器上的
     * @return LiveData<Result<List<News>>>
     */
    fun getGrade(isRefresh: Boolean = false): LiveData<Result<Grade>> {
        if (isRefresh) {
            //直接返回网络信息
            return getGrade()
        }
        val grade = AHUCache.getGrade() ?: return getGrade()
        return MutableLiveData(Result.success(grade))
    }

    /**
     * 查询成绩 From Web
     * @return LiveData<Result<Grade>>
     */
    private fun getGrade(): LiveData<Result<Grade>> {
        return liveData(Dispatchers.IO) {
            val result = try {
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
            emit(result)
        }
    }

    /**
     * 获取考试信息 本地优先
     * @param schoolYear String
     * @param schoolTerm String
     * @param isRefresh Boolean
     * @return LiveData<Result<List<Exam>>>
     */
    fun getExamInfo(schoolYear: String, schoolTerm: String, isRefresh: Boolean = false): LiveData<Result<List<Exam>>> {
        SinkLog.i("check argument start")
        if (!schoolTerm.isCampus()){
            throw IllegalArgumentException("schoolTerm must be 1 or 2")
        }

        if (isRefresh) {
            //直接返回网络信息
            return getExamInfo(schoolYear, schoolTerm)
        }
        val examInfo = AHUCache.getExamInfo()
        if (examInfo.isNullOrEmpty()) {
            return getExamInfo(schoolYear, schoolTerm)
        }
        return MutableLiveData(Result.success(examInfo))
    }
    /**
     * 获取考试信息 From Web
     * @param schoolYear String
     * @param schoolTerm String
     * @return LiveData<Result<List<Exam>>>
     */
    private fun getExamInfo(schoolYear: String, schoolTerm: String): LiveData<Result<List<Exam>>> {
        return liveData(Dispatchers.IO) {
            val result = try {
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
            //发射结果
            emit(result)

        }
    }


}