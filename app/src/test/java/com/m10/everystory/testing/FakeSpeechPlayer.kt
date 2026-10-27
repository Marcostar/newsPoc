package com.m10.everystory.testing

import com.m10.everystory.core.speech.SpeakResult
import com.m10.everystory.core.speech.SpeechEvent
import com.m10.everystory.core.speech.SpeechPlayer
import com.m10.everystory.domain.model.NewsLanguage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

// Records what it was asked to say; tests play the engine's part by sending events.
class FakeSpeechPlayer(var result: SpeakResult = SpeakResult.Started) : SpeechPlayer {

    data class Request(val parts: List<String>, val language: NewsLanguage, val rate: Float)

    val requests = mutableListOf<Request>()
    var stops = 0
    var released = false

    private val _events = MutableSharedFlow<SpeechEvent>(extraBufferCapacity = 16)
    override val events: SharedFlow<SpeechEvent> get() = _events

    suspend fun send(event: SpeechEvent) = _events.emit(event)

    override suspend fun speak(parts: List<String>, language: NewsLanguage, rate: Float): SpeakResult {
        requests += Request(parts, language, rate)
        return result
    }

    override fun stop() {
        stops++
    }

    override fun release() {
        released = true
    }
}
