package com.aryandi.domain.usecase

import com.aryandi.domain.common.Result
import com.aryandi.domain.model.ArticleDomain
import com.aryandi.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNewsListUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    suspend operator fun invoke(source: String, page: Int): Flow<Result<List<ArticleDomain>>> {
        return repository.getNewsList(source, page)
    }
}
