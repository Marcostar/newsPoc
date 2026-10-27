package com.m10.everystory.data.news.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface FeedDao {

    // Stories pruned from `articles` simply drop out of the join.
    @Query(
        """
        SELECT articles.* FROM feed_entries
        INNER JOIN articles ON articles.articleKey = feed_entries.articleKey
        WHERE feed_entries.feedKey = :feedKey
        ORDER BY feed_entries.position
        """,
    )
    suspend fun articles(feedKey: String): List<ArticleEntity>

    @Query("SELECT MAX(fetchedAt) FROM feed_entries WHERE feedKey = :feedKey")
    suspend fun fetchedAt(feedKey: String): Long?

    @Insert
    suspend fun insertAll(entries: List<FeedEntryEntity>)

    @Query("DELETE FROM feed_entries WHERE feedKey = :feedKey")
    suspend fun deleteFeed(feedKey: String)

    @Query("DELETE FROM feed_entries WHERE fetchedAt < :cutoffMillis")
    suspend fun deleteFetchedBefore(cutoffMillis: Long)

    // Readers never see half of the old list mixed with half of the new one.
    @Transaction
    suspend fun replaceFeed(feedKey: String, entries: List<FeedEntryEntity>) {
        deleteFeed(feedKey)
        insertAll(entries)
    }
}
