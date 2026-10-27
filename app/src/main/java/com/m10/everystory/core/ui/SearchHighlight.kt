package com.m10.everystory.core.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

private val OPERATORS = setOf("AND", "OR", "NOT")
private const val MIN_TERM_LENGTH = 2

// NewsAPI queries can carry quotes, +must, -exclude and AND/OR/NOT. Only the words the reader
// is actually looking for get highlighted, so "-repairs" never lights up "repairs".
fun searchTerms(query: String): List<String> =
    query.split(' ')
        .filterNot { it.startsWith('-') }
        .map { it.trim('"', '+', '(', ')') }
        .filter { it.length >= MIN_TERM_LENGTH && it.uppercase() !in OPERATORS }
        .distinct()

fun highlightTerms(text: String, terms: List<String>, style: SpanStyle): AnnotatedString =
    buildAnnotatedString {
        append(text)
        terms.forEach { term ->
            var start = text.indexOf(term, ignoreCase = true)
            while (start >= 0) {
                addStyle(style, start, start + term.length)
                start = text.indexOf(term, start + term.length, ignoreCase = true)
            }
        }
    }
