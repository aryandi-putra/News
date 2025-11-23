package com.aryandi.news.domain.usecase

import com.aryandi.domain.common.Result
import com.aryandi.domain.model.SourceDomain
import com.aryandi.domain.repository.NewsRepository
import com.aryandi.domain.usecase.GetSourceListUseCase
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

class GetSourceListUseCaseTest {

    private lateinit var useCase: GetSourceListUseCase
    private val repository: NewsRepository = mockk()

    @Before
    fun setUp() {
        useCase = GetSourceListUseCase(repository)
    }

    @Test
    fun `invoke returns success with sources from repository`() = runTest {
        // Given
        val sources = listOf(
            SourceDomain(
                id = "techcrunch",
                name = "TechCrunch",
                description = "Tech news"
            ),
            SourceDomain(
                id = "wired",
                name = "Wired",
                description = "Technology coverage"
            )
        )
        coEvery { repository.getSourceList("technology") } returns flowOf(
            Result.Success(sources)
        )

        // When
        val result = useCase("technology").toList()

        // Then
        assertTrue(result.last() is Result.Success)
        assertEquals(sources, (result.last() as Result.Success).data)
        coVerify(exactly = 1) { repository.getSourceList("technology") }
    }

    @Test
    fun `invoke returns error from repository`() = runTest {
        // Given
        val errorMessage = "Failed to fetch sources"
        coEvery { repository.getSourceList("technology") } returns flowOf(
            Result.Error(errorMessage)
        )

        // When
        val result = useCase("technology").toList()

        // Then
        assertTrue(result.last() is Result.Error)
        assertEquals(errorMessage, (result.last() as Result.Error).message)
    }

    @Test
    fun `invoke returns empty from repository`() = runTest {
        // Given
        coEvery { repository.getSourceList("technology") } returns flowOf(
            Result.Empty
        )

        // When
        val result = useCase("technology").toList()

        // Then
        assertEquals(Result.Empty, result.last())
    }

    @Test
    fun `invoke calls repository with correct category`() = runTest {
        // Given
        val category = "sports"
        coEvery { repository.getSourceList(category) } returns flowOf(
            Result.Success(emptyList())
        )

        // When
        useCase(category).toList()

        // Then
        coVerify(exactly = 1) { repository.getSourceList(category) }
    }
}
