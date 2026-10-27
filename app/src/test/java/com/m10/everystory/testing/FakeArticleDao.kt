package com.m10.everystory.testing

import com.m10.everystory.data.news.local.ArticleDao
import com.m10.everystory.data.news.local.ArticleEntity

class FakeArticleDao : ArticleDao {

    val rows = linkedMapOf<String, ArticleEntity>()
    var failWrites = false

    override suspend fun upsertAll(articles: List<ArticleEntity>) {
        if (failWrites) error("disk full")
        articles.forEach { rows[it.articleKey] = it }
    }

    override suspend fun getByKey(articleKey: String): ArticleEntity? = rows[articleKey]

    override suspend fun deleteCachedBefore(cutoffMillis: Long) {
        rows.values.removeAll { it.cachedAt < cutoffMillis }
    }
}
