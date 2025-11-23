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
import kotlin.collections.plus

const val EXTRA_SOURCE_KEY = "EXTRA_SOURCE"

@Stable
@HiltViewModel
class NewsListViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _newsList = MutableStateFlow<ApiResponse<List<Article>>>(ApiResponse.Loading)
    val newsList = _newsList.asStateFlow()

    private var currentPage = 1
    private var isLastPage = false
    private var isLoadingMore = false
    private var currentSource: String? = null

    init {
        val source = savedStateHandle.get<String>(EXTRA_SOURCE_KEY)
        if (source != null) {
            currentSource = source
            fetchNewsList(source = source, isFirstLoad = true)
        }
    }

    fun loadMoreSources() {
        val source = currentSource ?: return
        if (!isLoadingMore && !isLastPage) {
            fetchNewsList(source, isFirstLoad = false)
        }
    }


    private fun fetchNewsList(source: String, isFirstLoad: Boolean) {
        viewModelScope.launch {
            isLoadingMore = true

            if (isFirstLoad) {
                _newsList.value = ApiResponse.Loading
            }

            newsRepository.getNewsList(
                source = source,
                currentPage = currentPage
            )
                .collect { response ->
                    when (response) {
                        is ApiResponse.Success -> {
                            val newItems = response.data
                            val currentItems =
                                if (isFirstLoad) emptyList() else (_newsList.value as? ApiResponse.Success)?.data
                                    ?: emptyList()

                            if (newItems.isEmpty()) {
                                isLastPage = true
                            } else {
                                _newsList.value = ApiResponse.Success(currentItems + newItems)
                                currentPage++
                            }
                        }

                        is ApiResponse.Error -> {
                            if (isFirstLoad) {
                                _newsList.value = response
                            } else {
                            }
                        }

                        is ApiResponse.Loading -> {
                        }
                    }
                    isLoadingMore = false
                }
        }
    }
}