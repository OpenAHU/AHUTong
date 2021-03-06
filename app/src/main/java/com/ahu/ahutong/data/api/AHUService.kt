package com.ahu.ahutong.data.api

import arch.sink.utils.Utils
import com.ahu.ahutong.AHUApplication
import com.ahu.ahutong.BuildConfig
import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.model.*
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * @Author: Sink
 * @Date: 2021/7/31-下午8:40
 * @Email: 468766131@qq.com
 */
interface AHUService {
    @POST("/api/login")
    @FormUrlEncoded
    suspend fun login(@Field("userId") userId: String,
                      @Field("userId") password: String,
                      @Field("userId") type: User.UserType): AHUResponse<User>

    @POST
    @FormUrlEncoded
    suspend fun logout(@Field("userId") userId: String,
                       @Field("type") type: User.UserType): AHUResponse<Unit>

    /**
     * getSchedule
     * @param schoolYear String 2020-2021
     * @param schoolTerm String 1,2
     * @return AHUResponse<List<Course>>
     */
    @GET("/api/schedule")
    suspend fun getSchedule(@Query("schoolYear") schoolYear: String,
                            @Query("schoolTerm") schoolTerm: String): AHUResponse<List<Course>>

    /**
     * getExamInfo
     * @param schoolYear String 2020-2021
     * @param schoolTerm String 1,2
     * @return AHUResponse<List<Exam>>
     */
    @GET("/api/exam_info")
    suspend fun getExamInfo(@Query("schoolYear") schoolYear: String,
                            @Query("schoolTerm") schoolTerm: String): AHUResponse<List<Exam>>

    /**
     * 获取空教室API
     * @param campus 1为新区，2为老区
     * @param weekday 星期几
     * @param weekNum 第几周
     * @param time 1为1，2节；2为3，4节；3为5，6节；4为7，8节；5为9，10，11节；6为上午；7为下午；8为晚上；9为白天；10为整天
     * @return AHUResponse<List<Room>>
     */
    @GET("/api/empty_rooms")
    suspend fun getEmptyRoom(
        @Query("campus") campus: String,
        @Query("weekday") weekday: String,
        @Query("weeknum") weekNum: String,
        @Query("time") time: String
    ): AHUResponse<List<Room>>

    /**
     * 获取成绩
     * @return AHUResponse<Grade>
     */
    @GET("/api/grade")
    suspend fun getGrade(): AHUResponse<Grade>

    /**
     * 获取院系新闻
     * @return AHUResponse<News>
     */
    @GET("/api/departmentNews")
    suspend fun getNews(): AHUResponse<List<News>>

    @GET("/api/banner")
    suspend fun getBanner(): AHUResponse<List<Banner>>
    companion object{
        private const val BASE_URL = ""
        // Cookie 本地存储
        private val cookieJar by lazy {
            PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(Utils.getApp()))
        }
        //创建AHUService对象
        val API: AHUService by lazy{
            val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            val client = OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .cache(Cache(File(Utils.getApp().cacheDir,"app_cache"), Long.MAX_VALUE))
                .retryOnConnectionFailure(true)
                .cookieJar(cookieJar)//设置CookieJar
            //debug
            if (BuildConfig.DEBUG){
                client.addInterceptor(logger)
            }
            //创建API
            Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .client(client.build())
                .build().create(API::class.java)
        }

        /**
         * 清除缓存
         */
        @JvmStatic
        fun clearCookie(){
            cookieJar.clear()
        }
    }

}