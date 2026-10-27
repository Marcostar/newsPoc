package com.m10.everystory.feature.display

import com.m10.everystory.core.analytics.AnalyticsEvent
import com.m10.everystory.domain.model.TextSize
import com.m10.everystory.domain.model.ThemeSetting
import com.m10.everystory.testing.FakeAnalyticsTracker
import com.m10.everystory.testing.FakeUserPreferencesRepository
import com.m10.everystory.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DisplaySettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val preferences = FakeUserPreferencesRepository()
    private val analytics = FakeAnalyticsTracker()

    // uiState is WhileSubscribed, so collect it the way the sheet does.
    private fun TestScope.createViewModel() = DisplaySettingsViewModel(preferences, analytics).also { viewModel ->
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    @Test
    fun `a new text size is saved, shown and tracked`() = runTest {
        val viewModel = createViewModel()

        viewModel.onTextSizeChange(4)
        advanceUntilIdle()

        assertEquals(4, preferences.textSizeStep.first())
        assertEquals(4, viewModel.uiState.value.textSizeStep)
        assertEquals(listOf(AnalyticsEvent.TextSizeChange(4)), analytics.events)
    }

    @Test
    fun `sizes past either end stay at the end`() = runTest {
        val viewModel = createViewModel()

        viewModel.onTextSizeChange(99)
        advanceUntilIdle()
        assertEquals(TextSize.scales.lastIndex, viewModel.uiState.value.textSizeStep)

        viewModel.onTextSizeChange(-1)
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.textSizeStep)
    }

    @Test
    fun `choosing the current size again does nothing`() = runTest {
        val viewModel = createViewModel()

        // A slider drag reports the same stop many times over.
        viewModel.onTextSizeChange(TextSize.DEFAULT_STEP)
        advanceUntilIdle()

        assertEquals(emptyList<AnalyticsEvent>(), analytics.events)
    }

    @Test
    fun `reset goes back to 100 percent and the phone's theme`() = runTest {
        val viewModel = createViewModel()
        viewModel.onTextSizeChange(6)
        viewModel.onThemeSelected(ThemeSetting.Dark)
        advanceUntilIdle()

        viewModel.onReset()
        advanceUntilIdle()

        assertEquals(TextSize.DEFAULT_STEP, preferences.textSizeStep.first())
        assertEquals(ThemeSetting.System, preferences.themeSetting.first())
    }

    @Test
    fun `text size is sent to analytics as a percentage`() {
        assertEquals(mapOf("text_size" to "125"), AnalyticsEvent.TextSizeChange(4).params)
    }
}
