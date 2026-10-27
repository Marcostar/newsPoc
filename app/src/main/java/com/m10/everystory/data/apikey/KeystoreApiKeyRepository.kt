package com.m10.everystory.data.apikey

import android.content.SharedPreferences
import androidx.core.content.edit
import com.m10.everystory.domain.repository.ApiKeyRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import timber.log.Timber

private const val PREF_OWN_KEY = "own_api_key"

// SharedPreferences rather than DataStore: the OkHttp interceptor needs the key synchronously on
// its own thread, and DataStore would mean runBlocking there. Only the encrypted form is stored.
class KeystoreApiKeyRepository(
    private val preferences: SharedPreferences,
    private val cipher: KeystoreCipher,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ApiKeyRepository {

    private val key = MutableStateFlow<String?>(null)
    private var loaded = false

    override val ownKey: Flow<String?> = key
        .onStart { currentOwnKey() }
        .flowOn(ioDispatcher)

    @Synchronized
    override fun currentOwnKey(): String? {
        if (!loaded) {
            key.value = readStoredKey()
            loaded = true
        }
        return key.value
    }

    override suspend fun saveOwnKey(key: String) = withContext(ioDispatcher) {
        preferences.edit { putString(PREF_OWN_KEY, cipher.encrypt(key)) }
        remember(key)
    }

    override suspend fun removeOwnKey() = withContext(ioDispatcher) {
        preferences.edit { remove(PREF_OWN_KEY) }
        remember(null)
    }

    @Synchronized
    private fun remember(value: String?) {
        key.value = value
        loaded = true
    }

    // Keystore keys don't survive a reinstall or a restore to a new phone, so an old stored value
    // can stop decrypting. Then we forget it and fall back to the built-in key.
    private fun readStoredKey(): String? {
        val stored = preferences.getString(PREF_OWN_KEY, null) ?: return null
        return try {
            cipher.decrypt(stored)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Stored API key can't be decrypted; using the built-in key")
            preferences.edit { remove(PREF_OWN_KEY) }
            null
        }
    }
}
