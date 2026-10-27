package com.m10.everystory.domain.repository

import kotlinx.coroutines.flow.Flow

// A NewsAPI key the reader pasted in themselves, used instead of the one built into the app.
interface ApiKeyRepository {

    // Null while the built-in key is in use.
    val ownKey: Flow<String?>

    // Read on OkHttp's thread for every request, so it can't suspend. Blocks briefly the first time.
    fun currentOwnKey(): String?

    suspend fun saveOwnKey(key: String)

    suspend fun removeOwnKey()
}
