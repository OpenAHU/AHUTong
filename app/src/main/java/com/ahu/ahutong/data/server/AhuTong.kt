package com.ahu.ahutong.data.server


import com.ahu.ahutong.BuildConfig
import com.ahu.ahutong.data.server.model.ApkUpdateInfo
import com.ahu.ahutong.data.server.model.Captcha
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

interface AhuTong {

    @POST("/ocr/captcha")
    @Multipart
    suspend fun getCaptchaResult(@Part data: MultipartBody.Part): Captcha

    @GET("/api/check_apk_update")
    suspend fun getApkUpdateInfo(): ApkUpdateInfo

    @GET("/download/{filename}")
    suspend fun downloadFile(@Path(value = "filename", encoded = true) filename: String): retrofit2.Response<ResponseBody>

    @Streaming
    @GET
    suspend fun downloadByUrl(@Url fileUrl: String): retrofit2.Response<ResponseBody>


    companion object {
        val BASE_URL = "https://openahu.org"


        val okHttpClient = OkHttpClient
            .Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(15, TimeUnit.MINUTES)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .followSslRedirects(true)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "AHUTong/${BuildConfig.VERSION_NAME} (Android)")
                    .header("Accept", "*/*")
                    .build()
                chain.proceed(request)
            }
            .build()


        val API = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .build().create(AhuTong::class.java)
    }
}
