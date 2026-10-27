package com.m10.everystory.data.news.mapper

import com.m10.everystory.data.news.remote.dto.ArticleDto
import com.m10.everystory.data.news.remote.dto.ArticlesResponseDto
import com.m10.everystory.data.news.remote.dto.SourceDto
import com.m10.everystory.domain.model.Article
import com.m10.everystory.domain.model.ArticlePage
import com.m10.everystory.domain.model.ArticleSource
import com.m10.everystory.domain.model.NewsSource
import java.security.MessageDigest
import java.time.Instant
import java.time.format.DateTimeParseException

// Publishers can pull an article after the fact; NewsAPI keeps the slot but fills it with "[Removed]".
private const val REMOVED_PLACEHOLDER = "[Removed]"

// Content is cut at ~200 characters and ends with a marker like "… [+6057 chars]".
private val TRUNCATION_MARKER = Regex("""\s*\[\+\d+ chars]$""")

private const val KEY_BYTES = 8

fun ArticlesResponseDto.toArticlePage(page: Int, pageSize: Int, maxResults: Int): ArticlePage {
    val reachable = minOf(totalResults, maxResults)
    return ArticlePage(
        articles = articles.mapNotNull { it.toDomain() },
        page = page,
        totalResults = totalResults,
        isLastPage = articles.isEmpty() || page * pageSize >= reachable,
    )
}

fun ArticleDto.toDomain(): Article? {
    val url = url?.takeIf { it.isNotBlank() } ?: return null
    val title = title?.takeIf { it.isNotBlank() && it != REMOVED_PLACEHOLDER } ?: return null
    val sourceName = source.name?.takeIf { it.isNotBlank() } ?: return null
    val publishedAt = publishedAt?.toInstantOrNull() ?: return null

    return Article(
        key = articleKey(url),
        title = title.withoutSourceSuffix(sourceName),
        description = description?.trim()?.takeIf { it.isNotEmpty() && it != REMOVED_PLACEHOLDER },
        content = content?.withoutTruncationMarker()?.takeIf { it.isNotEmpty() },
        url = url,
        imageUrl = urlToImage?.takeIf { it.startsWith("http") },
        source = ArticleSource(id = source.id, name = sourceName),
        author = author?.trim()?.takeIf { it.isNotEmpty() },
        publishedAt = publishedAt,
    )
}

fun SourceDto.toDomain() = NewsSource(
    id = id,
    name = name,
    description = description,
    url = url,
    category = category,
    language = language,
    country = country,
)

// NewsAPI articles have no id, so the URL is the only stable thing to key on.
fun articleKey(url: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(url.toByteArray())
        .take(KEY_BYTES)
        .joinToString("") { "%02x".format(it) }

// Titles arrive as "Headline - Source"; the source is shown on its own line, so drop the suffix.
internal fun String.withoutSourceSuffix(sourceName: String): String =
    removeSuffix(" - $sourceName").trim()

internal fun String.withoutTruncationMarker(): String =
    replace(TRUNCATION_MARKER, "").trim()

private fun String.toInstantOrNull(): Instant? =
    try {
        Instant.parse(this)
    } catch (_: DateTimeParseException) {
        null
    }
