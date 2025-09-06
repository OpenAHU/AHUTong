package com.ahu.ahutong.data.crawler.api.adwmh

import android.content.Context
import arch.sink.utils.Utils
import com.ahu.ahutong.data.api.AHUCookieJar
import com.ahu.ahutong.data.crawler.api.jwxt.JwxtApi
import com.ahu.ahutong.data.crawler.manager.CookieManager
import com.ahu.ahutong.data.crawler.model.adwnh.Balance
import com.ahu.ahutong.data.crawler.model.adwnh.Captcha
import com.ahu.ahutong.data.crawler.model.adwnh.Info
import com.ahu.ahutong.data.crawler.model.adwnh.QRcode
import com.ahu.ahutong.data.crawler.net.AutoLoginInterceptor
import com.ahu.ahutong.data.crawler.net.TokenAuthenticator
import com.ahu.ahutong.data.dao.AHUCache
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

interface AdwmhApi {
    @POST("/user/session")
    suspend fun fetchSession(): ResponseBody

    @GET("/remind/authcode")
    suspend fun getAuthCode(): ResponseBody

    @POST("/user/login")
    @FormUrlEncoded
    suspend fun loginWithCaptcha(
        @Field("username") username: String,
        @Field("pwd") password: String,
        @Field("flag") flag: Int,
        @Field("imgcode") imgcode: String
    ): Info


    @GET("/xzxcard/yue")
    suspend fun getBalance(): Balance


    @GET("/xzxcard/qrcode")
    suspend fun getQrcode(): QRcode

    @POST
    @Multipart
    suspend fun getCaptchaResult(@Url url: String,@Part file: MultipartBody.Part): Captcha



    companion object {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        private val cookieJar = CookieManager.cookieJar
//        val cookieJar = PersistentCookieJar(
//            SetCookieCache(),
//            SharedPrefsCookiePersistor(MyApp.instance.applicationContext)
//        )

//        private val loginInterceptor =  RedirectLoginInterceptor()

        val BASE_URL = "https://adwmh.ahu.edu.cn/"


        val okHttpClient = OkHttpClient
            .Builder()
            .addNetworkInterceptor(AutoLoginInterceptor())
            .authenticator(TokenAuthenticator())
            .followRedirects(true)
            .followSslRedirects(true)
            .cookieJar(cookieJar)
            .addNetworkInterceptor(loggingInterceptor)
            .build()

        val API = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .build().create(AdwmhApi::class.java)

    }
}