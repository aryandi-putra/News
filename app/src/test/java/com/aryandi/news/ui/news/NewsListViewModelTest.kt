package com.aryandi.news.ui.news

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.aryandi.data.network.ApiResponse
import com.aryandi.domain.common.Result
import com.aryandi.domain.model.ArticleDomain
import com.aryandi.domain.usecase.GetPaginatedNewsUseCase
import com.aryandi.domain.usecase.SearchNewsUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewsListViewModelTest {

    private lateinit var viewModel: NewsListViewModel
    private val getPaginatedNewsUseCase: GetPaginatedNewsUseCase = mockk(relaxed = true)
    private val searchNewsUseCase: SearchNewsUseCase = mockk(relaxed = true)
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        // Default mock behavior
        every { getPaginatedNewsUseCase.shouldStopPagination(emptyList()) } returns true
        every { getPaginatedNewsUseCase.shouldStopPagination(match { it.isNotEmpty() }) } returns false
        every { getPaginatedNewsUseCase.mergeNewsLists(any(), any()) } answers {
            firstArg<List<ArticleDomain>>() + secondArg<List<ArticleDomain>>()
        }
        every { searchNewsUseCase(any(), any()) } answers { firstArg() }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test fetch news list success`() = runTest {
        // Given
        val domainArticles = getTestDomainArticles()

        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { getPaginatedNewsUseCase("abc-news", 1) } returns flowOf(
            Result.Success(
                domainArticles
            )
        )

        // When
        viewModel = NewsListViewModel(getPaginatedNewsUseCase, searchNewsUseCase, savedStateHandle)
        advanceUntilIdle() // Let all coroutines complete

        // Then
        viewModel.newsList.test {
            // Get current state (should be Success after advanceUntilIdle)
            val success = awaitItem()
            assertTrue("Expected Success", success is ApiResponse.Success)
            assertEquals(3, (success as ApiResponse.Success).data.size)

            cancelAndIgnoreRemainingEvents()
        }

        // Test filtered list
        viewModel.filteredNewsList.test {
            // Get current state (should be Success after advanceUntilIdle)
            val success = awaitItem()
            assertTrue(success is ApiResponse.Success)
            assertEquals(3, (success as ApiResponse.Success).data.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test search filter by title`() = runTest {
        // Given
        val domainArticles = getTestDomainArticles()
        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { getPaginatedNewsUseCase("abc-news", 1) } returns flowOf(
            Result.Success(
                domainArticles
            )
        )
        every { searchNewsUseCase(any(), "") } answers { firstArg() }
        every { searchNewsUseCase(any(), "Trump") } returns listOf(domainArticles[0])

        viewModel = NewsListViewModel(getPaginatedNewsUseCase, searchNewsUseCase, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.filteredNewsList.test {
            // Current state before update
            val beforeUpdate = awaitItem()
            assertTrue(beforeUpdate is ApiResponse.Success)
            assertEquals(3, (beforeUpdate as ApiResponse.Success).data.size)

            // Update search keyword
            viewModel.updateSearchKeyword("Trump")

            // Get filtered result
            val afterUpdate = awaitItem()
            assertTrue(afterUpdate is ApiResponse.Success)
            val filtered = (afterUpdate as ApiResponse.Success).data
            assertEquals(1, filtered.size)
            assertEquals("Trump news article", filtered[0].title)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test search keyword state updates`() = runTest {
        // Given
        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery {
            getPaginatedNewsUseCase(
                "abc-news",
                1
            )
        } returns flowOf(Result.Success(emptyList()))
        every { getPaginatedNewsUseCase.shouldStopPagination(emptyList()) } returns true

        viewModel = NewsListViewModel(getPaginatedNewsUseCase, searchNewsUseCase, savedStateHandle)
        advanceUntilIdle()

        // When & Then - Test search keyword updates
        viewModel.searchKeyword.test {
            // Initial state
            assertEquals("", awaitItem())

            // Update keyword
            viewModel.updateSearchKeyword("test keyword")
            assertEquals("test keyword", awaitItem())

            // Clear keyword
            viewModel.updateSearchKeyword("")
            assertEquals("", awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test pagination error state`() = runTest {
        // Given
        val firstPageDomain = listOf(ArticleDomain(title = "Article 1"))
        val errorMessage = "Failed to load page 2"

        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { getPaginatedNewsUseCase("abc-news", 1) } returns flowOf(
            Result.Success(
                firstPageDomain
            )
        )
        coEvery {
            getPaginatedNewsUseCase(
                "abc-news",
                2
            )
        } returns flowOf(Result.Error(errorMessage))

        viewModel = NewsListViewModel(getPaginatedNewsUseCase, searchNewsUseCase, savedStateHandle)
        advanceUntilIdle()

        // When & Then - Test pagination error
        viewModel.paginationError.test {
            // Initial state (no error)
            assertEquals(null, awaitItem())

            // Trigger load more
            viewModel.loadMoreSources()
            advanceUntilIdle()

            // Error should be emitted
            assertEquals(errorMessage, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test dismiss pagination error`() = runTest {
        // Given
        val firstPageDomain = listOf(ArticleDomain(title = "Article 1"))
        val errorMessage = "Failed to load page 2"

        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { getPaginatedNewsUseCase("abc-news", 1) } returns flowOf(
            Result.Success(
                firstPageDomain
            )
        )
        coEvery {
            getPaginatedNewsUseCase(
                "abc-news",
                2
            )
        } returns flowOf(Result.Error(errorMessage))

        viewModel = NewsListViewModel(getPaginatedNewsUseCase, searchNewsUseCase, savedStateHandle)
        advanceUntilIdle()

        viewModel.loadMoreSources()
        advanceUntilIdle()

        // When & Then - Test dismissing error
        viewModel.paginationError.test {
            // Current error state
            assertEquals(errorMessage, awaitItem())

            // Dismiss error
            viewModel.dismissPaginationError()

            // Error should be cleared
            assertEquals(null, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test empty state`() = runTest {
        // Given
        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { getPaginatedNewsUseCase("abc-news", 1) } returns flowOf(Result.Empty)

        // When
        viewModel = NewsListViewModel(getPaginatedNewsUseCase, searchNewsUseCase, savedStateHandle)
        advanceUntilIdle() // Let all coroutines complete

        // Then
        viewModel.newsList.test {
            // Get current state (should be Empty after advanceUntilIdle)
            val empty = awaitItem()
            assertEquals(ApiResponse.Empty, empty)

            cancelAndIgnoreRemainingEvents()
        }

        viewModel.filteredNewsList.test {
            // Get current state (should be Empty after advanceUntilIdle)
            val empty = awaitItem()
            assertEquals(ApiResponse.Empty, empty)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test error state`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery {
            getPaginatedNewsUseCase(
                "abc-news",
                1
            )
        } returns flowOf(Result.Error(errorMessage))

        // When
        viewModel = NewsListViewModel(getPaginatedNewsUseCase, searchNewsUseCase, savedStateHandle)

        // Then
        viewModel.newsList.test {
            val loading = awaitItem()
            assertTrue(loading is ApiResponse.Loading)

            val error = awaitItem()
            assertTrue(error is ApiResponse.Error)
            assertEquals(errorMessage, (error as ApiResponse.Error).message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test pagination appends articles`() = runTest {
        // Given
        val firstPageDomain = listOf(ArticleDomain(title = "Article 1"))
        val secondPageDomain = listOf(ArticleDomain(title = "Article 2"))

        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { getPaginatedNewsUseCase("abc-news", 1) } returns flowOf(
            Result.Success(
                firstPageDomain
            )
        )
        coEvery { getPaginatedNewsUseCase("abc-news", 2) } returns flowOf(
            Result.Success(
                secondPageDomain
            )
        )

        viewModel = NewsListViewModel(getPaginatedNewsUseCase, searchNewsUseCase, savedStateHandle)
        advanceUntilIdle()

        // When & Then - Test pagination
        viewModel.newsList.test {
            // Current state (page 1)
            val firstPage = awaitItem()
            assertTrue(firstPage is ApiResponse.Success)
            assertEquals(1, (firstPage as ApiResponse.Success).data.size)

            // Trigger load more
            viewModel.loadMoreSources()
            advanceUntilIdle()

            // Get merged result (page 1 + page 2)
            val merged = awaitItem()
            assertTrue(merged is ApiResponse.Success)
            val allArticles = (merged as ApiResponse.Success).data
            assertEquals(2, allArticles.size)
            assertEquals("Article 1", allArticles[0].title)
            assertEquals("Article 2", allArticles[1].title)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test retry initial load`() = runTest {
        // Given
        val errorMessage = "Network error"
        val domainArticles = getTestDomainArticles()

        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { getPaginatedNewsUseCase("abc-news", 1) } returns
                flowOf(Result.Error(errorMessage)) andThen
                flowOf(Result.Success(domainArticles))

        viewModel = NewsListViewModel(getPaginatedNewsUseCase, searchNewsUseCase, savedStateHandle)
        advanceUntilIdle()

        // When & Then - Test retry
        viewModel.newsList.test {
            // Current error state
            val error = awaitItem()
            assertTrue(error is ApiResponse.Error)

            // Retry
            viewModel.retryInitialLoad()
            advanceUntilIdle()

            // Should go through loading
            val loading = awaitItem()
            assertTrue(loading is ApiResponse.Loading)

            // Then success
            val success = awaitItem()
            assertTrue(success is ApiResponse.Success)
            assertEquals(3, (success as ApiResponse.Success).data.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun getTestDomainArticles(): List<ArticleDomain> {
        return listOf(
            ArticleDomain(
                source = ArticleDomain.SourceDomain("abc-news", "ABC News"),
                author = "John Doe",
                title = "Trump news article",
                description = "Political news description",
                url = "https://example.com/article1",
                urlToImage = "https://example.com/image1.jpg",
                publishedAt = "2025-11-21T04:17:36Z",
                content = "Political content here"
            ),
            ArticleDomain(
                source = ArticleDomain.SourceDomain("tech-news", "Tech News"),
                author = "Jane Smith",
                title = "Tech article",
                description = "Latest technology updates",
                url = "https://example.com/article2",
                urlToImage = "https://example.com/image2.jpg",
                publishedAt = "2025-11-22T04:17:36Z",
                content = "Technology content here"
            ),
            ArticleDomain(
                source = ArticleDomain.SourceDomain("sports-news", "Sports News"),
                author = "Bob Johnson",
                title = "Sports article",
                description = "Sports update",
                url = "https://example.com/article3",
                urlToImage = "https://example.com/image3.jpg",
                publishedAt = "2025-11-23T04:17:36Z",
                content = "Latest sports content here"
            )
        )
    }
}
