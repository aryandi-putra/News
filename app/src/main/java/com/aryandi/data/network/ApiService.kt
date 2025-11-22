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

    @GET("top-headlines?country=us&category=technology")
    suspend fun getNewsBySource(@Query("apiKey") key: String): NewsList
}