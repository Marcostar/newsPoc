package com.m10.everystory.domain.repository

import com.m10.everystory.core.common.AppResult
import com.m10.everystory.domain.model.Article
import com.m10.everystory.domain.model.ArticlePage
import com.m10.everystory.domain.model.CachedHeadlines
import com.m10.everystory.domain.model.NewsCategory
import com.m10.everystory.domain.model.NewsLanguage
import com.m10.everystory.domain.model.NewsSource

// NewsAPI's developer plan never returns more than this many results for one query.
const val MAX_RESULTS = 100

interface NewsRepository {

    suspend fun topHeadlines(
        page: Int,
        category: NewsCategory? = null,
        language: NewsLanguage = NewsLanguage.Default,
    ): AppResult<ArticlePage>

    // The first page from the last successful load of this feed, or null if it never loaded.
    suspend fun cachedHeadlines(
        category: NewsCategory? = null,
        language: NewsLanguage = NewsLanguage.Default,
    ): CachedHeadlines?

    suspend fun searchArticles(
        query: String,
        page: Int,
        language: NewsLanguage = NewsLanguage.Default,
    ): AppResult<ArticlePage>

    suspend fun sources(
        category: NewsCategory? = null,
        language: NewsLanguage = NewsLanguage.Default,
    ): AppResult<List<NewsSource>>

    // NewsAPI can't fetch one article by id, so this reads the copy cached when a feed or search loaded it.
    suspend fun article(key: String): Article?
}
