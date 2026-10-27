package com.m10.everystory.data.news.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "articles", indices = [Index("cachedAt")])
data class ArticleEntity(
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
    val cachedAt: Long,
)
