package com.ahu.ahutong.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.ahu.ahutong.data.api.APIDataSource
import com.ahu.ahutong.data.base.BaseDataSource
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.Room
import com.ahu.ahutong.data.model.Rubbish
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
    fun searchRubbish(keyword: String) = liveData(Dispatchers.IO){
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
            val response =  client.newCall(request).execute()
            if (response.isSuccessful){
                val body = response.body
                val jsonElement = JsonParser.parseString(body?.string() ?: "").asJsonObject["newslist"]
                if (jsonElement == null){
                    Result.failure<List<Rubbish>>(Throwable("返回结果为空"))
                }else{
                    Result.success(Gson().fromJson(jsonElement, object: TypeToken<List<Rubbish>>(){}.type))
                }
            }else{
                Result.failure(Throwable("response status is ${response.code}"))
            }
        }catch (e: Exception){
            Result.failure(e)
        }
        //发射结果
        emit(result)
    }


    fun getSchedule(schoolYear: String, schoolTerm: String, isRefresh: Boolean): LiveData<Result<List<Course>>>{
        SinkLog.i("check argument start")
        if (!schoolTerm.isCampus()){
            throw IllegalArgumentException("schoolTerm must be 1 or 2")
        }
        SinkLog.i("start get Schedule")
        if (isRefresh) {
            return getSchedule(schoolYear, schoolTerm)
        }
        val localData = AHUCache.getSchedule(schoolYear, schoolTerm)
        if (localData.isNullOrEmpty()){
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
            if (response.isSuccessful){
                //缓存
                AHUCache.saveSchedule(schoolYear, schoolTerm, response.data)
                Result.success(response.data)
            }else{
                Result.failure<List<Course>>(Throwable(response.msg))
            }
        }catch (e: Exception){
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
    fun getEmptyRoom(campus: String, weekday: String, weekNum: String, time: String): LiveData<Result<List<Room>>> {
        SinkLog.i("check argument start")
        if (!campus.isCampus()){
            throw IllegalArgumentException("campus must be 1 or 2")
        }
        if (!weekday.isWeekday()){
            throw IllegalArgumentException("weekday must be 1-7")
        }
        if (!time.isEmptyRoomTime()){
            throw IllegalArgumentException("time must be 1-10")
        }

        return liveData(Dispatchers.IO){
            val result = try {
                SinkLog.i("start get emptyRoom")
                val response = dataSource.getEmptyRoom(campus, weekday, weekNum, time)
                if (response.isSuccessful){
                    SinkLog.i("get emptyRoom success")
                    Result.success(response.data)
                }else{
                    SinkLog.e(response)
                    Result.failure(Throwable(response.msg))
                }
            }catch (e: Exception){
                SinkLog.e(e)
                Result.failure(e)
            }
            emit(result)
        }
    }


}