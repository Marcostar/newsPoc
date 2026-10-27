package com.m10.everystory.testing

import com.m10.everystory.data.news.local.ArticleEntity
import com.m10.everystory.data.news.local.FeedDao
import com.m10.everystory.data.news.local.FeedEntryEntity

// Joins against the article fake, like the real query joins against the articles table.
class FakeFeedDao(private val articleDao: FakeArticleDao) : FeedDao {

    val entries = mutableListOf<FeedEntryEntity>()

    override suspend fun articles(feedKey: String): List<ArticleEntity> =
        entries.filter { it.feedKey == feedKey }.sortedBy { it.position }.mapNotNull { articleDao.rows[it.articleKey] }

    override suspend fun fetchedAt(feedKey: String): Long? =
        entries.filter { it.feedKey == feedKey }.maxOfOrNull { it.fetchedAt }

    override suspend fun insertAll(entries: List<FeedEntryEntity>) {
        this.entries += entries
    }

    override suspend fun deleteFeed(feedKey: String) {
        entries.removeAll { it.feedKey == feedKey }
    }

    override suspend fun deleteFetchedBefore(cutoffMillis: Long) {
        entries.removeAll { it.fetchedAt < cutoffMillis }
    }
}
