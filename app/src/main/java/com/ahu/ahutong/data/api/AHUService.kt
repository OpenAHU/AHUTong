package com.ahu.ahutong.data.api

import android.util.Log
import arch.sink.utils.Utils
import com.ahu.ahutong.AHUApplication
import com.ahu.ahutong.BuildConfig
import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.*
import com.ahu.ahutong.ext.awaitString
import com.ahu.ahutong.ui.page.state.MainViewModel
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import okio.GzipSource
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
     * 获取成绩
     * @return AHUResponse<Grade>
     */
    @GET("/api/grade")
    suspend fun getGrade(): AHUResponse<Grade>

    // 获取banner
    @GET("/api/banner/all")
    suspend fun getBanner(): AHUResponse<List<Banner>>

    /**
     * 获取校园卡余额
     * @return AHUResponse<Card>
     */
    @GET("/api/campusCardBalance")
    suspend fun getCardMoney(): AHUResponse<Card>

    /**
     * 获取浴室开放
     * @return AHUResponse<BathRoom>
     */
    @GET("/api/bathroom/open")
    suspend fun getBathRooms(): AHUResponse<List<BathRoom>>

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
            AHUCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(Utils.getApp()))

        //创建AHUService对象
        val API: AHUService by lazy {
            val logger = HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .addInterceptor(LoginStateInterceptor())
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .cache(Cache(File(Utils.getApp().cacheDir, "app_cache"), Long.MAX_VALUE))
                .retryOnConnectionFailure(true)
                .cookieJar(cookieJar)   //设置CookieJar
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

        class LoginStateInterceptor : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val response = chain.proceed(chain.request())
                val responseBody = response.body
                if (response.code == 400 && responseBody != null) {
                    // 解决 Response#string() 函数仅能调用一次的问题
                    val source = responseBody.source()
                    source.request(Long.MAX_VALUE)
                    var buffer = source.buffer
                    if ("gzip".equals(response.headers["Content-Encoding"], ignoreCase = true)) {
                        GzipSource(buffer.clone()).use { gzippedResponseBody ->
                            buffer = Buffer()
                            buffer.writeAll(gzippedResponseBody)
                        }
                    }
                    val string = buffer.clone().readString(Charsets.UTF_8)

                    if (!string.contains("session", ignoreCase = true) || !AHUCache.isLogin()) {
                        return response
                    }
                    // 登录状态过期
                    AHUApplication.sessionUpdated.callFromOtherThread()
                    // 修改状态码，不抛出异常
                    return response.newBuilder()
                        .code(200).build()
                }
                return response
            }

        }
    }

}