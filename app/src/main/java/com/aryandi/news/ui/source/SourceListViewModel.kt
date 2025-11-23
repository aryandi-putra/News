package com.aryandi.news.ui.source

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aryandi.data.model.Source
import com.aryandi.data.network.ApiResponse
import com.aryandi.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

const val EXTRA_CATEGORY_KEY = "EXTRA_CATEGORY"

@Stable
@HiltViewModel
class SourceListViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _sourceList = MutableStateFlow<ApiResponse<List<Source>>>(ApiResponse.Loading)
    val sourceList = _sourceList.asStateFlow()

    private val _searchKeyword = MutableStateFlow("")
    val searchKeyword = _searchKeyword.asStateFlow()

    private val _filteredSourceList =
        MutableStateFlow<ApiResponse<List<Source>>>(ApiResponse.Loading)
    val filteredSourceList = _filteredSourceList.asStateFlow()

    init {
        val category = savedStateHandle.get<String>(EXTRA_CATEGORY_KEY)
        if (category != null) {
            fetchSourceList(category)
        }

        // Apply filter whenever source list or search keyword changes
        viewModelScope.launch {
            _sourceList.collect { apiResponse ->
                applyFilter(apiResponse)
            }
        }
    }

    fun updateSearchKeyword(keyword: String) {
        _searchKeyword.value = keyword
        applyFilter(_sourceList.value)
    }

    private fun applyFilter(apiResponse: ApiResponse<List<Source>>) {
        when (apiResponse) {
            is ApiResponse.Success -> {
                val keyword = _searchKeyword.value.trim()
                if (keyword.isEmpty()) {
                    _filteredSourceList.value = apiResponse
                } else {
                    val filtered = apiResponse.data.filter { source ->
                        source.name?.contains(keyword, ignoreCase = true) == true ||
                                source.description?.contains(keyword, ignoreCase = true) == true ||
                                source.category?.contains(keyword, ignoreCase = true) == true
                    }
                    _filteredSourceList.value = ApiResponse.Success(filtered)
                }
            }

            is ApiResponse.Error -> {
                _filteredSourceList.value = apiResponse
            }

            is ApiResponse.Loading -> {
                _filteredSourceList.value = apiResponse
            }
        }
    }

    private fun fetchSourceList(category: String) {
        viewModelScope.launch {
            newsRepository.getSourceList(category).collect { response ->
                _sourceList.value = response
            }
        }
    }
}