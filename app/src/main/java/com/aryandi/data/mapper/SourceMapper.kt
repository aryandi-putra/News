package com.aryandi.data.mapper

import com.aryandi.data.model.Source
import com.aryandi.domain.model.SourceDomain

fun Source.toDomain(): SourceDomain {
    return SourceDomain(
        id = id,
        name = name,
        description = description,
        url = url,
        category = category,
        language = language,
        country = country
    )
}

fun SourceDomain.toData(): Source {
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

fun List<Source>.toDomain(): List<SourceDomain> {
    return this.map { it.toDomain() }
}
