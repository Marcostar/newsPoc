package com.m10.everystory.data.saved

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedArticleDao {

    @Query("SELECT * FROM saved_articles ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<SavedArticleEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_articles WHERE articleKey = :articleKey)")
    fun observeIsSaved(articleKey: String): Flow<Boolean>

    @Query("SELECT * FROM saved_articles WHERE articleKey = :articleKey")
    suspend fun getByKey(articleKey: String): SavedArticleEntity?

    @Upsert
    suspend fun upsert(article: SavedArticleEntity)

    @Query("DELETE FROM saved_articles WHERE articleKey = :articleKey")
    suspend fun delete(articleKey: String)
}
