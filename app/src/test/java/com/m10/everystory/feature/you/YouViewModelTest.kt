package com.m10.everystory.feature.you

import com.m10.everystory.domain.model.ThemeSetting
import com.m10.everystory.testing.FakeUserPreferencesRepository
import com.m10.everystory.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class YouViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val preferences = FakeUserPreferencesRepository()

    @Test
    fun `choosing a theme saves it and the screen reflects it`() = runTest {
        val viewModel = YouViewModel(preferences)
        // WhileSubscribed only updates while someone is collecting, like the screen does.
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.onThemeSelected(ThemeSetting.Dark)
        advanceUntilIdle()

        assertEquals(ThemeSetting.Dark, preferences.themeSetting.first())
        assertEquals(ThemeSetting.Dark, viewModel.uiState.value.themeSetting)
    }
}
