package com.aryandi.data.repository

import com.aryandi.data.model.Article
import com.aryandi.data.model.Source
import com.aryandi.data.network.ApiResponse
import com.aryandi.data.network.ApiService
import com.aryandi.news.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(private val apiService: ApiService) : NewsRepository {
    override suspend fun getSourceList(category: String): Flow<ApiResponse<List<Source>>> = flow {
        emit(ApiResponse.Loading)
        try {
            val response = apiService.getSources(category = category, key = BuildConfig.TOKEN_KEY)
            if (response.sources.isNotEmpty()) {
                emit(ApiResponse.Success(response.sources))
            } else {
                emit(ApiResponse.Error("No news available now, please check after a while!"))
            }
        } catch (exception: Exception) {
            emit(ApiResponse.Error(exception.message ?: "Unexpected Error"))
        }
    }

    override suspend fun getNewsList(): Flow<ApiResponse<List<Article>>> = flow {
        emit(ApiResponse.Loading)
        try {
            val response = apiService.getNewsBySource(BuildConfig.TOKEN_KEY)
            if (response.articles.isNotEmpty()) {
                emit(ApiResponse.Success(response.articles))
            } else {
                emit(ApiResponse.Error("No news available now, please check after a while!"))
            }
        } catch (exception: Exception) {
            emit(ApiResponse.Error(exception.message ?: "Unexpected Error"))
        }
    }
}