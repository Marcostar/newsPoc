package com.m10.everystory.feature.story

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class SpeechHighlightTest {

    private val text = "The council voted. Homes will move! “It’s time,” she said. Done"

    @Test
    fun `a sentence runs from after the previous full stop to its own`() {
        assertEquals("The council voted.", text.substring(sentenceRange(text, 4)))
        assertEquals("Homes will move!", text.substring(sentenceRange(text, text.indexOf("will"))))
    }

    @Test
    fun `a closing quote stays with its sentence`() {
        assertEquals("“It’s time,” she said.", text.substring(sentenceRange(text, text.indexOf("time"))))
    }

    @Test
    fun `abbreviations and initials don't end a sentence`() {
        val news = "Filed in U.S. District Court by J. Smith on Monday. Next one."
        assertEquals("Filed in U.S. District Court by J. Smith on Monday.", news.substring(sentenceRange(news, news.indexOf("Court"))))
    }

    @Test
    fun `text without a final full stop ends at the last character`() {
        assertEquals("Done", text.substring(sentenceRange(text, text.indexOf("Done"))))
    }

    @Test
    fun `the word gets its own style on top of the sentence band`() {
        val word = Color.Green
        val sentence = Color.Gray

        val annotated = spokenText(text, SpokenWord(part = 0, start = 4, end = 11), word, sentence)

        val styles = annotated.spanStyles.map { Triple(it.item.background, it.start, it.end) }
        assertEquals(listOf(Triple(sentence, 0, 18), Triple(word, 4, 11)), styles)
    }

    @Test
    fun `nothing is highlighted while stopped`() {
        assertEquals(emptyList<Any>(), spokenText(text, null, Color.Green, Color.Gray).spanStyles)
    }
}
