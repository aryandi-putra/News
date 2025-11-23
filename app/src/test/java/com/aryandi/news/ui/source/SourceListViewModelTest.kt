package com.aryandi.news.ui.source

import androidx.lifecycle.SavedStateHandle
import com.aryandi.data.model.Source
import com.aryandi.data.network.ApiResponse
import com.aryandi.data.repository.NewsRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
class SourceListViewModelTest {

    private lateinit var viewModel: SourceListViewModel
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
    fun `test initial state is loading when category is provided`() = runTest {
        // Given
        coEvery { savedStateHandle.get<String>(EXTRA_CATEGORY_KEY) } returns "technology"
        coEvery { newsRepository.getSourceList("technology") } returns flowOf(
            ApiResponse.Loading
        )

        // When
        viewModel = SourceListViewModel(newsRepository, savedStateHandle)

        // Then
        assertEquals(ApiResponse.Loading, viewModel.filteredSourceList.value)
    }

    @Test
    fun `test fetch source list success`() = runTest {
        // Given
        val sources = getTestSources()
        coEvery { savedStateHandle.get<String>(EXTRA_CATEGORY_KEY) } returns "technology"
        coEvery { newsRepository.getSourceList("technology") } returns flowOf(
            ApiResponse.Success(sources)
        )

        // When
        viewModel = SourceListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // Then
        val filteredValue = viewModel.filteredSourceList.value
        assertTrue(filteredValue is ApiResponse.Success)
        assertEquals(sources, (filteredValue as ApiResponse.Success).data)
    }

    @Test
    fun `test fetch source list error`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { savedStateHandle.get<String>(EXTRA_CATEGORY_KEY) } returns "technology"
        coEvery { newsRepository.getSourceList("technology") } returns flowOf(
            ApiResponse.Error(errorMessage)
        )

        // When
        viewModel = SourceListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // Then
        val filteredValue = viewModel.filteredSourceList.value
        assertTrue(filteredValue is ApiResponse.Error)
        assertEquals(errorMessage, (filteredValue as ApiResponse.Error).message)
    }

    @Test
    fun `test fetch source list empty state`() = runTest {
        // Given
        coEvery { savedStateHandle.get<String>(EXTRA_CATEGORY_KEY) } returns "technology"
        coEvery { newsRepository.getSourceList("technology") } returns flowOf(
            ApiResponse.Empty
        )

        // When
        viewModel = SourceListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // Then
        assertEquals(ApiResponse.Empty, viewModel.filteredSourceList.value)
    }

    @Test
    fun `test search filter by name`() = runTest {
        // Given
        val sources = getTestSources()
        coEvery { savedStateHandle.get<String>(EXTRA_CATEGORY_KEY) } returns "technology"
        coEvery { newsRepository.getSourceList("technology") } returns flowOf(
            ApiResponse.Success(sources)
        )

        viewModel = SourceListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.updateSearchKeyword("TechCrunch")
        advanceUntilIdle()

        // Then
        val filteredValue = viewModel.filteredSourceList.value
        assertTrue(filteredValue is ApiResponse.Success)
        val filteredSources = (filteredValue as ApiResponse.Success).data
        assertEquals(1, filteredSources.size)
        assertEquals("TechCrunch", filteredSources[0].name)
    }

    @Test
    fun `test search filter by description`() = runTest {
        // Given
        val sources = getTestSources()
        coEvery { savedStateHandle.get<String>(EXTRA_CATEGORY_KEY) } returns "technology"
        coEvery { newsRepository.getSourceList("technology") } returns flowOf(
            ApiResponse.Success(sources)
        )

        viewModel = SourceListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.updateSearchKeyword("coding")
        advanceUntilIdle()

        // Then
        val filteredValue = viewModel.filteredSourceList.value
        assertTrue(filteredValue is ApiResponse.Success)
        val filteredSources = (filteredValue as ApiResponse.Success).data
        assertEquals(1, filteredSources.size)
        assertEquals("The Verge", filteredSources[0].name)
    }

    @Test
    fun `test search filter by category`() = runTest {
        // Given
        val sources = getTestSources()
        coEvery { savedStateHandle.get<String>(EXTRA_CATEGORY_KEY) } returns "technology"
        coEvery { newsRepository.getSourceList("technology") } returns flowOf(
            ApiResponse.Success(sources)
        )

        viewModel = SourceListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.updateSearchKeyword("technology")
        advanceUntilIdle()

        // Then
        val filteredValue = viewModel.filteredSourceList.value
        assertTrue(filteredValue is ApiResponse.Success)
        val filteredSources = (filteredValue as ApiResponse.Success).data
        assertEquals(3, filteredSources.size)
    }

    @Test
    fun `test search filter case insensitive`() = runTest {
        // Given
        val sources = getTestSources()
        coEvery { savedStateHandle.get<String>(EXTRA_CATEGORY_KEY) } returns "technology"
        coEvery { newsRepository.getSourceList("technology") } returns flowOf(
            ApiResponse.Success(sources)
        )

        viewModel = SourceListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.updateSearchKeyword("TECHCRUNCH")
        advanceUntilIdle()

        // Then
        val filteredValue = viewModel.filteredSourceList.value
        assertTrue(filteredValue is ApiResponse.Success)
        val filteredSources = (filteredValue as ApiResponse.Success).data
        assertEquals(1, filteredSources.size)
        assertEquals("TechCrunch", filteredSources[0].name)
    }

    @Test
    fun `test search filter with empty keyword returns all sources`() = runTest {
        // Given
        val sources = getTestSources()
        coEvery { savedStateHandle.get<String>(EXTRA_CATEGORY_KEY) } returns "technology"
        coEvery { newsRepository.getSourceList("technology") } returns flowOf(
            ApiResponse.Success(sources)
        )

        viewModel = SourceListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        viewModel.updateSearchKeyword("TechCrunch")
        advanceUntilIdle()

        // When - clear search
        viewModel.updateSearchKeyword("")
        advanceUntilIdle()

        // Then
        val filteredValue = viewModel.filteredSourceList.value
        assertTrue(filteredValue is ApiResponse.Success)
        val filteredSources = (filteredValue as ApiResponse.Success).data
        assertEquals(sources.size, filteredSources.size)
    }

    @Test
    fun `test search filter with whitespace keyword returns all sources`() = runTest {
        // Given
        val sources = getTestSources()
        coEvery { savedStateHandle.get<String>(EXTRA_CATEGORY_KEY) } returns "technology"
        coEvery { newsRepository.getSourceList("technology") } returns flowOf(
            ApiResponse.Success(sources)
        )

        viewModel = SourceListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.updateSearchKeyword("   ")
        advanceUntilIdle()

        // Then
        val filteredValue = viewModel.filteredSourceList.value
        assertTrue(filteredValue is ApiResponse.Success)
        val filteredSources = (filteredValue as ApiResponse.Success).data
        assertEquals(sources.size, filteredSources.size)
    }

    @Test
    fun `test search filter with no matches returns empty list`() = runTest {
        // Given
        val sources = getTestSources()
        coEvery { savedStateHandle.get<String>(EXTRA_CATEGORY_KEY) } returns "technology"
        coEvery { newsRepository.getSourceList("technology") } returns flowOf(
            ApiResponse.Success(sources)
        )

        viewModel = SourceListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.updateSearchKeyword("NonExistentSource123")
        advanceUntilIdle()

        // Then
        val filteredValue = viewModel.filteredSourceList.value
        assertTrue(filteredValue is ApiResponse.Success)
        val filteredSources = (filteredValue as ApiResponse.Success).data
        assertTrue(filteredSources.isEmpty())
    }

    @Test
    fun `test search keyword state flow updates correctly`() = runTest {
        // Given
        coEvery { savedStateHandle.get<String>(EXTRA_CATEGORY_KEY) } returns "technology"
        coEvery { newsRepository.getSourceList("technology") } returns flowOf(
            ApiResponse.Success(emptyList())
        )

        viewModel = SourceListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.updateSearchKeyword("test keyword")

        // Then
        assertEquals("test keyword", viewModel.searchKeyword.value)
    }

    @Test
    fun `test no category in saved state does not fetch sources`() = runTest {
        // Given
        coEvery { savedStateHandle.get<String>(EXTRA_CATEGORY_KEY) } returns null

        // When
        viewModel = SourceListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { newsRepository.getSourceList(any()) }
    }

    @Test
    fun `test retry load fetches sources again`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { savedStateHandle.get<String>(EXTRA_CATEGORY_KEY) } returns "technology"
        coEvery { newsRepository.getSourceList("technology") } returns flowOf(
            ApiResponse.Error(errorMessage)
        )

        viewModel = SourceListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        val sources = getTestSources()
        coEvery { newsRepository.getSourceList("technology") } returns flowOf(
            ApiResponse.Success(sources)
        )
        viewModel.retryLoad()
        advanceUntilIdle()

        // Then
        val filteredValue = viewModel.filteredSourceList.value
        assertTrue(filteredValue is ApiResponse.Success)
        assertEquals(sources, (filteredValue as ApiResponse.Success).data)
    }

    @Test
    fun `test empty state is preserved through filtering`() = runTest {
        // Given
        coEvery { savedStateHandle.get<String>(EXTRA_CATEGORY_KEY) } returns "technology"
        coEvery { newsRepository.getSourceList("technology") } returns flowOf(
            ApiResponse.Empty
        )

        viewModel = SourceListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.updateSearchKeyword("test")
        advanceUntilIdle()

        // Then
        assertEquals(ApiResponse.Empty, viewModel.filteredSourceList.value)
    }

    @Test
    fun `test error state is preserved through filtering`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { savedStateHandle.get<String>(EXTRA_CATEGORY_KEY) } returns "technology"
        coEvery { newsRepository.getSourceList("technology") } returns flowOf(
            ApiResponse.Error(errorMessage)
        )

        viewModel = SourceListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.updateSearchKeyword("test")
        advanceUntilIdle()

        // Then
        val filteredValue = viewModel.filteredSourceList.value
        assertTrue(filteredValue is ApiResponse.Error)
        assertEquals(errorMessage, (filteredValue as ApiResponse.Error).message)
    }

    @Test
    fun `test filter works with multiple matching criteria`() = runTest {
        // Given
        val sources = getTestSources()
        coEvery { savedStateHandle.get<String>(EXTRA_CATEGORY_KEY) } returns "technology"
        coEvery { newsRepository.getSourceList("technology") } returns flowOf(
            ApiResponse.Success(sources)
        )

        viewModel = SourceListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // When - search for a term that appears in both name and description
        viewModel.updateSearchKeyword("tech")
        advanceUntilIdle()

        // Then
        val filteredValue = viewModel.filteredSourceList.value
        assertTrue(filteredValue is ApiResponse.Success)
        val filteredSources = (filteredValue as ApiResponse.Success).data
        // Should find TechCrunch (in name) and Wired (in description)
        assertTrue(filteredSources.size >= 2)
    }

    @Test
    fun `test multiple consecutive filter updates`() = runTest {
        // Given
        val sources = getTestSources()
        coEvery { savedStateHandle.get<String>(EXTRA_CATEGORY_KEY) } returns "technology"
        coEvery { newsRepository.getSourceList("technology") } returns flowOf(
            ApiResponse.Success(sources)
        )

        viewModel = SourceListViewModel(newsRepository, savedStateHandle)
        advanceUntilIdle()

        // When - apply multiple filters
        viewModel.updateSearchKeyword("TechCrunch")
        advanceUntilIdle()
        var filteredValue = viewModel.filteredSourceList.value
        assertTrue(filteredValue is ApiResponse.Success)
        assertEquals(1, (filteredValue as ApiResponse.Success).data.size)

        viewModel.updateSearchKeyword("Wired")
        advanceUntilIdle()
        filteredValue = viewModel.filteredSourceList.value
        assertTrue(filteredValue is ApiResponse.Success)
        assertEquals(1, (filteredValue as ApiResponse.Success).data.size)
        assertEquals("Wired", (filteredValue as ApiResponse.Success).data[0].name)

        viewModel.updateSearchKeyword("")
        advanceUntilIdle()
        filteredValue = viewModel.filteredSourceList.value
        assertTrue(filteredValue is ApiResponse.Success)
        assertEquals(sources.size, (filteredValue as ApiResponse.Success).data.size)
    }

    private fun getTestSources(): List<Source> {
        return listOf(
            Source(
                id = "techcrunch",
                name = "TechCrunch",
                description = "The latest technology news and information on startups",
                url = "https://techcrunch.com",
                category = "technology",
                language = "en",
                country = "us"
            ),
            Source(
                id = "wired",
                name = "Wired",
                description = "In-depth coverage of current and future trends in tech",
                url = "https://wired.com",
                category = "technology",
                language = "en",
                country = "us"
            ),
            Source(
                id = "the-verge",
                name = "The Verge",
                description = "The Verge covers the intersection of technology, science, art, and coding",
                url = "https://theverge.com",
                category = "technology",
                language = "en",
                country = "us"
            )
        )
    }
}
