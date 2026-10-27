package com.m10.everystory.domain.repository

import com.m10.everystory.core.common.AppResult
import com.m10.everystory.domain.model.Article
import com.m10.everystory.domain.model.ArticlePage
import com.m10.everystory.domain.model.NewsCategory
import com.m10.everystory.domain.model.NewsSource

const val DEFAULT_NEWS_LANGUAGE = "en"

// NewsAPI's developer plan never returns more than this many results for one query.
const val MAX_RESULTS = 100

interface NewsRepository {

    suspend fun topHeadlines(
        page: Int,
        category: NewsCategory? = null,
        language: String = DEFAULT_NEWS_LANGUAGE,
    ): AppResult<ArticlePage>

    suspend fun searchArticles(
        query: String,
        page: Int,
        language: String = DEFAULT_NEWS_LANGUAGE,
    ): AppResult<ArticlePage>

    suspend fun sources(
        category: NewsCategory? = null,
        language: String = DEFAULT_NEWS_LANGUAGE,
    ): AppResult<List<NewsSource>>

    // NewsAPI can't fetch one article by id, so this reads the copy cached when a feed or search loaded it.
    suspend fun article(key: String): Article?
}
