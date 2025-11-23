package com.aryandi.domain.model

data class ArticleDomain(
    val source: SourceDomain? = null,
    val author: String? = null,
    val title: String? = null,
    val description: String? = null,
    val url: String? = null,
    val urlToImage: String? = null,
    val publishedAt: String? = null,
    val content: String? = null
) {
    data class SourceDomain(
        val id: String? = null,
        val name: String? = null
    )
}
