package com.m10.everystory.data.news

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchQueryTest {

    @Test
    fun `plain query is only trimmed`() {
        assertEquals("climate", searchQueryFor("  climate "))
    }

    @Test
    fun `matched brackets are kept`() {
        assertEquals("(crypto OR bitcoin) AND regulation", searchQueryFor("(crypto OR bitcoin) AND regulation"))
    }

    @Test
    fun `unmatched brackets are dropped`() {
        assertEquals("kzdaj", searchQueryFor("kzdaj)"))
        assertEquals("climate", searchQueryFor("(climate"))
        assertEquals("a (b) c", searchQueryFor(")a (b) c("))
    }

    @Test
    fun `query is capped at 500 characters`() {
        assertEquals(500, searchQueryFor("a".repeat(600)).length)
    }
}
