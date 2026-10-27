package com.m10.everystory.domain.repository

import com.m10.everystory.domain.model.Article
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface SavedArticlesRepository {
    val savedArticles: Flow<List<Article>>

    fun isSaved(key: String): Flow<Boolean>

    suspend fun savedArticle(key: String): Article?

    suspend fun save(article: Article)

    suspend fun remove(key: String)

    // Stories saved after this moment and still saved now.
    suspend fun countSavedSince(since: Instant): Int
}
