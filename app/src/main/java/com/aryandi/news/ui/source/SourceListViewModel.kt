package com.aryandi.news.ui.source

import androidx.compose.runtime.Stable
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

@Stable
@HiltViewModel
class SourceListViewModel @Inject constructor(private val newsRepository: NewsRepository) :
    ViewModel() {
    private val _sourceList = MutableStateFlow<ApiResponse<List<Source>>>(ApiResponse.Loading)
    val sourceList = _sourceList.asStateFlow()

    init {
        fetchSourceList()
    }

    private fun fetchSourceList() {
        viewModelScope.launch {
            newsRepository.getSourceList("technology").collect { response ->
                _sourceList.value = response
            }
        }
    }
}