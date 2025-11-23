package com.aryandi.news.domain.usecase

import com.aryandi.domain.model.SourceDomain
import com.aryandi.domain.usecase.SearchSourcesUseCase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class SearchSourcesUseCaseTest {

    private lateinit var useCase: SearchSourcesUseCase

    @Before
    fun setUp() {
        useCase = SearchSourcesUseCase()
    }

    @Test
    fun `invoke with empty keyword returns all sources`() {
        // Given
        val sources = getTestSources()

        // When
        val result = useCase(sources, "")

        // Then
        assertEquals(sources.size, result.size)
        assertEquals(sources, result)
    }

    @Test
    fun `invoke with whitespace keyword returns all sources`() {
        // Given
        val sources = getTestSources()

        // When
        val result = useCase(sources, "   ")

        // Then
        assertEquals(sources.size, result.size)
    }

    @Test
    fun `invoke filters by name case insensitive`() {
        // Given
        val sources = getTestSources()

        // When
        val result = useCase(sources, "TECHCRUNCH")

        // Then
        assertEquals(1, result.size)
        assertEquals("TechCrunch", result[0].name)
    }

    @Test
    fun `invoke filters by description`() {
        // Given
        val sources = getTestSources()

        // When
        val result = useCase(sources, "coding")

        // Then
        assertEquals(1, result.size)
        assertEquals("The Verge", result[0].name)
    }

    @Test
    fun `invoke filters by category`() {
        // Given
        val sources = getTestSources()

        // When
        val result = useCase(sources, "technology")

        // Then
        assertEquals(3, result.size)
    }

    @Test
    fun `invoke returns empty list when no matches`() {
        // Given
        val sources = getTestSources()

        // When
        val result = useCase(sources, "NonExistentSource123")

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke returns multiple matches across different fields`() {
        // Given
        val sources = getTestSources()

        // When
        val result = useCase(sources, "tech")

        // Then
        assertTrue(result.size >= 2)
    }

    @Test
    fun `invoke with partial keyword match returns results`() {
        // Given
        val sources = getTestSources()

        // When
        val result = useCase(sources, "Wire")

        // Then
        assertEquals(1, result.size)
        assertEquals("Wired", result[0].name)
    }

    @Test
    fun `invoke handles null values in sources`() {
        // Given
        val sources = listOf(
            SourceDomain(
                id = "source-1",
                name = null,
                description = null,
                category = null
            ),
            SourceDomain(
                id = "source-2",
                name = "Valid Source",
                description = "Valid description",
                category = "technology"
            )
        )

        // When
        val result = useCase(sources, "Valid")

        // Then
        assertEquals(1, result.size)
        assertEquals("Valid Source", result[0].name)
    }

    @Test
    fun `invoke filters with mixed case keyword`() {
        // Given
        val sources = getTestSources()

        // When
        val result = useCase(sources, "TeCh")

        // Then
        assertTrue(result.size >= 2)
    }

    private fun getTestSources(): List<SourceDomain> {
        return listOf(
            SourceDomain(
                id = "techcrunch",
                name = "TechCrunch",
                description = "The latest technology news and information on startups",
                url = "https://techcrunch.com",
                category = "technology",
                language = "en",
                country = "us"
            ),
            SourceDomain(
                id = "wired",
                name = "Wired",
                description = "In-depth coverage of current and future trends in tech",
                url = "https://wired.com",
                category = "technology",
                language = "en",
                country = "us"
            ),
            SourceDomain(
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
