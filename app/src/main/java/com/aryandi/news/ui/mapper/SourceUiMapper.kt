package com.aryandi.news.ui.mapper

import com.aryandi.data.model.Source
import com.aryandi.domain.model.SourceDomain

fun SourceDomain.toUi(): Source {
    return Source(
        id = id,
        name = name,
        description = description,
        url = url,
        category = category,
        language = language,
        country = country
    )
}

fun List<SourceDomain>.toUi(): List<Source> {
    return this.map { it.toUi() }
}
