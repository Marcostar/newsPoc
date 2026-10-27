package com.m10.everystory.core.speech

import com.m10.everystory.domain.model.NewsLanguage
import kotlinx.coroutines.flow.Flow

/**
 * Speaks a list of texts one after the other and reports progress. Part indexes in events are
 * positions in the list given to the latest [speak]; events from earlier calls are never sent.
 */
interface SpeechPlayer {
    val events: Flow<SpeechEvent>

    suspend fun speak(parts: List<String>, language: NewsLanguage, rate: Float): SpeakResult

    fun stop()

    // Frees the engine; the next speak() starts a new one.
    fun release()
}

sealed interface SpeechEvent {
    data class PartStarted(val part: Int) : SpeechEvent

    // The engine is about to say text[start until end] of that part.
    data class Word(val part: Int, val start: Int, val end: Int) : SpeechEvent

    data class PartDone(val part: Int) : SpeechEvent

    data object Failed : SpeechEvent
}

enum class SpeakResult { Started, NoVoice, Unavailable }
