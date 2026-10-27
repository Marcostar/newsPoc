package com.m10.everystory.domain.usecase

import com.m10.everystory.core.common.AppError
import com.m10.everystory.core.common.AppResult
import com.m10.everystory.testing.FakeApiKeyRepository
import com.m10.everystory.testing.FakeNewsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private const val GOOD_KEY = "0123456789abcdef0123456789abcdef"

class SaveOwnApiKeyUseCaseTest {

    private val news = FakeNewsRepository()
    private val keys = FakeApiKeyRepository()
    private val saveOwnApiKey = SaveOwnApiKeyUseCase(news, keys)

    @Test
    fun `an accepted key is trimmed, checked and saved`() = runTest {
        assertEquals(SaveOwnApiKeyUseCase.Result.Saved, saveOwnApiKey("  $GOOD_KEY\n"))

        assertEquals(listOf(GOOD_KEY), news.checkedKeys)
        assertEquals(GOOD_KEY, keys.currentOwnKey())
    }

    @Test
    fun `empty and badly shaped keys never reach the network`() = runTest {
        assertEquals(SaveOwnApiKeyUseCase.Result.Empty, saveOwnApiKey("   "))
        assertEquals(SaveOwnApiKeyUseCase.Result.WrongFormat, saveOwnApiKey("not a key"))
        assertEquals(SaveOwnApiKeyUseCase.Result.WrongFormat, saveOwnApiKey(GOOD_KEY.replace('a', 'z')))

        assertTrue(news.checkedKeys.isEmpty())
        assertNull(keys.currentOwnKey())
    }

    @Test
    fun `a key NewsAPI rejects is not saved`() = runTest {
        news.keyCheckResult = AppResult.Failure(AppError.Unauthorized)

        assertEquals(SaveOwnApiKeyUseCase.Result.Rejected, saveOwnApiKey(GOOD_KEY))
        assertNull(keys.currentOwnKey())
    }

    @Test
    fun `a failed check says why and saves nothing`() = runTest {
        news.keyCheckResult = AppResult.Failure(AppError.Offline)

        assertEquals(SaveOwnApiKeyUseCase.Result.CouldNotCheck(AppError.Offline), saveOwnApiKey(GOOD_KEY))
        assertNull(keys.currentOwnKey())
    }
}
