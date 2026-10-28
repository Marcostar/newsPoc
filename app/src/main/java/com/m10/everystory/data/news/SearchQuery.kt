package com.m10.everystory.data.news

private const val MAX_QUERY_LENGTH = 500

// NewsAPI caps q at 500 characters and answers 400 "unexpectedError" when a bracket has no partner
// (monkey found it with "kzdaj)"; a reader typing "(climate" would too). Unmatched ones are dropped,
// matched groups are kept because NewsAPI uses them for AND/OR/NOT.
internal fun searchQueryFor(typed: String): String {
    val capped = typed.trim().take(MAX_QUERY_LENGTH)
    val unmatched = mutableSetOf<Int>()
    val open = ArrayDeque<Int>()
    capped.forEachIndexed { index, char ->
        when (char) {
            '(' -> open.addLast(index)
            ')' -> if (open.isEmpty()) unmatched += index else open.removeLast()
        }
    }
    unmatched += open
    return capped.filterIndexed { index, _ -> index !in unmatched }.trim()
}
