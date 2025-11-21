package com.aryandi.data.network

import com.aryandi.data.model.News
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("top-headlines?country=us")
    suspend fun getNews(@Query("apiKey")key: String): News
}