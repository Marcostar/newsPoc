package com.m10.everystory.data.news.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ArticleDao {

    @Upsert
    suspend fun upsertAll(articles: List<ArticleEntity>)

    @Query("SELECT * FROM articles WHERE articleKey = :articleKey")
    suspend fun getByKey(articleKey: String): ArticleEntity?

    @Query("DELETE FROM articles WHERE cachedAt < :cutoffMillis")
    suspend fun deleteCachedBefore(cutoffMillis: Long)
}
