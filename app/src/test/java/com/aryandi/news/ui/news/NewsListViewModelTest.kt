package com.aryandi.news.ui.news

import androidx.lifecycle.SavedStateHandle
import com.aryandi.data.model.Article
import com.aryandi.data.network.ApiResponse
import com.aryandi.data.repository.NewsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
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
    private val newsRepository: NewsRepository = mockk()
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial state is loading when source is provided`() = runTest {
        // Given
        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { newsRepository.getNewsList("abc-news", 1) } returns flowOf(
            ApiResponse.Loading
        )

        // When
        viewModel = NewsListViewModel(newsRepository, savedStateHandle)

        // Then
        assertEquals(ApiResponse.Loading, viewModel.newsList.value)
        assertEquals(ApiResponse.Loading, viewModel.filteredNewsList.value)
    }

    @Test
    fun `test fetch news list success`() = runTest {
        // Given
        val articles = getTestArticles()
        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { newsRepository.getNewsList("abc-news", 1) } returns flowOf(
            ApiResponse.Success(articles)
        )

        // When
        viewModel = NewsListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // Then
        val newsListValue = viewModel.newsList.value
        assertTrue(newsListValue is ApiResponse.Success)
        assertEquals(articles, (newsListValue as ApiResponse.Success).data)

        val filteredValue = viewModel.filteredNewsList.value
        assertTrue(filteredValue is ApiResponse.Success)
        assertEquals(articles, (filteredValue as ApiResponse.Success).data)
    }

    @Test
    fun `test fetch news list error`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { newsRepository.getNewsList("abc-news", 1) } returns flowOf(
            ApiResponse.Error(errorMessage)
        )

        // When
        viewModel = NewsListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // Then
        val newsListValue = viewModel.newsList.value
        assertTrue(newsListValue is ApiResponse.Error)
        assertEquals(errorMessage, (newsListValue as ApiResponse.Error).message)

        val filteredValue = viewModel.filteredNewsList.value
        assertTrue(filteredValue is ApiResponse.Error)
        assertEquals(errorMessage, (filteredValue as ApiResponse.Error).message)
    }

    @Test
    fun `test fetch news list empty state`() = runTest {
        // Given
        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { newsRepository.getNewsList("abc-news", 1) } returns flowOf(
            ApiResponse.Empty
        )

        // When
        viewModel = NewsListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // Then
        assertEquals(ApiResponse.Empty, viewModel.newsList.value)
        assertEquals(ApiResponse.Empty, viewModel.filteredNewsList.value)
    }

    @Test
    fun `test search filter by title`() = runTest {
        // Given
        val articles = getTestArticles()
        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { newsRepository.getNewsList("abc-news", 1) } returns flowOf(
            ApiResponse.Success(articles)
        )

        viewModel = NewsListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.updateSearchKeyword("Trump")
        advanceUntilIdle()

        // Then
        val filteredValue = viewModel.filteredNewsList.value
        assertTrue(filteredValue is ApiResponse.Success)
        val filteredArticles = (filteredValue as ApiResponse.Success).data
        assertEquals(1, filteredArticles.size)
        assertEquals("Trump news article", filteredArticles[0].title)
    }

    @Test
    fun `test search filter by description`() = runTest {
        // Given
        val articles = getTestArticles()
        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { newsRepository.getNewsList("abc-news", 1) } returns flowOf(
            ApiResponse.Success(articles)
        )

        viewModel = NewsListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.updateSearchKeyword("technology")
        advanceUntilIdle()

        // Then
        val filteredValue = viewModel.filteredNewsList.value
        assertTrue(filteredValue is ApiResponse.Success)
        val filteredArticles = (filteredValue as ApiResponse.Success).data
        assertEquals(1, filteredArticles.size)
        assertEquals("Tech article", filteredArticles[0].title)
    }

    @Test
    fun `test search filter by author`() = runTest {
        // Given
        val articles = getTestArticles()
        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { newsRepository.getNewsList("abc-news", 1) } returns flowOf(
            ApiResponse.Success(articles)
        )

        viewModel = NewsListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.updateSearchKeyword("John Doe")
        advanceUntilIdle()

        // Then
        val filteredValue = viewModel.filteredNewsList.value
        assertTrue(filteredValue is ApiResponse.Success)
        val filteredArticles = (filteredValue as ApiResponse.Success).data
        assertEquals(1, filteredArticles.size)
        assertEquals("John Doe", filteredArticles[0].author)
    }

    @Test
    fun `test search filter by content`() = runTest {
        // Given
        val articles = getTestArticles()
        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { newsRepository.getNewsList("abc-news", 1) } returns flowOf(
            ApiResponse.Success(articles)
        )

        viewModel = NewsListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.updateSearchKeyword("sports content")
        advanceUntilIdle()

        // Then
        val filteredValue = viewModel.filteredNewsList.value
        assertTrue(filteredValue is ApiResponse.Success)
        val filteredArticles = (filteredValue as ApiResponse.Success).data
        assertEquals(1, filteredArticles.size)
        assertEquals("Sports article", filteredArticles[0].title)
    }

    @Test
    fun `test search filter case insensitive`() = runTest {
        // Given
        val articles = getTestArticles()
        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { newsRepository.getNewsList("abc-news", 1) } returns flowOf(
            ApiResponse.Success(articles)
        )

        viewModel = NewsListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.updateSearchKeyword("TRUMP")
        advanceUntilIdle()

        // Then
        val filteredValue = viewModel.filteredNewsList.value
        assertTrue(filteredValue is ApiResponse.Success)
        val filteredArticles = (filteredValue as ApiResponse.Success).data
        assertEquals(1, filteredArticles.size)
        assertEquals("Trump news article", filteredArticles[0].title)
    }

    @Test
    fun `test search filter with empty keyword returns all articles`() = runTest {
        // Given
        val articles = getTestArticles()
        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { newsRepository.getNewsList("abc-news", 1) } returns flowOf(
            ApiResponse.Success(articles)
        )

        viewModel = NewsListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        viewModel.updateSearchKeyword("Trump")
        advanceUntilIdle()

        // When - clear search
        viewModel.updateSearchKeyword("")
        advanceUntilIdle()

        // Then
        val filteredValue = viewModel.filteredNewsList.value
        assertTrue(filteredValue is ApiResponse.Success)
        val filteredArticles = (filteredValue as ApiResponse.Success).data
        assertEquals(articles.size, filteredArticles.size)
    }

    @Test
    fun `test search filter with whitespace keyword returns all articles`() = runTest {
        // Given
        val articles = getTestArticles()
        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { newsRepository.getNewsList("abc-news", 1) } returns flowOf(
            ApiResponse.Success(articles)
        )

        viewModel = NewsListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.updateSearchKeyword("   ")
        advanceUntilIdle()

        // Then
        val filteredValue = viewModel.filteredNewsList.value
        assertTrue(filteredValue is ApiResponse.Success)
        val filteredArticles = (filteredValue as ApiResponse.Success).data
        assertEquals(articles.size, filteredArticles.size)
    }

    @Test
    fun `test search filter with no matches returns empty list`() = runTest {
        // Given
        val articles = getTestArticles()
        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { newsRepository.getNewsList("abc-news", 1) } returns flowOf(
            ApiResponse.Success(articles)
        )

        viewModel = NewsListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.updateSearchKeyword("NonExistentKeyword123")
        advanceUntilIdle()

        // Then
        val filteredValue = viewModel.filteredNewsList.value
        assertTrue(filteredValue is ApiResponse.Success)
        val filteredArticles = (filteredValue as ApiResponse.Success).data
        assertTrue(filteredArticles.isEmpty())
    }

    @Test
    fun `test search keyword state flow updates correctly`() = runTest {
        // Given
        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { newsRepository.getNewsList("abc-news", 1) } returns flowOf(
            ApiResponse.Success(emptyList())
        )

        viewModel = NewsListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.updateSearchKeyword("test keyword")

        // Then
        assertEquals("test keyword", viewModel.searchKeyword.value)
    }

    @Test
    fun `test load more sources calls repository with correct parameters`() = runTest {
        // Given
        val articles = getTestArticles()
        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { newsRepository.getNewsList("abc-news", 1) } returns flowOf(
            ApiResponse.Success(articles)
        )
        coEvery { newsRepository.getNewsList("abc-news", 2) } returns flowOf(
            ApiResponse.Success(getTestArticles().take(1))
        )

        viewModel = NewsListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.loadMoreSources()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { newsRepository.getNewsList("abc-news", 1) }
        coVerify(exactly = 1) { newsRepository.getNewsList("abc-news", 2) }
    }

    @Test
    fun `test pagination appends new articles to existing list`() = runTest {
        // Given
        val firstPageArticles = listOf(
            Article(
                title = "Article 1",
                description = "Description 1",
                author = "Author 1"
            )
        )
        val secondPageArticles = listOf(
            Article(
                title = "Article 2",
                description = "Description 2",
                author = "Author 2"
            )
        )

        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { newsRepository.getNewsList("abc-news", 1) } returns flowOf(
            ApiResponse.Success(firstPageArticles)
        )
        coEvery { newsRepository.getNewsList("abc-news", 2) } returns flowOf(
            ApiResponse.Success(secondPageArticles)
        )

        viewModel = NewsListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.loadMoreSources()
        advanceUntilIdle()

        // Then
        val newsListValue = viewModel.newsList.value
        assertTrue(newsListValue is ApiResponse.Success)
        val allArticles = (newsListValue as ApiResponse.Success).data
        assertEquals(2, allArticles.size)
        assertEquals("Article 1", allArticles[0].title)
        assertEquals("Article 2", allArticles[1].title)
    }

    @Test
    fun `test filter applies to paginated results`() = runTest {
        // Given
        val firstPageArticles = listOf(
            Article(title = "Trump news 1"),
            Article(title = "Sports news 1")
        )
        val secondPageArticles = listOf(
            Article(title = "Trump news 2"),
            Article(title = "Tech news 1")
        )

        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { newsRepository.getNewsList("abc-news", 1) } returns flowOf(
            ApiResponse.Success(firstPageArticles)
        )
        coEvery { newsRepository.getNewsList("abc-news", 2) } returns flowOf(
            ApiResponse.Success(secondPageArticles)
        )

        viewModel = NewsListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // Load more
        viewModel.loadMoreSources()
        advanceUntilIdle()

        // When - apply filter
        viewModel.updateSearchKeyword("Trump")
        advanceUntilIdle()

        // Then
        val filteredValue = viewModel.filteredNewsList.value
        assertTrue(filteredValue is ApiResponse.Success)
        val filteredArticles = (filteredValue as ApiResponse.Success).data
        assertEquals(2, filteredArticles.size)
        assertTrue(filteredArticles.all { it.title?.contains("Trump") == true })
    }

    @Test
    fun `test no source in saved state does not fetch news`() = runTest {
        // Given
        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns null

        // When
        viewModel = NewsListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { newsRepository.getNewsList(any(), any()) }
    }

    @Test
    fun `test retry initial load resets state and fetches again`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { newsRepository.getNewsList("abc-news", 1) } returns flowOf(
            ApiResponse.Error(errorMessage)
        )

        viewModel = NewsListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        val articles = getTestArticles()
        coEvery { newsRepository.getNewsList("abc-news", 1) } returns flowOf(
            ApiResponse.Success(articles)
        )
        viewModel.retryInitialLoad()
        advanceUntilIdle()

        // Then
        val newsListValue = viewModel.newsList.value
        assertTrue(newsListValue is ApiResponse.Success)
        assertEquals(articles, (newsListValue as ApiResponse.Success).data)
    }

    @Test
    fun `test pagination error is stored`() = runTest {
        // Given
        val firstPageArticles = listOf(Article(title = "Article 1"))
        val errorMessage = "Failed to load page 2"

        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { newsRepository.getNewsList("abc-news", 1) } returns flowOf(
            ApiResponse.Success(firstPageArticles)
        )
        coEvery { newsRepository.getNewsList("abc-news", 2) } returns flowOf(
            ApiResponse.Error(errorMessage)
        )

        viewModel = NewsListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.loadMoreSources()
        advanceUntilIdle()

        // Then
        assertEquals(errorMessage, viewModel.paginationError.value)
    }

    @Test
    fun `test retry pagination reloads failed page`() = runTest {
        // Given
        val firstPageArticles = listOf(Article(title = "Article 1"))
        val secondPageArticles = listOf(Article(title = "Article 2"))
        val errorMessage = "Failed to load page 2"

        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { newsRepository.getNewsList("abc-news", 1) } returns flowOf(
            ApiResponse.Success(firstPageArticles)
        )
        coEvery { newsRepository.getNewsList("abc-news", 2) } returns flowOf(
            ApiResponse.Error(errorMessage)
        )

        viewModel = NewsListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        viewModel.loadMoreSources()
        advanceUntilIdle()

        // When - retry with success
        coEvery { newsRepository.getNewsList("abc-news", 2) } returns flowOf(
            ApiResponse.Success(secondPageArticles)
        )
        viewModel.retryPagination()
        advanceUntilIdle()

        // Then
        assertNull(viewModel.paginationError.value)
        val newsListValue = viewModel.newsList.value
        assertTrue(newsListValue is ApiResponse.Success)
        val allArticles = (newsListValue as ApiResponse.Success).data
        assertEquals(2, allArticles.size)
    }

    @Test
    fun `test dismiss pagination error clears error state`() = runTest {
        // Given
        val firstPageArticles = listOf(Article(title = "Article 1"))
        val errorMessage = "Failed to load page 2"

        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { newsRepository.getNewsList("abc-news", 1) } returns flowOf(
            ApiResponse.Success(firstPageArticles)
        )
        coEvery { newsRepository.getNewsList("abc-news", 2) } returns flowOf(
            ApiResponse.Error(errorMessage)
        )

        viewModel = NewsListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        viewModel.loadMoreSources()
        advanceUntilIdle()

        // When
        viewModel.dismissPaginationError()

        // Then
        assertNull(viewModel.paginationError.value)
    }

    @Test
    fun `test empty state is preserved through filtering`() = runTest {
        // Given
        coEvery { savedStateHandle.get<String>(EXTRA_SOURCE_KEY) } returns "abc-news"
        coEvery { newsRepository.getNewsList("abc-news", 1) } returns flowOf(
            ApiResponse.Empty
        )

        viewModel = NewsListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.updateSearchKeyword("test")
        advanceUntilIdle()

        // Then
        assertEquals(ApiResponse.Empty, viewModel.filteredNewsList.value)
    }

    private fun getTestArticles(): List<Article> {
        return listOf(
            Article(
                source = Article.Source("abc-news", "ABC News"),
                author = "John Doe",
                title = "Trump news article",
                description = "Political news description",
                url = "https://example.com/article1",
                urlToImage = "https://example.com/image1.jpg",
                publishedAt = "2025-11-21T04:17:36Z",
                content = "Political content here"
            ),
            Article(
                source = Article.Source("tech-news", "Tech News"),
                author = "Jane Smith",
                title = "Tech article",
                description = "Latest technology updates",
                url = "https://example.com/article2",
                urlToImage = "https://example.com/image2.jpg",
                publishedAt = "2025-11-22T04:17:36Z",
                content = "Technology content here"
            ),
            Article(
                source = Article.Source("sports-news", "Sports News"),
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
