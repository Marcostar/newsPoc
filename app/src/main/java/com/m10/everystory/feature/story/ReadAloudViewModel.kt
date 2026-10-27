package com.m10.everystory.feature.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.m10.everystory.EveryStoryApplication
import com.m10.everystory.core.analytics.AnalyticsEvent
import com.m10.everystory.core.analytics.AnalyticsTracker
import com.m10.everystory.core.speech.SpeakResult
import com.m10.everystory.core.speech.SpeechEvent
import com.m10.everystory.core.speech.SpeechPlayer
import com.m10.everystory.domain.model.Article
import com.m10.everystory.domain.model.ListenRate
import com.m10.everystory.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReadAloudUiState(
    val playback: Playback = Playback.Stopped,
    val rate: Float = ListenRate.DEFAULT,
    // The part being read and the word within it; null while stopped.
    val spoken: SpokenWord? = null,
    val spokenKind: StoryPart? = null,
    val progress: Float = 0f,
    val problem: ReadAloudProblem? = null,
)

// start == end before the engine reaches the first word of a part.
data class SpokenWord(val part: Int, val start: Int, val end: Int)

enum class Playback { Stopped, Playing, Paused }

enum class ReadAloudProblem { NoVoice, Unavailable }

class ReadAloudViewModel(
    private val player: SpeechPlayer,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val analytics: AnalyticsTracker,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReadAloudUiState())
    val uiState: StateFlow<ReadAloudUiState> = _uiState.asStateFlow()

    private var parts: List<SpokenPart> = emptyList()
    private var speakJob: Job? = null

    // Resume and a new speed restart from the middle of a part, so the engine's part numbers and
    // character positions are relative to where that restart began.
    private var firstPart = 0
    private var firstChar = 0

    init {
        viewModelScope.launch {
            val rate = userPreferencesRepository.listenRate.first()
            _uiState.update { it.copy(rate = rate) }
        }
        viewModelScope.launch { player.events.collect(::onSpeechEvent) }
    }

    fun onListen(article: Article) {
        parts = spokenParts(article)
        analytics.log(AnalyticsEvent.ListenStart(article.source.name, _uiState.value.rate))
        speakFrom(part = 0, char = 0)
    }

    fun onPause() {
        if (_uiState.value.playback != Playback.Playing) return
        speakJob?.cancel()
        player.stop()
        _uiState.update { it.copy(playback = Playback.Paused) }
    }

    fun onResume() {
        val spoken = _uiState.value.spoken ?: return
        if (_uiState.value.playback == Playback.Paused) speakFrom(spoken.part, spoken.start)
    }

    fun onStop() = stopWith(problem = null)

    fun onSpeedClick() {
        val rate = ListenRate.next(_uiState.value.rate)
        _uiState.update { it.copy(rate = rate) }
        viewModelScope.launch { userPreferencesRepository.setListenRate(rate) }
        // The engine can't change speed mid-utterance, so carry on from the current word.
        val spoken = _uiState.value.spoken
        if (_uiState.value.playback == Playback.Playing && spoken != null) speakFrom(spoken.part, spoken.start)
    }

    private fun speakFrom(part: Int, char: Int) {
        firstPart = part
        firstChar = char
        val queue = listOf(parts[part].text.substring(char)) + parts.drop(part + 1).map { it.text }
        _uiState.update {
            it.copy(playback = Playback.Playing, problem = null)
        }
        showWord(queued = 0, start = 0, end = 0)

        speakJob?.cancel()
        speakJob = viewModelScope.launch {
            // Stories don't carry their language, so this assumes the one the reader picked for news.
            val language = userPreferencesRepository.newsLanguage.first()
            when (player.speak(queue, language, _uiState.value.rate)) {
                SpeakResult.Started -> Unit
                SpeakResult.NoVoice -> stopWith(ReadAloudProblem.NoVoice)
                SpeakResult.Unavailable -> stopWith(ReadAloudProblem.Unavailable)
            }
        }
    }

    private fun onSpeechEvent(event: SpeechEvent) {
        if (_uiState.value.playback != Playback.Playing) return
        when (event) {
            is SpeechEvent.PartStarted -> showWord(event.part, start = 0, end = 0)
            is SpeechEvent.Word -> showWord(event.part, event.start, event.end)
            is SpeechEvent.PartDone -> if (firstPart + event.part == parts.lastIndex) stopWith(problem = null)
            SpeechEvent.Failed -> stopWith(ReadAloudProblem.Unavailable)
        }
    }

    private fun showWord(queued: Int, start: Int, end: Int) {
        val part = firstPart + queued
        val shift = if (queued == 0) firstChar else 0
        val word = SpokenWord(part, start + shift, end + shift)
        _uiState.update {
            it.copy(spoken = word, spokenKind = parts.getOrNull(part)?.kind, progress = progressAt(word))
        }
    }

    private fun progressAt(word: SpokenWord): Float {
        val total = parts.sumOf { it.text.length }
        if (total == 0) return 0f
        return (parts.take(word.part).sumOf { it.text.length } + word.start).toFloat() / total
    }

    private fun stopWith(problem: ReadAloudProblem?) {
        speakJob?.cancel()
        player.stop()
        _uiState.update { ReadAloudUiState(rate = it.rate, problem = problem) }
    }

    override fun onCleared() {
        player.release()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EveryStoryApplication
                ReadAloudViewModel(
                    player = app.container.createSpeechPlayer(),
                    userPreferencesRepository = app.container.userPreferencesRepository,
                    analytics = app.container.analyticsTracker,
                )
            }
        }
    }
}
