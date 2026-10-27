package com.m10.everystory.testing

import com.m10.everystory.data.search.withRecent
import com.m10.everystory.domain.repository.RecentSearchesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeRecentSearchesRepository : RecentSearchesRepository {

    override val recentSearches: StateFlow<List<String>> get() = state
    private val state = MutableStateFlow<List<String>>(emptyList())

    override suspend fun add(query: String) {
        state.value = state.value.withRecent(query)
    }
}
