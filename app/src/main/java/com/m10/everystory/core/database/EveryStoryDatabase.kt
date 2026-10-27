package com.m10.everystory.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.m10.everystory.data.news.local.ArticleDao
import com.m10.everystory.data.news.local.ArticleEntity
import com.m10.everystory.data.saved.SavedArticleDao
import com.m10.everystory.data.saved.SavedArticleEntity

// v2 adds saved_articles. A new table needs no hand-written SQL, so Room migrates it automatically.
@Database(
    entities = [ArticleEntity::class, SavedArticleEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
abstract class EveryStoryDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao

    abstract fun savedArticleDao(): SavedArticleDao
}
