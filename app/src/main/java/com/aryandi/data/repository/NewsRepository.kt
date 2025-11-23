package com.aryandi.data.repository

import com.aryandi.data.model.Article
import com.aryandi.data.model.Source
import com.aryandi.data.network.ApiResponse
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    suspend fun getSourceList(category: String): Flow<ApiResponse<List<Source>>>
    suspend fun getNewsList(source: String, currentPage: Int): Flow<ApiResponse<List<Article>>>
}