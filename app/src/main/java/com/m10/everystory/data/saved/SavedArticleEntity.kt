package com.m10.everystory.data.saved

import androidx.room.Entity
import androidx.room.PrimaryKey

// A full copy rather than a flag on the cache table, so the cache's weekly clean-up can never
// take a saved story with it, and saved stories open without a connection.
@Entity(tableName = "saved_articles")
data class SavedArticleEntity(
    @PrimaryKey val articleKey: String,
    val title: String,
    val description: String?,
    val content: String?,
    val url: String,
    val imageUrl: String?,
    val sourceId: String?,
    val sourceName: String,
    val author: String?,
    val publishedAtMillis: Long,
    val savedAt: Long,
)
