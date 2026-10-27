package com.m10.everystory.data.search

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.m10.everystory.domain.repository.RecentSearchesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val MAX_RECENT_SEARCHES = 5

private val RECENT_SEARCHES = stringPreferencesKey("recent_searches")

// Five short strings don't need a database; one newline-separated preference is plenty.
private const val SEPARATOR = "\n"

class DataStoreRecentSearchesRepository(
    private val dataStore: DataStore<Preferences>,
) : RecentSearchesRepository {

    override val recentSearches: Flow<List<String>> =
        dataStore.data.map { prefs -> prefs[RECENT_SEARCHES].toQueryList() }

    override suspend fun add(query: String) {
        dataStore.edit { prefs ->
            prefs[RECENT_SEARCHES] = prefs[RECENT_SEARCHES].toQueryList()
                .withRecent(query)
                .joinToString(SEPARATOR)
        }
    }

    private fun String?.toQueryList(): List<String> =
        this?.split(SEPARATOR)?.filter { it.isNotBlank() }.orEmpty()
}

// Newest first, no case-insensitive duplicates, capped.
fun List<String>.withRecent(query: String, max: Int = MAX_RECENT_SEARCHES): List<String> {
    val cleaned = query.replace(SEPARATOR, " ").trim()
    if (cleaned.isEmpty()) return this
    return (listOf(cleaned) + filterNot { it.equals(cleaned, ignoreCase = true) }).take(max)
}
