package com.aryandi.data.repository

import com.aryandi.data.model.Articles
import com.aryandi.data.network.ApiResponse
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    suspend fun getNewsList(): Flow<ApiResponse<List<Articles>>>
}