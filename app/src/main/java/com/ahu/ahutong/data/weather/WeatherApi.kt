package com.ahu.ahutong.data.weather

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface WeatherApi {

    @GET("api/v1/misc/weather")
    suspend fun getWeather(
        @Query("city") city: String? = null,
        @Query("adcode") adcode: String? = null,
        @Query("extended") extended: Boolean = true,
        @Query("indices") indices: Boolean = true,
        @Query("forecast") forecast: Boolean = true,
        @Query("hourly") hourly: Boolean = true,
        @Query("lang") lang: String = "zh"
    ): WeatherResponse

    companion object {
        private val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        private val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()

        val API: WeatherApi = Retrofit.Builder()
            .baseUrl("https://uapis.cn/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }
}
