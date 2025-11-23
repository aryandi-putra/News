package com.aryandi.data.network

import com.aryandi.data.model.NewsList
import com.aryandi.data.model.SourcesList
import com.aryandi.news.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("top-headlines/sources")
    suspend fun getSources(
        @Query(value ="category") category: String,
        @Query(value ="country") country: String = "us",
        @Query(value ="language") language: String = "en",
        @Query(value ="apiKey") key: String = BuildConfig.TOKEN_KEY,
    ): SourcesList

    @GET("top-headlines")
    suspend fun getNewsBySource(
        @Query(value = "sources") source: String,
        @Query(value ="page") page: Int = 1,
        @Query(value ="pageSize") pageSize: Int = 5,
        @Query(value ="apiKey") key: String = BuildConfig.TOKEN_KEY
    ): NewsList
}