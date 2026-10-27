package com.m10.everystory.feature.story

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration

private val SENTENCE_ENDS = setOf('.', '!', '?', '…')
private val CLOSING_QUOTES = setOf('"', '”', '’', ')')

/**
 * The sentence holding position [at], as a range of indexes into [text]. Simple on purpose:
 * a full stop followed by a space ends a sentence, unless it closes an abbreviation like
 * "U.S." or an initial like "J." — common in news copy. "Mr." still splits; good enough for a band.
 */
fun sentenceRange(text: String, at: Int): IntRange {
    val position = at.coerceIn(0, text.length)
    val previousEnd = (position - 1 downTo 0).firstOrNull { endsSentence(text, it) }
    var start = previousEnd?.plus(1) ?: 0
    while (start < position && text[start].isWhitespace()) start++
    val end = (position until text.length).firstOrNull { endsSentence(text, it) } ?: text.lastIndex
    return start..end
}

private fun endsSentence(text: String, index: Int): Boolean {
    val char = text[index]
    val isEnd = char in SENTENCE_ENDS || (char in CLOSING_QUOTES && index > 0 && text[index - 1] in SENTENCE_ENDS)
    return isEnd && (index == text.lastIndex || text[index + 1].isWhitespace()) && !isAbbreviation(text, index)
}

private fun isAbbreviation(text: String, dotIndex: Int): Boolean {
    if (text[dotIndex] != '.') return false
    val wordStart = text.lastIndexOfAny(charArrayOf(' ', '\n', '\t'), dotIndex) + 1
    val word = text.substring(wordStart, dotIndex)
    return word.length == 1 || '.' in word
}

// Background colours and an underline only, never weight or size, so the text doesn't reflow
// while it's being read.
fun spokenText(
    text: String,
    word: SpokenWord?,
    wordColor: Color,
    sentenceColor: Color,
): AnnotatedString = buildAnnotatedString {
    append(text)
    if (word == null || text.isEmpty()) return@buildAnnotatedString
    val start = word.start.coerceIn(0, text.length)
    val end = word.end.coerceIn(start, text.length)
    val sentence = sentenceRange(text, start)
    addStyle(SpanStyle(background = sentenceColor), sentence.first, sentence.last + 1)
    if (end > start) {
        addStyle(SpanStyle(background = wordColor, textDecoration = TextDecoration.Underline), start, end)
    }
}
