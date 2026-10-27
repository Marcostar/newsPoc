package com.m10.everystory.data.usage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.m10.everystory.domain.model.NetworkKind
import com.m10.everystory.domain.model.TrafficCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.YearMonth

// What the counting interceptor talks to; a plain function so tests can collect the numbers.
fun interface TrafficRecorder {
    fun record(category: TrafficCategory, network: NetworkKind, bytes: Long)
}

/**
 * Running byte totals per month, network and category ("2026-10/WiFi/Images"), kept in their own
 * DataStore file. Old months simply stay behind; at a few bytes each they cost nothing.
 */
class TrafficCounter(
    private val dataStore: DataStore<Preferences>,
    private val scope: CoroutineScope,
    private val currentMonth: () -> YearMonth = YearMonth::now,
) : TrafficRecorder {

    // Called on OkHttp's threads, which mustn't wait on disk; the write runs on the app's scope.
    override fun record(category: TrafficCategory, network: NetworkKind, bytes: Long) {
        if (bytes <= 0) return
        val month = currentMonth()
        scope.launch { add(month, network, category, bytes) }
    }

    suspend fun add(month: YearMonth, network: NetworkKind, category: TrafficCategory, bytes: Long) {
        val key = key(month, network, category)
        dataStore.edit { it[key] = (it[key] ?: 0L) + bytes }
    }

    suspend fun bytes(month: YearMonth, network: NetworkKind, category: TrafficCategory): Long =
        dataStore.data.first()[key(month, network, category)] ?: 0L

    private fun key(month: YearMonth, network: NetworkKind, category: TrafficCategory): Preferences.Key<Long> =
        longPreferencesKey("$month/${network.name}/${category.name}")
}
