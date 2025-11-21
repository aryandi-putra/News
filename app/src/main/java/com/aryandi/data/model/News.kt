package com.aryandi.data.model

data class News(
    var status: String? = null,
    var totalResults: Int? = null,
    var articles: ArrayList<Articles> = arrayListOf()
)