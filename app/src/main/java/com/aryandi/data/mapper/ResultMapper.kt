package com.aryandi.data.mapper

import com.aryandi.data.network.ApiResponse
import com.aryandi.domain.common.Result

fun <T, R> ApiResponse<T>.toResult(mapper: (T) -> R): Result<R> {
    return when (this) {
        is ApiResponse.Loading -> Result.Loading
        is ApiResponse.Empty -> Result.Empty
        is ApiResponse.Success -> Result.Success(mapper(data))
        is ApiResponse.Error -> Result.Error(message)
    }
}
