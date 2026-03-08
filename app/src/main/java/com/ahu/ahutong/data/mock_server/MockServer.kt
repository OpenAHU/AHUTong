package com.ahu.ahutong.data.mock_server


import com.ahu.ahutong.data.server.model.ApkUpdateInfo
import okhttp3.ResponseBody
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface MockServer {

//    @POST("/ocr/captcha")
//    @Multipart
//    suspend fun getCaptchaResult(@Part data: MultipartBody.Part): Captcha

    @GET("/api/check_apk_update")
    suspend fun getApkUpdateInfo(): ApkUpdateInfo

    @GET("/download/{filename}")
    suspend fun downloadFile(@Path(value = "filename", encoded = true) filename: String): Response<ResponseBody>
    
    @GET
    suspend fun downloadByUrl(@Url fileUrl: String): ResponseBody

    companion object {
        val BASE_URL = "http://192.168.31.103:5000"


        val okHttpClient = OkHttpClient
            .Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .build()


        val API = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .build().create(MockServer::class.java)
    }
}
