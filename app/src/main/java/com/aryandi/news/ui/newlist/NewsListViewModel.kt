package com.aryandi.news.ui.newlist

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aryandi.data.model.Articles
import com.aryandi.data.network.ApiResponse
import com.aryandi.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class NewsListViewModel @Inject constructor(private val newsRepository: NewsRepository) :
    ViewModel() {
    private val _newsList = MutableStateFlow<ApiResponse<List<Articles>>>(ApiResponse.Loading)
    val newsList = _newsList.asStateFlow()

    init {
        fetchNewsList()
    }

    private fun fetchNewsList() {
        viewModelScope.launch {
            newsRepository.getNewsList().collect { response ->
                _newsList.value = response
            }
        }
    }
}