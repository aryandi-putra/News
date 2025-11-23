package com.aryandi.domain.repository

import com.aryandi.domain.common.Result
import com.aryandi.domain.model.ArticleDomain
import com.aryandi.domain.model.SourceDomain
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    suspend fun getSourceList(category: String): Flow<Result<List<SourceDomain>>>
    suspend fun getNewsList(source: String, currentPage: Int): Flow<Result<List<ArticleDomain>>>
}
