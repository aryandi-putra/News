package com.aryandi.data.mapper

import com.aryandi.data.model.Article
import com.aryandi.domain.model.ArticleDomain

fun Article.toDomain(): ArticleDomain {
    return ArticleDomain(
        source = source?.let {
            ArticleDomain.SourceDomain(
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

fun ArticleDomain.toData(): Article {
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

fun List<Article>.toDomain(): List<ArticleDomain> {
    return this.map { it.toDomain() }
}
