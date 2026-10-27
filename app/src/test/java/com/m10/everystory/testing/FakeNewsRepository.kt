package com.m10.everystory.testing

import com.m10.everystory.core.common.AppResult
import com.m10.everystory.domain.model.Article
import com.m10.everystory.domain.model.ArticlePage
import com.m10.everystory.domain.model.ArticleSource
import com.m10.everystory.domain.model.NewsCategory
import com.m10.everystory.domain.model.NewsLanguage
import com.m10.everystory.domain.model.NewsSource
import com.m10.everystory.domain.repository.NewsRepository
import java.time.Instant

class FakeNewsRepository : NewsRepository {

    val headlineRequests = mutableListOf<Pair<Int, NewsCategory?>>()

    // Queue answers in order; each call to topHeadlines takes the next one.
    val headlineResults = ArrayDeque<AppResult<ArticlePage>>()

    val headlineLanguages = mutableListOf<NewsLanguage>()

    override suspend fun topHeadlines(page: Int, category: NewsCategory?, language: NewsLanguage): AppResult<ArticlePage> {
        headlineRequests += page to category
        headlineLanguages += language
        return headlineResults.removeFirst()
    }

    val searchRequests = mutableListOf<Pair<String, Int>>()
    val searchResults = ArrayDeque<AppResult<ArticlePage>>()

    val searchLanguages = mutableListOf<NewsLanguage>()

    override suspend fun searchArticles(query: String, page: Int, language: NewsLanguage): AppResult<ArticlePage> {
        searchRequests += query to page
        searchLanguages += language
        return searchResults.removeFirst()
    }

    override suspend fun sources(category: NewsCategory?, language: NewsLanguage): AppResult<List<NewsSource>> =
        error("Not used in this test")

    val cachedArticles = mutableMapOf<String, Article>()

    override suspend fun article(key: String): Article? = cachedArticles[key]
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
