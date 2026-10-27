package com.m10.everystory.data.search

import org.junit.Assert.assertEquals
import org.junit.Test

class RecentSearchesTest {

    @Test
    fun `newest query goes first`() {
        assertEquals(listOf("levee", "fare cap"), listOf("fare cap").withRecent("levee"))
    }

    @Test
    fun `repeating a query moves it to the top without duplicating`() {
        assertEquals(listOf("Levee", "fare cap"), listOf("fare cap", "levee").withRecent("Levee"))
    }

    @Test
    fun `list is capped`() {
        val five = listOf("a", "b", "c", "d", "e")
        assertEquals(listOf("f", "a", "b", "c", "d"), five.withRecent("f"))
    }

    @Test
    fun `blank queries are ignored`() {
        assertEquals(listOf("a"), listOf("a").withRecent("   "))
    }
}
