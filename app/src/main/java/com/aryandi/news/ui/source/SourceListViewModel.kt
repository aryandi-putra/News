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

    init {
        val category = savedStateHandle.get<String>(EXTRA_CATEGORY_KEY)
        if (category != null) {
            fetchSourceList(category)
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