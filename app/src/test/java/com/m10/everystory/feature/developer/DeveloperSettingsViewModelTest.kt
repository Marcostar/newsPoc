package com.m10.everystory.feature.developer

import com.m10.everystory.core.analytics.AnalyticsEvent
import com.m10.everystory.core.common.AppError
import com.m10.everystory.core.common.AppResult
import com.m10.everystory.domain.usecase.SaveOwnApiKeyUseCase
import com.m10.everystory.testing.FakeAnalyticsTracker
import com.m10.everystory.testing.FakeApiKeyRepository
import com.m10.everystory.testing.FakeNewsRepository
import com.m10.everystory.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

private const val GOOD_KEY = "0123456789abcdef0123456789abcd1a"

@OptIn(ExperimentalCoroutinesApi::class)
class DeveloperSettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val news = FakeNewsRepository()
    private val keys = FakeApiKeyRepository()
    private val analytics = FakeAnalyticsTracker()

    private fun createViewModel() =
        DeveloperSettingsViewModel(keys, SaveOwnApiKeyUseCase(news, keys), analytics)

    @Test
    fun `saving a good key clears the field, shows the ending and confirms`() = runTest {
        val viewModel = createViewModel()
        viewModel.onInputChange(GOOD_KEY)

        viewModel.onTestAndSave()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("cd1a", state.ownKeyEnding)
        assertEquals("", state.input)
        assertFalse(state.isChecking)
        assertEquals(DeveloperMessage.KeySaved, state.message)
        assertEquals(listOf(AnalyticsEvent.OwnApiKey(saved = true)), analytics.events)
    }

    @Test
    fun `a rejected key keeps the input and explains`() = runTest {
        news.keyCheckResult = AppResult.Failure(AppError.Unauthorized)
        val viewModel = createViewModel()
        viewModel.onInputChange(GOOD_KEY)

        viewModel.onTestAndSave()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(KeyInputError.Rejected, state.inputError)
        assertEquals(GOOD_KEY, state.input)
        assertNull(state.ownKeyEnding)
    }

    @Test
    fun `typing clears the previous error`() = runTest {
        val viewModel = createViewModel()
        viewModel.onTestAndSave()
        advanceUntilIdle()
        assertEquals(KeyInputError.Empty, viewModel.uiState.value.inputError)

        viewModel.onInputChange("4")

        assertNull(viewModel.uiState.value.inputError)
    }

    @Test
    fun `removing the key goes back to the built-in one`() = runTest {
        keys.ownKey.value = GOOD_KEY
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onRemoveKey()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.ownKeyEnding)
        assertEquals(DeveloperMessage.KeyRemoved, viewModel.uiState.value.message)
        assertEquals(listOf(AnalyticsEvent.OwnApiKey(saved = false)), analytics.events)
    }
}
