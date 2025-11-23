package com.aryandi.domain.usecase

import com.aryandi.domain.common.Result
import com.aryandi.domain.model.SourceDomain
import com.aryandi.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSourceListUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    suspend operator fun invoke(category: String): Flow<Result<List<SourceDomain>>> {
        return repository.getSourceList(category)
    }
}
