package com.m10.everystory.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.m10.everystory.domain.model.ListenRate
import com.m10.everystory.domain.model.NewsLanguage
import com.m10.everystory.domain.model.TextSize
import com.m10.everystory.domain.model.ThemeSetting
import com.m10.everystory.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

private val THEME_SETTING = stringPreferencesKey("theme_setting")
private val TEXT_SIZE_STEP = intPreferencesKey("text_size_step")
private val NEWS_LANGUAGE = stringPreferencesKey("news_language")
private val SAVED_LAST_VIEWED_AT = longPreferencesKey("saved_last_viewed_at")
private val NOTIFICATION_PERMISSION_ASKED = booleanPreferencesKey("notification_permission_asked")
private val TOUR_SEEN = booleanPreferencesKey("tour_seen")
private val LISTEN_RATE = floatPreferencesKey("listen_rate")

// englishOnly pins the news language for automated runs (Firebase Test Lab): other languages
// cost an extra sources request per feed, and crawlers would burn the free quota switching them.
class DataStoreUserPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
    private val englishOnly: Boolean = false,
) : UserPreferencesRepository {

    // Stored by name; an unknown value from an older build falls back to following the system.
    override val themeSetting: Flow<ThemeSetting> = dataStore.data.map { prefs ->
        ThemeSetting.entries.firstOrNull { it.name == prefs[THEME_SETTING] } ?: ThemeSetting.System
    }

    override suspend fun setThemeSetting(setting: ThemeSetting) {
        dataStore.edit { it[THEME_SETTING] = setting.name }
    }

    override val textSizeStep: Flow<Int> = dataStore.data.map { prefs ->
        (prefs[TEXT_SIZE_STEP] ?: TextSize.DEFAULT_STEP).coerceIn(TextSize.scales.indices)
    }

    override suspend fun setTextSizeStep(step: Int) {
        dataStore.edit { it[TEXT_SIZE_STEP] = step.coerceIn(TextSize.scales.indices) }
    }

    // Stored as NewsAPI's code; a language dropped from the list falls back to English.
    override val newsLanguage: Flow<NewsLanguage> = dataStore.data.map { prefs ->
        if (englishOnly) NewsLanguage.English else NewsLanguage.fromCode(prefs[NEWS_LANGUAGE])
    }

    override suspend fun setNewsLanguage(language: NewsLanguage) {
        if (englishOnly) return
        dataStore.edit { it[NEWS_LANGUAGE] = language.code }
    }

    override val savedLastViewedAt: Flow<Instant> = dataStore.data.map { prefs ->
        Instant.ofEpochMilli(prefs[SAVED_LAST_VIEWED_AT] ?: 0L)
    }

    override suspend fun setSavedLastViewedAt(time: Instant) {
        dataStore.edit { it[SAVED_LAST_VIEWED_AT] = time.toEpochMilli() }
    }

    override val notificationPermissionAsked: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[NOTIFICATION_PERMISSION_ASKED] ?: false
    }

    override suspend fun setNotificationPermissionAsked() {
        dataStore.edit { it[NOTIFICATION_PERMISSION_ASKED] = true }
    }

    override val tourSeen: Flow<Boolean> = dataStore.data.map { prefs -> prefs[TOUR_SEEN] ?: false }

    override suspend fun setTourSeen() {
        dataStore.edit { it[TOUR_SEEN] = true }
    }

    override val listenRate: Flow<Float> = dataStore.data.map { prefs -> ListenRate.valid(prefs[LISTEN_RATE]) }

    override suspend fun setListenRate(rate: Float) {
        dataStore.edit { it[LISTEN_RATE] = ListenRate.valid(rate) }
    }
}
