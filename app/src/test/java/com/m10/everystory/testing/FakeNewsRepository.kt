package com.m10.everystory.testing

import com.m10.everystory.core.common.AppResult
import com.m10.everystory.domain.model.Article
import com.m10.everystory.domain.model.ArticlePage
import com.m10.everystory.domain.model.ArticleSource
import com.m10.everystory.domain.model.NewsCategory
import com.m10.everystory.domain.model.NewsSource
import com.m10.everystory.domain.repository.NewsRepository
import java.time.Instant

class FakeNewsRepository : NewsRepository {

    val headlineRequests = mutableListOf<Pair<Int, NewsCategory?>>()

    // Queue answers in order; each call to topHeadlines takes the next one.
    val headlineResults = ArrayDeque<AppResult<ArticlePage>>()

    override suspend fun topHeadlines(page: Int, category: NewsCategory?, language: String): AppResult<ArticlePage> {
        headlineRequests += page to category
        return headlineResults.removeFirst()
    }

    override suspend fun searchArticles(query: String, page: Int, language: String): AppResult<ArticlePage> =
        error("Not used in this test")

    override suspend fun sources(category: NewsCategory?, language: String): AppResult<List<NewsSource>> =
        error("Not used in this test")
}

fun testArticle(key: String) = Article(
    key = key,
    title = "Headline $key",
    description = null,
    content = null,
    url = "https://example.com/$key",
    imageUrl = null,
    source = ArticleSource(id = null, name = "Northfield Post"),
    author = null,
    publishedAt = Instant.parse("2026-10-26T09:00:00Z"),
)

fun testPage(page: Int, keys: List<String>, isLastPage: Boolean = false): AppResult<ArticlePage> =
    AppResult.Success(
        ArticlePage(articles = keys.map(::testArticle), page = page, totalResults = 100, isLastPage = isLastPage),
    )
