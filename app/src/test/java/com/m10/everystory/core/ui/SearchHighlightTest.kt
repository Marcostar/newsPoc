package com.m10.everystory.core.ui

import androidx.compose.ui.text.SpanStyle
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchHighlightTest {

    @Test
    fun `operators, quotes and excluded words are not highlighted`() {
        assertEquals(listOf("night", "shift", "levee"), searchTerms("\"night shift\" AND +levee -repairs"))
        assertEquals(listOf("levee"), searchTerms("+levee -repairs OR a"))
    }

    @Test
    fun `every match is highlighted regardless of case`() {
        val highlighted = highlightTerms("Levee breaks; levee rebuilt", listOf("levee"), SpanStyle())

        assertEquals(listOf(0 to 5, 14 to 19), highlighted.spanStyles.map { it.start to it.end })
    }
}
