package com.aryandi.news.data.repository

import com.aryandi.data.model.Article
import com.aryandi.data.model.NewsList
import com.aryandi.data.model.Source
import com.aryandi.data.model.SourcesList
import com.aryandi.data.network.ApiService
import com.aryandi.data.repository.NewsRepositoryImpl
import com.aryandi.domain.common.Result
import com.aryandi.domain.repository.NewsRepository
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class NewsRepositoryImplTest {

    private lateinit var repository: NewsRepository
    private val apiService: ApiService = mockk()

    @Before
    fun initialSetUp() {
        repository = NewsRepositoryImpl(apiService)
    }

    @Test
    fun `test get source list returns success with data`() = runTest {
        // Given
        val sourcesData = getSourcesSuccessData()
        coEvery { apiService.getSources("general") } returns sourcesData

        // When
        val response = repository.getSourceList("general").last()

        // Then
        assertTrue(response is Result.Success)
        val successResponse = response as Result.Success
        assertEquals(1, successResponse.data.size)
        assertEquals("abc-news", successResponse.data[0].id)
        assertEquals("ABC News", successResponse.data[0].name)
    }

    @Test
    fun `test get news list returns success with data`() = runTest {
        // Given
        val articlesData = getArticlesData()
        coEvery { apiService.getNewsBySource("abc-news", page = 1) } returns articlesData

        // When
        val response = repository.getNewsList("abc-news", currentPage = 1).last()

        // Then
        assertTrue(response is Result.Success)
        val successResponse = response as Result.Success
        assertEquals(1, successResponse.data.size)
        assertEquals(
            "How Marjorie Taylor Greene took on Trump and stayed true to her base",
            successResponse.data[0].title
        )
        assertEquals("Andy", successResponse.data[0].author)
    }

    @Test
    fun `test get source list returns empty when no sources`() = runTest {
        // Given
        coEvery { apiService.getSources("general") } returns getSourcesEmptyData()

        // When
        val response = repository.getSourceList("general").last()

        // Then
        assertEquals(Result.Empty, response)
    }

    @Test
    fun `test get news list returns empty when no articles`() = runTest {
        // Given
        coEvery {
            apiService.getNewsBySource(
                source = "abc-news",
                page = 1
            )
        } returns getNewsEmptyData()

        // When
        val response = repository.getNewsList(source = "abc-news", currentPage = 1).last()

        // Then
        assertEquals(Result.Empty, response)
    }

    @Test
    fun `test get source list returns error on exception`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { apiService.getSources(category = "general") } throws Exception(errorMessage)

        // When
        val response = repository.getSourceList(category = "general").last()

        // Then
        assertTrue(response is Result.Error)
        assertEquals(errorMessage, (response as Result.Error).message)
    }

    @Test
    fun `test get news list returns error on exception`() = runTest {
        // Given
        val errorMessage = "Connection timeout"
        coEvery {
            apiService.getNewsBySource(
                source = "abc-news", page = 1
            )
        } throws Exception(errorMessage)

        // When
        val response = repository.getNewsList(source = "abc-news", currentPage = 1).last()

        // Then
        assertTrue(response is Result.Error)
        assertEquals(errorMessage, (response as Result.Error).message)
    }

    @Test
    fun `test get source list returns error with generic message on null pointer`() = runTest {
        // Given
        coEvery { apiService.getSources(category = "general") } throws NullPointerException()

        // When
        val response = repository.getSourceList(category = "general").last()

        // Then
        assertTrue(response is Result.Error)
        assertEquals("Unexpected Error", (response as Result.Error).message)
    }

    @Test
    fun `test get news list emits loading state first`() = runTest {
        // Given
        coEvery { apiService.getNewsBySource("abc-news", page = 1) } returns getArticlesData()

        // When
        val responses = mutableListOf<Result<*>>()
        repository.getNewsList("abc-news", 1).collect { responses.add(it) }

        // Then
        assertTrue(responses.size >= 2)
        assertTrue(responses[0] is Result.Loading)
        assertTrue(responses.last() is Result.Success)
    }

    @Test
    fun `test get source list with multiple sources maps correctly`() = runTest {
        // Given
        val sourcesData = SourcesList(
            "ok", sources = arrayListOf(
                Source(id = "source-1", name = "Source 1", description = "Description 1"),
                Source(id = "source-2", name = "Source 2", description = "Description 2"),
                Source(id = "source-3", name = "Source 3", description = "Description 3")
            )
        )
        coEvery { apiService.getSources("technology") } returns sourcesData

        // When
        val response = repository.getSourceList("technology").last()

        // Then
        assertTrue(response is Result.Success)
        val successResponse = response as Result.Success
        assertEquals(3, successResponse.data.size)
        assertEquals("source-1", successResponse.data[0].id)
        assertEquals("source-2", successResponse.data[1].id)
        assertEquals("source-3", successResponse.data[2].id)
    }

    private fun getSourcesSuccessData(): SourcesList {
        return SourcesList(
            "ok", sources = arrayListOf(
                Source(
                    id = "abc-news",
                    name = "ABC News",
                    description = "Your trusted source for breaking news",
                    url = "https://abcnews.go.com",
                    category = "general",
                    language = "en",
                    country = "us"
                )
            )
        )
    }

    private fun getArticlesData(): NewsList {
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
                    content = "Marjorie Taylor Greene may be the freest woman in America. \\r\\nIn months of TV appearances, she's taken on men in her own party and the president who inspired her to run for office. … [+1418 chars]"
                )
            )
        )
    }

    private fun getSourcesEmptyData(): SourcesList {
        return SourcesList(
            "ok", sources = arrayListOf()
        )
    }

    private fun getNewsEmptyData(): NewsList {
        return NewsList(
            "ok", totalResults = 0, articles = arrayListOf()
        )
    }
}
