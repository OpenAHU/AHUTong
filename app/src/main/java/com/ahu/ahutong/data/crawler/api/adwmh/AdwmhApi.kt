package com.ahu.ahutong.data.crawler.api.adwmh

import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.crawler.manager.CookieManager
import com.ahu.ahutong.data.crawler.model.adwnh.AllCampus
import com.ahu.ahutong.data.crawler.model.adwnh.AllLostFoundType
import com.ahu.ahutong.data.crawler.model.adwnh.Balance
import com.ahu.ahutong.data.crawler.model.adwnh.Captcha
import com.ahu.ahutong.data.crawler.model.adwnh.Info
import com.ahu.ahutong.data.crawler.model.adwnh.LostFoundPublishRequest
import com.ahu.ahutong.data.crawler.model.adwnh.LostFoundResponse
import com.ahu.ahutong.data.crawler.model.adwnh.QRcode
import com.ahu.ahutong.data.crawler.net.AutoLoginInterceptor
import com.ahu.ahutong.data.crawler.net.TokenAuthenticator
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Url

interface AdwmhApi {
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

    @GET("/lostfound/campus/all")
    suspend fun getAllcampus(): AllCampus

    @GET("/lostfound/type/all")
    suspend fun getAlllostfoundtype(): AllLostFoundType

    @GET("/lostfound/all")
    suspend fun getLostFoundList(
        @Query("pageNo") pageNo: Int,
        @Query("pageSize") pageSize: Int,
        @Query("state") state: Int
    ): LostFoundResponse//state=1是招领物品，state=2是寻找物品

    @POST("lostfound/saveupdate")
    suspend fun publishLostFound(
        @Body request: LostFoundPublishRequest
    ): AHUResponse<Any>

    @FormUrlEncoded
    @POST("lostfound/delete")
    suspend fun deleteLostFound(
        @Field("id") id: String
    ): AHUResponse<Any>

    @POST
    @Multipart
    suspend fun getCaptchaResult(@Url url: String,@Part data: MultipartBody.Part): Captcha



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