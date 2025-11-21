package com.aryandi.news.data.repository

import com.aryandi.data.model.Articles
import com.aryandi.data.model.News
import com.aryandi.data.network.ApiResponse
import com.aryandi.data.network.ApiService
import com.aryandi.data.repository.NewsRepository
import com.aryandi.data.repository.NewsRepositoryImpl
import com.aryandi.news.BuildConfig
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
    fun testResponseSuccess() = runTest {
        coEvery { apiService.getNews(BuildConfig.TOKEN_KEY) } returns (getSuccessData())
        val response = repository.getNewsList()

        assertEquals(ApiResponse.Success(getSuccessData().articles), response.last())
    }

    @Test
    fun testEmptyResponseErrorMessage() = runTest {
        coEvery { apiService.getNews(BuildConfig.TOKEN_KEY) } returns (getEmptyData())
        val response = repository.getNewsList()

        assertEquals(ApiResponse.Error("No news available now, please check after a while!"), response.last())
    }

    @Test
    fun testNullResponseMessage() = runTest {
        coEvery { apiService.getNews(BuildConfig.TOKEN_KEY) } throws (NullPointerException())
        val response = repository.getNewsList()

        assertEquals(ApiResponse.Error("Unexpected Error"), response.last())
    }

    fun getSuccessData(): News {
        return News(
            "ok", totalResults = 1, articles = arrayListOf(
                Articles(
                    source = Articles.Source("usa-today", "USA Today"),
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

    fun getEmptyData(): News {
        return News(
            "ok",
            totalResults = 0,
            articles = arrayListOf()
        )
    }
}