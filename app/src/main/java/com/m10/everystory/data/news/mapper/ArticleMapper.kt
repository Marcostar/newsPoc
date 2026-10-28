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

// Some feeds (Google News) send their summary as HTML, like "<ol><li><a href=…>Headline</a>…".
private val HTML_TAG = Regex("<[^>]+>")
private val WHITESPACE = Regex("""\s+""")

// &amp; goes last so "&amp;lt;" ends up as the text "&lt;", not "<".
private val HTML_ENTITIES = listOf(
    "&nbsp;" to " ",
    "&quot;" to "\"",
    "&#39;" to "'",
    "&lt;" to "<",
    "&gt;" to ">",
    "&amp;" to "&",
)

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
    val title = title?.withoutHtml()?.takeIf { it.isNotEmpty() && it != REMOVED_PLACEHOLDER } ?: return null
    val sourceName = source.name?.takeIf { it.isNotBlank() } ?: return null
    // Aggregators sometimes fill a slot with just their own name ("Google News"), not a headline.
    if (sourceName.startsWith(title, ignoreCase = true)) return null
    val publishedAt = publishedAt?.toInstantOrNull() ?: return null

    return Article(
        key = articleKey(url),
        title = title.withoutSourceSuffix(sourceName),
        description = description?.withoutHtml()?.takeIf { it.isNotEmpty() && it != REMOVED_PLACEHOLDER },
        content = content?.withoutHtml()?.withoutTruncationMarker()?.takeIf { it.isNotEmpty() },
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

// Titles arrive as "Headline - Source" (sometimes "- Source.com"); the source has its own line, so
// drop that tail. A tail that isn't the source, like "Levee repairs - phase two", is kept.
internal fun String.withoutSourceSuffix(sourceName: String): String {
    val separator = lastIndexOf(" - ")
    if (separator <= 0) return trim()
    val tail = substring(separator + 3).trim()
    val isSource = tail.startsWith(sourceName, ignoreCase = true) ||
        sourceName.startsWith(tail.substringBefore('.'), ignoreCase = true)
    return if (isSource) substring(0, separator).trim() else trim()
}

internal fun String.withoutHtml(): String {
    var text = replace(HTML_TAG, " ")
    HTML_ENTITIES.forEach { (entity, character) -> text = text.replace(entity, character) }
    return text.replace(WHITESPACE, " ").trim()
}

internal fun String.withoutTruncationMarker(): String =
    replace(TRUNCATION_MARKER, "").trim()

private fun String.toInstantOrNull(): Instant? =
    try {
        Instant.parse(this)
    } catch (_: DateTimeParseException) {
        null
    }
