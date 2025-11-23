package com.aryandi.news.domain.usecase

import com.aryandi.domain.common.Result
import com.aryandi.domain.model.ArticleDomain
import com.aryandi.domain.repository.NewsRepository
import com.aryandi.domain.usecase.GetNewsListUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetNewsListUseCaseTest {

    private lateinit var useCase: GetNewsListUseCase
    private val repository: NewsRepository = mockk()

    @Before
    fun setUp() {
        useCase = GetNewsListUseCase(repository)
    }

    @Test
    fun `invoke returns success with articles from repository`() = runTest {
        // Given
        val articles = listOf(
            ArticleDomain(
                title = "Test Article 1",
                description = "Description 1",
                author = "Author 1"
            ),
            ArticleDomain(
                title = "Test Article 2",
                description = "Description 2",
                author = "Author 2"
            )
        )
        coEvery { repository.getNewsList("abc-news", 1) } returns flowOf(
            Result.Success(articles)
        )

        // When
        val result = useCase("abc-news", 1).toList()

        // Then
        assertTrue(result.last() is Result.Success)
        assertEquals(articles, (result.last() as Result.Success).data)
        coVerify(exactly = 1) { repository.getNewsList("abc-news", 1) }
    }

    @Test
    fun `invoke returns error from repository`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { repository.getNewsList("abc-news", 1) } returns flowOf(
            Result.Error(errorMessage)
        )

        // When
        val result = useCase("abc-news", 1).toList()

        // Then
        assertTrue(result.last() is Result.Error)
        assertEquals(errorMessage, (result.last() as Result.Error).message)
    }

    @Test
    fun `invoke returns empty from repository`() = runTest {
        // Given
        coEvery { repository.getNewsList("abc-news", 1) } returns flowOf(
            Result.Empty
        )

        // When
        val result = useCase("abc-news", 1).toList()

        // Then
        assertEquals(Result.Empty, result.last())
    }

    @Test
    fun `invoke returns loading from repository`() = runTest {
        // Given
        coEvery { repository.getNewsList("abc-news", 1) } returns flowOf(
            Result.Loading
        )

        // When
        val result = useCase("abc-news", 1).toList()

        // Then
        assertEquals(Result.Loading, result.first())
    }

    @Test
    fun `invoke calls repository with correct parameters`() = runTest {
        // Given
        val source = "techcrunch"
        val page = 5
        coEvery { repository.getNewsList(source, page) } returns flowOf(
            Result.Success(emptyList())
        )

        // When
        useCase(source, page).toList()

        // Then
        coVerify(exactly = 1) { repository.getNewsList(source, page) }
    }
}
