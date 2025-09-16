package com.ahu.ahutong.data.crawler.api.jwxt


import arch.sink.utils.Utils
import com.ahu.ahutong.data.api.AHUCookieJar
import com.ahu.ahutong.data.crawler.manager.CookieManager
import com.ahu.ahutong.data.crawler.model.jwxt.CourseTable
import com.ahu.ahutong.data.crawler.model.jwxt.CurrentSemester
import com.ahu.ahutong.data.crawler.model.jwxt.CurrentTeachWeek
import com.ahu.ahutong.data.crawler.model.jwxt.GradeResponse
import com.ahu.ahutong.data.crawler.net.AutoLoginInterceptor
import com.ahu.ahutong.data.crawler.net.TokenAuthenticator
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface JwxtApi {

    @GET("/student/sso/login")
    suspend fun fetchLoginInfo(): Response<ResponseBody>

    @GET("/student/for-std/course-table/semester/{id}/print-data")
    suspend fun getCourse(
        @Path("id") semesterPathId: Int,
        @Query("semesterId") semesterQueryId: Int,
        @Query("hasExperiment") hasExperiment: Boolean = false
    ): CourseTable

    @GET("/student/for-std/course-table")
    suspend fun fetchCourseTableBasicInfo(): Response<ResponseBody>

    @GET("/student/home/get-current-teach-week")
    suspend fun getCurrentTeachWeek(): CurrentTeachWeek

    @GET("/student/for-std/exam-arrange/info/96223")
    suspend fun getExamInfo(): Response<ResponseBody>

    // To retrieve a student's examInfo/grade, you need their ID
    // This interface return's student' grade, and it also returns student's ID via its redirect URL
    // So,before you get above data, you need access this interface to get student's ID
    @GET("/student/for-std/grade/sheet")
    suspend fun getGrade(): Response<ResponseBody>

    @GET("/student/for-std/grade/sheet/info/{id}")
    suspend fun getGrade(@Path("id") id: String): GradeResponse

    @FormUrlEncoded
    @POST
    suspend fun device(
        @Url url: String,
        @Field("ul") username: Int,
        @Field("pl") password: Int,
        @Field("rsa") rsa: String,
        @Field("method") method: String = "login"
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST
    suspend fun login(
        @Url url: String,
        @Field("rsa") rsa: String,
        @Field("ul") username: Int,
        @Field("pl") password: Int,
        @Field("lt") lt: String,
        @Field("execution") execution: String = "e1s1",
        @Field("_eventId") eventId: String = "submit"
    ): Response<ResponseBody>

    companion object {
        private val BASE_URL = "https://jw.ahu.edu.cn/"

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        private val cookieJar = CookieManager.cookieJar

        val okHttpClient = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addNetworkInterceptor(AutoLoginInterceptor())
            .authenticator(TokenAuthenticator())
            .followRedirects(true)
            .followSslRedirects(true)
            .addNetworkInterceptor(loggingInterceptor)
            .build()


        val API = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(JwxtApi::class.java)
    }
}