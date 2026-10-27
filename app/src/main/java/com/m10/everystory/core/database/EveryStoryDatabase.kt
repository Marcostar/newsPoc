package com.m10.everystory.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.m10.everystory.data.news.local.ArticleDao
import com.m10.everystory.data.news.local.ArticleEntity

@Database(entities = [ArticleEntity::class], version = 1, exportSchema = true)
abstract class EveryStoryDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
}
