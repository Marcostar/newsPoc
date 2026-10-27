package com.m10.everystory.feature.tour

import com.m10.everystory.core.analytics.AnalyticsEvent
import com.m10.everystory.core.ui.TourStep
import com.m10.everystory.testing.FakeAnalyticsTracker
import com.m10.everystory.testing.FakeUserPreferencesRepository
import com.m10.everystory.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TourViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val analytics = FakeAnalyticsTracker()

    @Test
    fun `first launch starts the tour on the first tip`() = runTest {
        val viewModel = TourViewModel(FakeUserPreferencesRepository(tourSeen = false), analytics)
        advanceUntilIdle()

        assertEquals(TourStep.Search, viewModel.uiState.value.step)
    }

    @Test
    fun `the tour does not come back once seen`() = runTest {
        val viewModel = TourViewModel(FakeUserPreferencesRepository(tourSeen = true), analytics)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.step)
    }

    @Test
    fun `next and back move between tips, and back stops at the first`() = runTest {
        val viewModel = TourViewModel(FakeUserPreferencesRepository(), analytics)
        advanceUntilIdle()

        viewModel.onBack()
        assertEquals(TourStep.Search, viewModel.uiState.value.step)
        viewModel.onNext()
        viewModel.onNext()
        assertEquals(TourStep.Language, viewModel.uiState.value.step)
        viewModel.onBack()
        assertEquals(TourStep.TextSize, viewModel.uiState.value.step)
    }

    @Test
    fun `next on the last tip finishes and remembers it`() = runTest {
        val preferences = FakeUserPreferencesRepository()
        val viewModel = TourViewModel(preferences, analytics)
        advanceUntilIdle()

        repeat(TourStep.entries.size) { viewModel.onNext() }
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.step)
        assertTrue(preferences.tourSeen.first())
        assertEquals(listOf(AnalyticsEvent.TourComplete), analytics.events)
    }

    @Test
    fun `skip closes the tour, remembers it and records the tip`() = runTest {
        val preferences = FakeUserPreferencesRepository()
        val viewModel = TourViewModel(preferences, analytics)
        advanceUntilIdle()

        viewModel.onNext()
        viewModel.onSkip()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.step)
        assertTrue(preferences.tourSeen.first())
        assertEquals(listOf(AnalyticsEvent.TourSkip(TourStep.TextSize)), analytics.events)
    }

    @Test
    fun `replaying from You starts again at the first tip`() = runTest {
        val preferences = FakeUserPreferencesRepository(tourSeen = true)
        val viewModel = TourViewModel(preferences, analytics)
        advanceUntilIdle()

        viewModel.onStart()

        assertEquals(TourStep.Search, viewModel.uiState.value.step)
    }
}
