package com.ahu.ahutong.data.crawler.api.jwxt


import arch.sink.utils.Utils
import com.ahu.ahutong.data.api.AHUCookieJar
import com.ahu.ahutong.data.crawler.model.jwxt.CourseTable
import com.ahu.ahutong.data.crawler.model.jwxt.CurrentSemester
import com.ahu.ahutong.data.crawler.model.jwxt.CurrentTeachWeek
import com.ahu.ahutong.data.crawler.net.AutoLoginInterceptor
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
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
        @Query("hasExperiment") hasExperiment: Boolean=false
    ): CourseTable

    @GET("/student/for-std/course-table")
    suspend fun fetchCourseTableBasicInfo():Response<ResponseBody>

    @GET("/student/home/get-current-teach-week")
    suspend fun getCurrentTeachWeek(): CurrentTeachWeek

    @FormUrlEncoded
    @POST
    suspend fun login(
        @Url url: String,
        @Field("rsa") rsa: String,
        @Field("ul") username: Int,
        @Field("pl") password: Int,
        @Field("lt") lt: String,
        @Field("execution") execution: String = "e1s1",
        @Field("_eventId") eventId: String ="submit"
    ): Response<ResponseBody>

    companion object{
        private val BASE_URL = "https://jw.ahu.edu.cn/"

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        private val cookieJar = AHUCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(Utils.getApp()))

        val okHttpClient = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(AutoLoginInterceptor())
            .followRedirects(true)
            .followSslRedirects(true)
            .addInterceptor(loggingInterceptor)

            .build()


        val API = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(JwxtApi::class.java)
    }
}