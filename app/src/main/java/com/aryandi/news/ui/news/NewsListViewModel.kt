package com.aryandi.news.ui.news

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aryandi.data.model.Article
import com.aryandi.data.network.ApiResponse
import com.aryandi.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

const val EXTRA_SOURCE_KEY = "EXTRA_SOURCE"

@Stable
@HiltViewModel
class NewsListViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _newsList = MutableStateFlow<ApiResponse<List<Article>>>(ApiResponse.Loading)
    val newsList = _newsList.asStateFlow()

    init {
        val source = savedStateHandle.get<String>(EXTRA_SOURCE_KEY)
        if (source != null) {
            fetchNewsList(source)
        }
    }

    private fun fetchNewsList(source: String) {
        viewModelScope.launch {
            newsRepository.getNewsList(source).collect { response ->
                _newsList.value = response
            }
        }
    }
}