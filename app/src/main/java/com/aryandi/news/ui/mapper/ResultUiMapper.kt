package com.aryandi.news.ui.mapper

import com.aryandi.data.network.ApiResponse
import com.aryandi.domain.common.Result

fun <T> Result<T>.toUiState(): ApiResponse<T> {
    return when (this) {
        is Result.Loading -> ApiResponse.Loading
        is Result.Empty -> ApiResponse.Empty
        is Result.Success -> ApiResponse.Success(data)
        is Result.Error -> ApiResponse.Error(message)
    }
}
