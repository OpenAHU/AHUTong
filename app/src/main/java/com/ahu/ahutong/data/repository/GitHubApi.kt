package com.ahu.ahutong.data.repository

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GitHubApi {

    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getContents(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path", encoded = true) path: String = "",
        @Query("ref") ref: String = "master"
    ): List<GitHubContentItem>

    @GET("repos/{owner}/{repo}/git/trees/{tree}")
    suspend fun getTree(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("tree") tree: String,
        @Query("recursive") recursive: String = "1"
    ): GitHubTreeResponse

    companion object {
        private const val BASE_URL = "https://api.github.com/"

        val instance: GitHubApi by lazy {
            val client = OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(12, TimeUnit.SECONDS)
                .callTimeout(18, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("Accept", "application/vnd.github.v3+json")
                        .header("Cache-Control", "no-cache")
                        .header("User-Agent", "AHUTong-Android")
                        .build()
                    chain.proceed(request)
                }
                .build()

            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GitHubApi::class.java)
        }
    }
}
