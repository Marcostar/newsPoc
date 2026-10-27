package com.m10.everystory.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.m10.everystory.data.news.local.ArticleDao
import com.m10.everystory.data.news.local.ArticleEntity
import com.m10.everystory.data.news.local.FeedDao
import com.m10.everystory.data.news.local.FeedEntryEntity
import com.m10.everystory.data.saved.SavedArticleDao
import com.m10.everystory.data.saved.SavedArticleEntity

// v2 adds saved_articles, v3 feed_entries. New tables need no hand-written SQL, so Room migrates
// them automatically.
@Database(
    entities = [ArticleEntity::class, SavedArticleEntity::class, FeedEntryEntity::class],
    version = 3,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2), AutoMigration(from = 2, to = 3)],
)
abstract class EveryStoryDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao

    abstract fun savedArticleDao(): SavedArticleDao

    abstract fun feedDao(): FeedDao
}
