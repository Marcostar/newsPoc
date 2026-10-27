package com.m10.everystory.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.google.firebase.analytics.FirebaseAnalytics
import com.m10.everystory.BuildConfig
import com.m10.everystory.core.analytics.AnalyticsTracker
import com.m10.everystory.core.analytics.FirebaseAnalyticsTracker
import com.m10.everystory.core.database.EveryStoryDatabase
import com.m10.everystory.core.network.API_KEY_HEADER
import com.m10.everystory.core.network.ApiKeyInterceptor
import com.m10.everystory.data.news.DefaultNewsRepository
import com.m10.everystory.data.news.remote.NewsApiService
import com.m10.everystory.data.preferences.DataStoreUserPreferencesRepository
import com.m10.everystory.data.saved.RoomSavedArticlesRepository
import com.m10.everystory.data.search.DataStoreRecentSearchesRepository
import com.m10.everystory.domain.repository.NewsRepository
import com.m10.everystory.domain.repository.RecentSearchesRepository
import com.m10.everystory.domain.repository.SavedArticlesRepository
import com.m10.everystory.domain.repository.UserPreferencesRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import timber.log.Timber
import java.util.concurrent.TimeUnit

private const val CONNECT_TIMEOUT_SECONDS = 10L
private const val READ_TIMEOUT_SECONDS = 15L
private const val PREFERENCES_FILE = "user_prefs"
private const val DATABASE_FILE = "everystory.db"

// Manual DI: everything the app shares is created here once, and ViewModel
// factories pull what they need from it.
interface AppContainer {
    val newsRepository: NewsRepository
    val recentSearchesRepository: RecentSearchesRepository
    val userPreferencesRepository: UserPreferencesRepository
    val savedArticlesRepository: SavedArticlesRepository
    val analyticsTracker: AnalyticsTracker
}

class DefaultAppContainer(
    private val context: Context,
) : AppContainer {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(ApiKeyInterceptor(BuildConfig.NEWS_API_KEY))
            .apply { if (BuildConfig.DEBUG) addInterceptor(loggingInterceptor()) }
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.NEWS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    private val database: EveryStoryDatabase by lazy {
        Room.databaseBuilder(context, EveryStoryDatabase::class.java, DATABASE_FILE).build()
    }

    override val newsRepository: NewsRepository by lazy {
        DefaultNewsRepository(
            api = retrofit.create<NewsApiService>(),
            articleDao = database.articleDao(),
            json = json,
        )
    }

    override val savedArticlesRepository: SavedArticlesRepository by lazy {
        RoomSavedArticlesRepository(database.savedArticleDao())
    }

    private val preferences: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile(PREFERENCES_FILE) }
    }

    // One DataStore per file: both preference repositories share it.
    override val recentSearchesRepository: RecentSearchesRepository by lazy {
        DataStoreRecentSearchesRepository(preferences)
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        DataStoreUserPreferencesRepository(preferences)
    }

    override val analyticsTracker: AnalyticsTracker by lazy {
        FirebaseAnalyticsTracker(FirebaseAnalytics.getInstance(context))
    }

    private fun loggingInterceptor() =
        HttpLoggingInterceptor { message -> Timber.tag("OkHttp").d(message) }.apply {
            level = HttpLoggingInterceptor.Level.BASIC
            redactHeader(API_KEY_HEADER)
        }
}
