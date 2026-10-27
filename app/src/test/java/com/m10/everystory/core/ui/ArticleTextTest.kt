package com.m10.everystory.core.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ArticleTextTest {

    @Test
    fun `domain drops the scheme, path and www`() {
        assertEquals("northfieldpost.com", displayDomain("https://www.northfieldpost.com/2026/flood-plan?x=1"))
        assertEquals("apnews.com", displayDomain("https://apnews.com/article/1"))
    }

    @Test
    fun `an unparseable url is shown as given`() {
        assertEquals("not a url", displayDomain("not a url"))
    }

    @Test
    fun `initials skip articles and stop at two letters`() {
        assertEquals("NP", sourceInitials("The Northfield Post"))
        assertEquals("WS", sourceInitials("The Wall Street Journal"))
        assertEquals("BB", sourceInitials("Bloomberg Business"))
        assertEquals("E", sourceInitials("ESPN"))
    }
}
