package com.aryandi.domain.usecase

import com.aryandi.domain.model.SourceDomain
import javax.inject.Inject

class SearchSourcesUseCase @Inject constructor() {

    operator fun invoke(
        sources: List<SourceDomain>,
        keyword: String
    ): List<SourceDomain> {
        val trimmedKeyword = keyword.trim()
        if (trimmedKeyword.isEmpty()) {
            return sources
        }

        return sources.filter { source ->
            source.name?.contains(trimmedKeyword, ignoreCase = true) == true ||
                    source.description?.contains(trimmedKeyword, ignoreCase = true) == true ||
                    source.category?.contains(trimmedKeyword, ignoreCase = true) == true
        }
    }
}
