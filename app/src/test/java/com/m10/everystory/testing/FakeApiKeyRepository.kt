package com.m10.everystory.testing

import com.m10.everystory.domain.repository.ApiKeyRepository
import kotlinx.coroutines.flow.MutableStateFlow

class FakeApiKeyRepository(key: String? = null) : ApiKeyRepository {

    override val ownKey = MutableStateFlow(key)

    override fun currentOwnKey(): String? = ownKey.value

    override suspend fun saveOwnKey(key: String) {
        ownKey.value = key
    }

    override suspend fun removeOwnKey() {
        ownKey.value = null
    }
}
