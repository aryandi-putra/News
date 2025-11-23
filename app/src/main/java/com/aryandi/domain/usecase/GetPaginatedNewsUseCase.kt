package com.aryandi.domain.usecase

import com.aryandi.domain.common.Result
import com.aryandi.domain.model.ArticleDomain
import com.aryandi.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for handling paginated news fetching with state management
 */
class GetPaginatedNewsUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    suspend operator fun invoke(
        source: String,
        page: Int
    ): Flow<Result<List<ArticleDomain>>> {
        return repository.getNewsList(source, page)
    }

    /**
     * Merge new items with existing items for pagination
     */
    fun mergeNewsLists(
        existingItems: List<ArticleDomain>,
        newItems: List<ArticleDomain>
    ): List<ArticleDomain> {
        return existingItems + newItems
    }

    /**
     * Check if pagination should stop (no more items)
     */
    fun shouldStopPagination(items: List<ArticleDomain>): Boolean {
        return items.isEmpty()
    }
}
