package com.aryandi.news.domain.usecase

import com.aryandi.domain.common.Result
import com.aryandi.domain.model.ArticleDomain
import com.aryandi.domain.repository.NewsRepository
import com.aryandi.domain.usecase.GetPaginatedNewsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetPaginatedNewsUseCaseTest {

    private lateinit var useCase: GetPaginatedNewsUseCase
    private val repository: NewsRepository = mockk()

    @Before
    fun setUp() {
        useCase = GetPaginatedNewsUseCase(repository)
    }

    @Test
    fun `invoke returns success from repository`() = runTest {
        // Given
        val articles = listOf(
            ArticleDomain(title = "Article 1"),
            ArticleDomain(title = "Article 2")
        )
        coEvery { repository.getNewsList("abc-news", 1) } returns flowOf(
            Result.Success(articles)
        )

        // When
        val result = useCase("abc-news", 1).toList()

        // Then
        assertTrue(result.last() is Result.Success)
        assertEquals(articles, (result.last() as Result.Success).data)
    }

    @Test
    fun `mergeNewsLists combines existing and new items correctly`() {
        // Given
        val existingItems = listOf(
            ArticleDomain(title = "Article 1"),
            ArticleDomain(title = "Article 2")
        )
        val newItems = listOf(
            ArticleDomain(title = "Article 3"),
            ArticleDomain(title = "Article 4")
        )

        // When
        val result = useCase.mergeNewsLists(existingItems, newItems)

        // Then
        assertEquals(4, result.size)
        assertEquals("Article 1", result[0].title)
        assertEquals("Article 2", result[1].title)
        assertEquals("Article 3", result[2].title)
        assertEquals("Article 4", result[3].title)
    }

    @Test
    fun `mergeNewsLists with empty existing list returns new items`() {
        // Given
        val existingItems = emptyList<ArticleDomain>()
        val newItems = listOf(
            ArticleDomain(title = "Article 1"),
            ArticleDomain(title = "Article 2")
        )

        // When
        val result = useCase.mergeNewsLists(existingItems, newItems)

        // Then
        assertEquals(2, result.size)
        assertEquals(newItems, result)
    }

    @Test
    fun `mergeNewsLists with empty new list returns existing items`() {
        // Given
        val existingItems = listOf(
            ArticleDomain(title = "Article 1"),
            ArticleDomain(title = "Article 2")
        )
        val newItems = emptyList<ArticleDomain>()

        // When
        val result = useCase.mergeNewsLists(existingItems, newItems)

        // Then
        assertEquals(2, result.size)
        assertEquals(existingItems, result)
    }

    @Test
    fun `shouldStopPagination returns true for empty list`() {
        // When
        val result = useCase.shouldStopPagination(emptyList())

        // Then
        assertTrue(result)
    }

    @Test
    fun `shouldStopPagination returns false for non-empty list`() {
        // Given
        val items = listOf(
            ArticleDomain(title = "Article 1")
        )

        // When
        val result = useCase.shouldStopPagination(items)

        // Then
        assertFalse(result)
    }

    @Test
    fun `invoke calls repository with correct parameters`() = runTest {
        // Given
        val source = "techcrunch"
        val page = 3
        coEvery { repository.getNewsList(source, page) } returns flowOf(
            Result.Success(emptyList())
        )

        // When
        useCase(source, page).toList()

        // Then
        coVerify(exactly = 1) { repository.getNewsList(source, page) }
    }

    @Test
    fun `mergeNewsLists maintains order`() {
        // Given
        val existingItems = listOf(
            ArticleDomain(title = "A"),
            ArticleDomain(title = "B"),
            ArticleDomain(title = "C")
        )
        val newItems = listOf(
            ArticleDomain(title = "D"),
            ArticleDomain(title = "E")
        )

        // When
        val result = useCase.mergeNewsLists(existingItems, newItems)

        // Then
        assertEquals(5, result.size)
        assertEquals(listOf("A", "B", "C", "D", "E"), result.map { it.title })
    }

    @Test
    fun `mergeNewsLists handles large lists`() {
        // Given
        val existingItems = (1..100).map { ArticleDomain(title = "Existing $it") }
        val newItems = (1..50).map { ArticleDomain(title = "New $it") }

        // When
        val result = useCase.mergeNewsLists(existingItems, newItems)

        // Then
        assertEquals(150, result.size)
        assertEquals("Existing 1", result.first().title)
        assertEquals("New 50", result.last().title)
    }
}
