package com.m10.everystory.feature.story

import com.m10.everystory.core.analytics.AnalyticsEvent
import com.m10.everystory.core.speech.SpeakResult
import com.m10.everystory.core.speech.SpeechEvent
import com.m10.everystory.domain.model.NewsLanguage
import com.m10.everystory.testing.FakeAnalyticsTracker
import com.m10.everystory.testing.FakeSpeechPlayer
import com.m10.everystory.testing.FakeUserPreferencesRepository
import com.m10.everystory.testing.MainDispatcherRule
import com.m10.everystory.testing.testArticle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReadAloudViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val player = FakeSpeechPlayer()
    private val preferences = FakeUserPreferencesRepository()
    private val analytics = FakeAnalyticsTracker()
    private val article = testArticle("a").copy(
        title = "Flood plan passes",
        description = "The council voted. Homes will move.",
        content = "Work starts in spring.",
    )

    private fun TestScope.listening(): ReadAloudViewModel {
        val viewModel = ReadAloudViewModel(player, preferences, analytics)
        advanceUntilIdle()
        viewModel.onListen(article)
        advanceUntilIdle()
        return viewModel
    }

    @Test
    fun `listen reads headline, standfirst and excerpt in the reader's language and speed`() = runTest {
        preferences.setNewsLanguage(NewsLanguage.German)
        preferences.setListenRate(1.25f)

        val viewModel = listening()

        val request = player.requests.single()
        assertEquals(listOf("Flood plan passes", "The council voted. Homes will move.", "Work starts in spring."), request.parts)
        assertEquals(NewsLanguage.German, request.language)
        assertEquals(1.25f, request.rate)
        assertEquals(Playback.Playing, viewModel.uiState.value.playback)
        assertEquals(AnalyticsEvent.ListenStart("Northfield Post", 1.25f), analytics.events.single())
    }

    @Test
    fun `the spoken word follows the engine`() = runTest {
        val viewModel = listening()

        player.send(SpeechEvent.Word(part = 1, start = 4, end = 11))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(SpokenWord(part = 1, start = 4, end = 11), state.spoken)
        assertEquals(StoryPart.Standfirst, state.spokenKind)
        assertTrue(state.progress > 0f)
    }

    @Test
    fun `resume carries on from the word it paused on`() = runTest {
        val viewModel = listening()
        player.send(SpeechEvent.Word(part = 1, start = 19, end = 24))
        advanceUntilIdle()

        viewModel.onPause()
        assertEquals(Playback.Paused, viewModel.uiState.value.playback)
        assertEquals(1, player.stops)

        viewModel.onResume()
        advanceUntilIdle()
        assertEquals(listOf("Homes will move.", "Work starts in spring."), player.requests.last().parts)

        // The engine now counts from the restart, so its first word maps back into the full text.
        player.send(SpeechEvent.Word(part = 0, start = 6, end = 10))
        advanceUntilIdle()
        assertEquals(SpokenWord(part = 1, start = 25, end = 29), viewModel.uiState.value.spoken)
    }

    @Test
    fun `a new speed is remembered and picks up from the current word`() = runTest {
        val viewModel = listening()
        player.send(SpeechEvent.Word(part = 0, start = 6, end = 10))
        advanceUntilIdle()

        viewModel.onSpeedClick()
        advanceUntilIdle()

        assertEquals(1.25f, viewModel.uiState.value.rate)
        assertEquals(1.25f, preferences.listenRate.first())
        val restart = player.requests.last()
        assertEquals(1.25f, restart.rate)
        assertEquals("plan passes", restart.parts.first())
    }

    @Test
    fun `finishing the last part stops the player`() = runTest {
        val viewModel = listening()

        player.send(SpeechEvent.PartDone(part = 2))
        advanceUntilIdle()

        assertEquals(Playback.Stopped, viewModel.uiState.value.playback)
        assertNull(viewModel.uiState.value.spoken)
    }

    @Test
    fun `a missing voice is explained instead of playing`() = runTest {
        player.result = SpeakResult.NoVoice

        val viewModel = listening()

        assertEquals(Playback.Stopped, viewModel.uiState.value.playback)
        assertEquals(ReadAloudProblem.NoVoice, viewModel.uiState.value.problem)
    }

    @Test
    fun `stop clears the highlight and keeps the speed`() = runTest {
        preferences.setListenRate(1.5f)
        val viewModel = listening()
        player.send(SpeechEvent.Word(part = 0, start = 0, end = 5))
        advanceUntilIdle()

        viewModel.onStop()

        assertEquals(ReadAloudUiState(rate = 1.5f), viewModel.uiState.value)
    }
}
