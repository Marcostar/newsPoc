package com.m10.everystory.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.m10.everystory.domain.model.ThemeSetting
import com.m10.everystory.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val THEME_SETTING = stringPreferencesKey("theme_setting")
private val TOUR_SEEN = booleanPreferencesKey("tour_seen")

class DataStoreUserPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {

    // Stored by name; an unknown value from an older build falls back to following the system.
    override val themeSetting: Flow<ThemeSetting> = dataStore.data.map { prefs ->
        ThemeSetting.entries.firstOrNull { it.name == prefs[THEME_SETTING] } ?: ThemeSetting.System
    }

    override suspend fun setThemeSetting(setting: ThemeSetting) {
        dataStore.edit { it[THEME_SETTING] = setting.name }
    }

    override val tourSeen: Flow<Boolean> = dataStore.data.map { prefs -> prefs[TOUR_SEEN] ?: false }

    override suspend fun setTourSeen() {
        dataStore.edit { it[TOUR_SEEN] = true }
    }
}
