package com.aryandi.news.ui.news

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aryandi.data.model.Article
import com.aryandi.data.network.ApiResponse
import com.aryandi.domain.model.ArticleDomain
import com.aryandi.domain.usecase.GetPaginatedNewsUseCase
import com.aryandi.domain.usecase.SearchNewsUseCase
import com.aryandi.news.ui.mapper.toUi
import com.aryandi.news.ui.mapper.toUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

const val EXTRA_SOURCE_KEY = "EXTRA_SOURCE"

@Stable
@HiltViewModel
class NewsListViewModel @Inject constructor(
    private val getPaginatedNewsUseCase: GetPaginatedNewsUseCase,
    private val searchNewsUseCase: SearchNewsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _newsList = MutableStateFlow<ApiResponse<List<Article>>>(ApiResponse.Loading)
    val newsList = _newsList.asStateFlow()

    private val _searchKeyword = MutableStateFlow("")
    val searchKeyword = _searchKeyword.asStateFlow()

    private val _filteredNewsList =
        MutableStateFlow<ApiResponse<List<Article>>>(ApiResponse.Loading)
    val filteredNewsList = _filteredNewsList.asStateFlow()

    private val _paginationError = MutableStateFlow<String?>(null)
    val paginationError = _paginationError.asStateFlow()

    private var currentPage = 1
    private var isLastPage = false
    private var isLoadingMore = false
    private var currentSource: String? = null
    private var lastFailedPage: Int? = null

    init {
        val source = savedStateHandle.get<String>(EXTRA_SOURCE_KEY)
        if (source != null) {
            currentSource = source
            fetchNewsList(source = source, isFirstLoad = true)
        }

        viewModelScope.launch {
            _newsList.collect { apiResponse ->
                applyFilter(apiResponse)
            }
        }
    }

    fun updateSearchKeyword(keyword: String) {
        _searchKeyword.value = keyword
        applyFilter(_newsList.value)
    }

    private fun applyFilter(apiResponse: ApiResponse<List<Article>>) {
        when (apiResponse) {
            is ApiResponse.Success -> {
                val keyword = _searchKeyword.value
                val domainArticles = apiResponse.data.map { article ->
                    // Convert UI model back to domain for filtering
                    ArticleDomain(
                        source = article.source?.let {
                            ArticleDomain.SourceDomain(
                                id = it.id,
                                name = it.name
                            )
                        },
                        author = article.author,
                        title = article.title,
                        description = article.description,
                        url = article.url,
                        urlToImage = article.urlToImage,
                        publishedAt = article.publishedAt,
                        content = article.content
                    )
                }

                val filteredDomain = searchNewsUseCase(domainArticles, keyword)
                val filteredUi = filteredDomain.toUi()
                _filteredNewsList.value = ApiResponse.Success(filteredUi)
            }

            is ApiResponse.Error -> {
                _filteredNewsList.value = apiResponse
            }

            is ApiResponse.Loading -> {
                _filteredNewsList.value = apiResponse
            }

            ApiResponse.Empty -> {
                _filteredNewsList.value = apiResponse
            }
        }
    }

    fun loadMoreSources() {
        val source = currentSource ?: return
        if (!isLoadingMore && !isLastPage) {
            fetchNewsList(source, isFirstLoad = false)
        }
    }

    fun retryInitialLoad() {
        val source = currentSource ?: return
        currentPage = 1
        isLastPage = false
        lastFailedPage = null
        fetchNewsList(source, isFirstLoad = true)
    }

    fun retryPagination() {
        val source = currentSource ?: return
        val failedPage = lastFailedPage ?: return
        if (!isLoadingMore) {
            currentPage = failedPage
            lastFailedPage = null
            _paginationError.value = null
            fetchNewsList(source, isFirstLoad = false)
        }
    }

    fun dismissPaginationError() {
        _paginationError.value = null
        lastFailedPage = null
    }

    private fun fetchNewsList(source: String, isFirstLoad: Boolean) {
        viewModelScope.launch {
            isLoadingMore = true

            if (isFirstLoad) {
                _newsList.value = ApiResponse.Loading
            }

            getPaginatedNewsUseCase(
                source = source,
                page = currentPage
            )
                .collect { result ->
                    val response = result.toUiState()
                    when (response) {
                        is ApiResponse.Success -> {
                            val newItemsDomain = response.data

                            // Use use case to check if pagination should stop
                            if (getPaginatedNewsUseCase.shouldStopPagination(newItemsDomain)) {
                                isLastPage = true
                            } else {
                                val currentItemsDomain = if (isFirstLoad) {
                                    emptyList()
                                } else {
                                    (_newsList.value as? ApiResponse.Success)?.data?.map { article ->
                                        ArticleDomain(
                                            source = article.source?.let {
                                                ArticleDomain.SourceDomain(
                                                    id = it.id,
                                                    name = it.name
                                                )
                                            },
                                            author = article.author,
                                            title = article.title,
                                            description = article.description,
                                            url = article.url,
                                            urlToImage = article.urlToImage,
                                            publishedAt = article.publishedAt,
                                            content = article.content
                                        )
                                    } ?: emptyList()
                                }

                                // Use use case to merge lists
                                val mergedDomain = getPaginatedNewsUseCase.mergeNewsLists(
                                    currentItemsDomain,
                                    newItemsDomain
                                )

                                _newsList.value = ApiResponse.Success(mergedDomain.toUi())
                                currentPage++
                            }
                        }

                        is ApiResponse.Error -> {
                            if (isFirstLoad) {
                                _newsList.value = response
                            } else {
                                // Store the failed page for retry
                                lastFailedPage = currentPage
                                _paginationError.value = response.message
                            }
                        }

                        is ApiResponse.Loading -> {
                        }

                        ApiResponse.Empty -> {
                            if (isFirstLoad) {
                                _newsList.value = ApiResponse.Empty
                            }
                        }
                    }
                    isLoadingMore = false
                }
        }
    }
}
