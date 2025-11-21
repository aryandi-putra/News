package com.aryandi.data.repository

import com.aryandi.data.model.Articles
import com.aryandi.data.network.ApiResponse
import com.aryandi.data.network.ApiService
import com.aryandi.news.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(private val apiService: ApiService) : NewsRepository {
    override suspend fun getNewsList(): Flow<ApiResponse<List<Articles>>> = flow {
        emit(ApiResponse.Loading)
        try {
            val response = apiService.getNews(BuildConfig.TOKEN_KEY)
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