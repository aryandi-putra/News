package com.aryandi.data.network

import com.aryandi.data.model.NewsList
import com.aryandi.data.model.SourcesList
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("top-headlines/sources?country=us")
    suspend fun getSources(
        @Query("category") category: String,
        @Query("apiKey") key: String
    ): SourcesList

    @GET("top-headlines")
    suspend fun getNewsBySource(
        @Query(value = "sources") source: String,
        @Query("apiKey") key: String,
    ): NewsList
}