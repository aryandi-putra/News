package com.aryandi.news.ui.source

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aryandi.data.model.Source
import com.aryandi.data.network.ApiResponse
import com.aryandi.domain.model.SourceDomain
import com.aryandi.domain.usecase.GetSourceListUseCase
import com.aryandi.domain.usecase.SearchSourcesUseCase
import com.aryandi.news.ui.mapper.toUi
import com.aryandi.news.ui.mapper.toUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

const val EXTRA_CATEGORY_KEY = "EXTRA_CATEGORY"

@Stable
@HiltViewModel
class SourceListViewModel @Inject constructor(
    private val getSourceListUseCase: GetSourceListUseCase,
    private val searchSourcesUseCase: SearchSourcesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _sourceList = MutableStateFlow<ApiResponse<List<Source>>>(ApiResponse.Loading)

    private val _searchKeyword = MutableStateFlow("")
    val searchKeyword = _searchKeyword.asStateFlow()

    private val _filteredSourceList =
        MutableStateFlow<ApiResponse<List<Source>>>(ApiResponse.Loading)
    val filteredSourceList = _filteredSourceList.asStateFlow()

    private var currentCategory: String? = null

    init {
        val category = savedStateHandle.get<String>(EXTRA_CATEGORY_KEY)
        if (category != null) {
            currentCategory = category
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
                val keyword = _searchKeyword.value
                val domainSources = apiResponse.data.map { source ->
                    // Convert UI model back to domain for filtering
                    SourceDomain(
                        id = source.id,
                        name = source.name,
                        description = source.description,
                        url = source.url,
                        category = source.category,
                        language = source.language,
                        country = source.country
                    )
                }

                val filteredDomain = searchSourcesUseCase(domainSources, keyword)
                val filteredUi = filteredDomain.toUi()
                _filteredSourceList.value = ApiResponse.Success(filteredUi)
            }

            is ApiResponse.Error -> {
                _filteredSourceList.value = apiResponse
            }

            is ApiResponse.Loading -> {
                _filteredSourceList.value = apiResponse
            }

            ApiResponse.Empty -> {
                _filteredSourceList.value = apiResponse
            }
        }
    }

    fun retryLoad() {
        val category = currentCategory ?: return
        fetchSourceList(category)
    }

    private fun fetchSourceList(category: String) {
        viewModelScope.launch {
            _sourceList.value = ApiResponse.Loading
            getSourceListUseCase(category).collect { result ->
                val response = result.toUiState()
                _sourceList.value = when (response) {
                    is ApiResponse.Success -> ApiResponse.Success(response.data.toUi())
                    is ApiResponse.Error -> response
                    is ApiResponse.Loading -> response
                    ApiResponse.Empty -> ApiResponse.Empty
                }
            }
        }
    }
}
