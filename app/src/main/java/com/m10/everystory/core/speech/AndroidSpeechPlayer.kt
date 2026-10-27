package com.m10.everystory.core.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.m10.everystory.domain.model.NewsLanguage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

private const val EVENT_BUFFER = 64

/**
 * The phone's text-to-speech engine. Each part is its own utterance, and onRangeStart (API 26+)
 * tells us which word is being spoken. Call from the main thread; the engine reports back on its own.
 */
class AndroidSpeechPlayer(private val context: Context) : SpeechPlayer {

    private val _events = MutableSharedFlow<SpeechEvent>(extraBufferCapacity = EVENT_BUFFER)
    override val events: Flow<SpeechEvent> = _events.asSharedFlow()

    private var engine: TextToSpeech? = null
    private var ready = CompletableDeferred<Boolean>()

    // Utterance ids are "session:part"; stop() and every speak() move on to a new session, so
    // late callbacks from the old queue are dropped.
    private val session = AtomicInteger()

    private val listener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String) {
            partOf(utteranceId)?.let { _events.tryEmit(SpeechEvent.PartStarted(it)) }
        }

        override fun onRangeStart(utteranceId: String, start: Int, end: Int, frame: Int) {
            partOf(utteranceId)?.let { _events.tryEmit(SpeechEvent.Word(it, start, end)) }
        }

        override fun onDone(utteranceId: String) {
            partOf(utteranceId)?.let { _events.tryEmit(SpeechEvent.PartDone(it)) }
        }

        @Deprecated("Replaced by onError(String, Int), still abstract")
        override fun onError(utteranceId: String) {
            onError(utteranceId, TextToSpeech.ERROR)
        }

        override fun onError(utteranceId: String, errorCode: Int) {
            Timber.w("Text-to-speech error %d", errorCode)
            if (partOf(utteranceId) != null) _events.tryEmit(SpeechEvent.Failed)
        }
    }

    override suspend fun speak(parts: List<String>, language: NewsLanguage, rate: Float): SpeakResult {
        val tts = startedEngine() ?: return SpeakResult.Unavailable
        val languageResult = tts.setLanguage(Locale.forLanguageTag(language.code))
        if (languageResult == TextToSpeech.LANG_MISSING_DATA || languageResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            Timber.i("No text-to-speech voice for %s", language.code)
            return SpeakResult.NoVoice
        }
        tts.setSpeechRate(rate)

        val current = session.incrementAndGet()
        parts.forEachIndexed { index, text ->
            val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            if (tts.speak(text, queueMode, null, "$current:$index") == TextToSpeech.ERROR) {
                return SpeakResult.Unavailable
            }
        }
        return SpeakResult.Started
    }

    override fun stop() {
        session.incrementAndGet()
        engine?.stop()
    }

    override fun release() {
        stop()
        engine?.shutdown()
        engine = null
        ready = CompletableDeferred()
    }

    // The engine binds to a system service, so it's only created the first time someone taps Listen.
    private suspend fun startedEngine(): TextToSpeech? {
        val tts = engine ?: TextToSpeech(context) { status -> ready.complete(status == TextToSpeech.SUCCESS) }
            .also {
                it.setOnUtteranceProgressListener(listener)
                engine = it
            }
        return tts.takeIf { ready.await() }
    }

    private fun partOf(utteranceId: String): Int? {
        val (id, part) = utteranceId.split(':').takeIf { it.size == 2 } ?: return null
        return part.toIntOrNull()?.takeIf { id.toIntOrNull() == session.get() }
    }
}
