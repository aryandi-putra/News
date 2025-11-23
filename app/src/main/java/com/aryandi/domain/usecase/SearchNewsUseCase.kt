package com.aryandi.domain.usecase

import com.aryandi.domain.model.ArticleDomain
import javax.inject.Inject

class SearchNewsUseCase @Inject constructor() {

    operator fun invoke(
        articles: List<ArticleDomain>,
        keyword: String
    ): List<ArticleDomain> {
        val trimmedKeyword = keyword.trim()
        if (trimmedKeyword.isEmpty()) {
            return articles
        }

        return articles.filter { article ->
            article.title?.contains(trimmedKeyword, ignoreCase = true) == true ||
                    article.description?.contains(trimmedKeyword, ignoreCase = true) == true ||
                    article.author?.contains(trimmedKeyword, ignoreCase = true) == true ||
                    article.content?.contains(trimmedKeyword, ignoreCase = true) == true
        }
    }
}
