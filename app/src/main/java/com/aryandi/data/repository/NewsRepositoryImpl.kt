package com.aryandi.data.repository

import com.aryandi.data.mapper.toDomain
import com.aryandi.data.network.ApiService
import com.aryandi.domain.common.Result
import com.aryandi.domain.model.ArticleDomain
import com.aryandi.domain.model.SourceDomain
import com.aryandi.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : NewsRepository {

    override suspend fun getSourceList(category: String): Flow<Result<List<SourceDomain>>> = flow {
        emit(Result.Loading)
        try {
            val response = apiService.getSources(category = category)
            if (response.sources.isNotEmpty()) {
                emit(Result.Success(response.sources.toDomain()))
            } else {
                emit(Result.Empty)
            }
        } catch (exception: Exception) {
            emit(Result.Error(exception.message ?: "Unexpected Error"))
        }
    }

    override suspend fun getNewsList(
        source: String,
        currentPage: Int
    ): Flow<Result<List<ArticleDomain>>> = flow {
        emit(Result.Loading)
        try {
            val response = apiService.getNewsBySource(source = source, page = currentPage)
            if (response.articles.isNotEmpty()) {
                emit(Result.Success(response.articles.toDomain()))
            } else {
                emit(Result.Empty)
            }
        } catch (exception: Exception) {
            emit(Result.Error(exception.message ?: "Unexpected Error"))
        }
    }
}
