package com.aryandi.news.data.repository

import com.aryandi.data.model.Article
import com.aryandi.data.model.NewsList
import com.aryandi.data.model.Source
import com.aryandi.data.model.SourcesList
import com.aryandi.data.network.ApiResponse
import com.aryandi.data.network.ApiService
import com.aryandi.data.repository.NewsRepository
import com.aryandi.data.repository.NewsRepositoryImpl
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class NewsRepositoryImplTest {

    lateinit var repository: NewsRepository
    private val apiService: ApiService = mockk()

    @Before
    fun initialSetUp() {
        repository = NewsRepositoryImpl(apiService)
    }

    @Test
    fun testGetSourceResponseSuccess() = runTest {
        coEvery { apiService.getSources("general") } returns (getSourcesSuccessData())
        val response = repository.getSourceList("general")
        assertEquals(ApiResponse.Success(getSourcesSuccessData().sources), response.last())
    }

    @Test
    fun testGetNewsBySourceResponseSuccess() = runTest {
        coEvery { apiService.getNewsBySource("abc-news", page = 1) } returns (getArticlesData())
        val response = repository.getNewsList("abc-news", currentPage = 1)
        assertEquals(ApiResponse.Success(getArticlesData().articles), response.last())
    }

    @Test
    fun testGetSourceResponseEmptyMessage() = runTest {
        coEvery { apiService.getSources("general") } returns (getSourcesEmptyData())
        val response = repository.getSourceList("general")
        assertEquals(
            ApiResponse.Error("No sources available now, please check after a while!"), response.last()
        )
    }

    @Test
    fun testGetNewsBySourceResponseEmptyMessage() = runTest {
        coEvery {
            apiService.getNewsBySource(
                source = "abc-news",
                page = 1
            )
        } returns (getNewsEmptyData())
        val response = repository.getNewsList(source = "abc-news", currentPage = 1)
        assertEquals(
            ApiResponse.Error("No news available now, please check after a while!"), response.last()
        )
    }

    @Test
    fun testGetSourceResponseErrorMessage() = runTest {
        coEvery { apiService.getSources(category = "general") } throws (NullPointerException())
        val response = repository.getSourceList(category = "general")
        assertEquals(ApiResponse.Error("Unexpected Error"), response.last())
    }

    @Test
    fun testGetNewsBySourceResponseErrorMessage() = runTest {
        coEvery {
            apiService.getNewsBySource(
                source = "abc-news", page = 1
            )
        } throws (NullPointerException())
        val response = repository.getNewsList(source = "abc-news", currentPage = 1)

        assertEquals(ApiResponse.Error("Unexpected Error"), response.last())
    }

    fun getSourcesSuccessData(): SourcesList {
        return SourcesList(
            "ok", sources = arrayListOf(
                Source(
                    id = "abc-news", name = "ABC News"
                )
            )
        )
    }

    fun getArticlesData(): NewsList {
        return NewsList(
            "ok", totalResults = 1, articles = arrayListOf(
                Article(
                    source = Article.Source("usa-today", "USA Today"),
                    author = "Andy",
                    title = "How Marjorie Taylor Greene took on Trump and stayed true to her base",
                    description = "People who know Marjorie Taylor Greene say she speaks for MAGA when Donald Trump is off course. Her critics don't trust her changed demeanor.",
                    url = "https://www.usatoday.com/story/news/politics/2025/11/21/majorie-taylor-greene-trump-republicans/86834873007/",
                    urlToImage = "https://www.usatoday.com/gcdn/authoring/authoring-images/2025/11/21/USAT/87402004007-mtg-topper.jpg?width=1960&height=1032&fit=crop&format=pjpg&auto=webp",
                    publishedAt = "2025-11-21T04:17:36Z",
                    content = "Marjorie Taylor Greene may be the freest woman in America. \\r\\nIn months of TV appearances, she’s taken on men in her own party and the president who inspired her to run for office. … [+1418 chars]"
                )
            )
        )
    }

    fun getSourcesEmptyData(): SourcesList {
        return SourcesList(
            "ok", sources = arrayListOf()
        )
    }

    fun getNewsEmptyData(): NewsList {
        return NewsList(
            "ok", totalResults = 0, articles = arrayListOf()
        )
    }
}