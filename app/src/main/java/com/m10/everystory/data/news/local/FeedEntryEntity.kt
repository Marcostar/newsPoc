package com.m10.everystory.data.news.local

import androidx.room.Entity

// One row per story in the first page of a feed (language + topic), in the order NewsAPI sent them.
// The story itself lives in `articles`; this only remembers which ones and where.
@Entity(tableName = "feed_entries", primaryKeys = ["feedKey", "position"])
data class FeedEntryEntity(
    val feedKey: String,
    val position: Int,
    val articleKey: String,
    val fetchedAt: Long,
)
