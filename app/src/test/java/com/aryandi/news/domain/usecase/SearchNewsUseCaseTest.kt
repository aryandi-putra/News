package com.aryandi.news.domain.usecase

import com.aryandi.domain.model.ArticleDomain
import com.aryandi.domain.usecase.SearchNewsUseCase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class SearchNewsUseCaseTest {

    private lateinit var useCase: SearchNewsUseCase

    @Before
    fun setUp() {
        useCase = SearchNewsUseCase()
    }

    @Test
    fun `invoke with empty keyword returns all articles`() {
        // Given
        val articles = getTestArticles()

        // When
        val result = useCase(articles, "")

        // Then
        assertEquals(articles.size, result.size)
        assertEquals(articles, result)
    }

    @Test
    fun `invoke with whitespace keyword returns all articles`() {
        // Given
        val articles = getTestArticles()

        // When
        val result = useCase(articles, "   ")

        // Then
        assertEquals(articles.size, result.size)
    }

    @Test
    fun `invoke filters by title case insensitive`() {
        // Given
        val articles = getTestArticles()

        // When
        val result = useCase(articles, "TRUMP")

        // Then
        assertEquals(1, result.size)
        assertEquals("Trump news article", result[0].title)
    }

    @Test
    fun `invoke filters by description`() {
        // Given
        val articles = getTestArticles()

        // When
        val result = useCase(articles, "technology updates")

        // Then
        assertEquals(1, result.size)
        assertEquals("Tech article", result[0].title)
    }

    @Test
    fun `invoke filters by author`() {
        // Given
        val articles = getTestArticles()

        // When
        val result = useCase(articles, "John Doe")

        // Then
        assertEquals(1, result.size)
        assertEquals("John Doe", result[0].author)
    }

    @Test
    fun `invoke filters by content`() {
        // Given
        val articles = getTestArticles()

        // When
        val result = useCase(articles, "sports content")

        // Then
        assertEquals(1, result.size)
        assertEquals("Sports article", result[0].title)
    }

    @Test
    fun `invoke returns empty list when no matches`() {
        // Given
        val articles = getTestArticles()

        // When
        val result = useCase(articles, "NonExistentKeyword123")

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke returns multiple matches across different fields`() {
        // Given
        val articles = getTestArticles()

        // When
        val result = useCase(articles, "article")

        // Then
        assertEquals(3, result.size)
    }

    @Test
    fun `invoke with partial keyword match returns results`() {
        // Given
        val articles = getTestArticles()

        // When
        val result = useCase(articles, "Trum")

        // Then
        assertEquals(1, result.size)
        assertEquals("Trump news article", result[0].title)
    }

    @Test
    fun `invoke filters correctly with special characters`() {
        // Given
        val articles = listOf(
            ArticleDomain(
                title = "Article with special chars: @#\$%",
                description = "Description",
                author = "Author"
            )
        )

        // When
        val result = useCase(articles, "@#")

        // Then
        assertEquals(1, result.size)
    }

    @Test
    fun `invoke handles null values in articles`() {
        // Given
        val articles = listOf(
            ArticleDomain(
                title = null,
                description = null,
                author = null,
                content = null
            ),
            ArticleDomain(
                title = "Valid article",
                description = "Valid description",
                author = "Valid author",
                content = "Valid content"
            )
        )

        // When
        val result = useCase(articles, "Valid")

        // Then
        assertEquals(1, result.size)
        assertEquals("Valid article", result[0].title)
    }

    private fun getTestArticles(): List<ArticleDomain> {
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
