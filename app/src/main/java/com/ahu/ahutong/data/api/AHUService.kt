package com.ahu.ahutong.data.api

import arch.sink.utils.Utils
import com.ahu.ahutong.AHUApplication
import com.ahu.ahutong.BuildConfig
import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.api.interceptor.ServerErrorInterceptor
import com.ahu.ahutong.data.model.*
import com.ahu.ahutong.utils.SinkCookieJar
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
    suspend fun login(
        @Field("userId") userId: String,
        @Field("password") password: String,
        @Field("type") type: User.UserType
    ): AHUResponse<User>

    @GET("/api/logout")
    suspend fun logout(@Query("type") type: User.UserType): AHUResponse<Unit>

    /**
     * getSchedule
     * @param schoolYear String 2020-2021
     * @param schoolTerm String 1,2
     * @return AHUResponse<List<Course>>
     */
    @GET("/api/schedule")
    suspend fun getSchedule(
        @Query("schoolYear") schoolYear: String,
        @Query("schoolTerm") schoolTerm: String
    ): AHUResponse<List<Course>>

    /**
     * getExamInfo
     * @param schoolYear String 2020-2021
     * @param schoolTerm String 1,2
     * @return AHUResponse<List<Exam>>
     */
    @GET("/api/examInfo")
    suspend fun getExamInfo(
        @Query("schoolYear") schoolYear: String,
        @Query("schoolTerm") schoolTerm: String
    ): AHUResponse<List<Exam>>

    /**
     * 获取空教室API
     * @param campus 1为新区，2为老区
     * @param weekday 星期几
     * @param weekNum 第几周
     * @param time 1为1，2节；2为3，4节；3为5，6节；4为7，8节；5为9，10，11节；6为上午；7为下午；8为晚上；9为白天；10为整天
     * @return AHUResponse<List<Room>>
     */
    @GET("/api/emptyRoom")
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

    @GET("/api/banner/all")
    suspend fun getBanner(): AHUResponse<List<Banner>>

    /**
     * 获取校园卡余额
     * @return AHUResponse<Card>
     */
    @GET("/api/campusCardBalance")
    suspend fun getCardMoney(): AHUResponse<Card>

    /**
     * 获取最新版本
     * @return AHUResponse<AppVersion>
     */
    @GET("/api/android/version")
    suspend fun getLatestVersion(): AHUResponse<AppVersion>

    /**
     * 日活统计接口
     * @return Unit
     */
    @GET("/api/android/access")
    suspend fun addAppAccess(): Unit

    companion object {
        private const val BASE_URL = "https://ahuer.cn"

        // Cookie 本地存储
        private val cookieJar =
            SinkCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(Utils.getApp()))

        //创建AHUService对象
        val API: AHUService by lazy {
            val logger =
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            val client = OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .cache(Cache(File(Utils.getApp().cacheDir, "app_cache"), Long.MAX_VALUE))
                .retryOnConnectionFailure(true)
                .cookieJar(cookieJar)   //设置CookieJar
                .addInterceptor(ServerErrorInterceptor())  // 后端状态检测拦截器
            //debug
            if (BuildConfig.DEBUG) {
                client.addInterceptor(logger)
            }
            //创建API
            Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .client(client.build())
                .build().create(AHUService::class.java)
        }

        /**
         * 清除缓存
         */
        @JvmStatic
        fun clearCookie() {
            cookieJar.clear()
        }
    }

}