package com.m10.everystory.domain.repository

import kotlinx.coroutines.flow.Flow

interface RecentSearchesRepository {
    val recentSearches: Flow<List<String>>

    suspend fun add(query: String)
}
