package com.aryandi.data.model

data class NewsList(
    var status: String? = null,
    var totalResults: Int? = null,
    var articles: ArrayList<Article> = arrayListOf()
)