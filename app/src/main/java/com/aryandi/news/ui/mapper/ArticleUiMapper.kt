package com.aryandi.news.ui.mapper

import com.aryandi.data.model.Article
import com.aryandi.domain.model.ArticleDomain

fun ArticleDomain.toUi(): Article {
    return Article(
        source = source?.let {
            Article.Source(
                id = it.id,
                name = it.name
            )
        },
        author = author,
        title = title,
        description = description,
        url = url,
        urlToImage = urlToImage,
        publishedAt = publishedAt,
        content = content
    )
}

fun List<ArticleDomain>.toUi(): List<Article> {
    return this.map { it.toUi() }
}
