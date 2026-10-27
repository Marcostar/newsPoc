package com.m10.everystory.domain.repository

import com.m10.everystory.core.common.AppResult
import com.m10.everystory.domain.model.ArticlePage
import com.m10.everystory.domain.model.NewsCategory
import com.m10.everystory.domain.model.NewsSource

const val DEFAULT_NEWS_LANGUAGE = "en"

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
}
