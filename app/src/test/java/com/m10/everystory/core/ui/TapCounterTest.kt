package com.m10.everystory.core.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TapCounterTest {

    private var time = 10_000L
    private val counter = TapCounter(target = 3, windowMillis = 700, now = { time })

    private fun tapAfter(millis: Long): Boolean {
        time += millis
        return counter.tap()
    }

    @Test
    fun `three quick taps complete the run and start the count again`() {
        assertFalse(tapAfter(0))
        assertEquals(2, counter.tapsLeft)
        assertFalse(tapAfter(300))
        assertEquals(1, counter.tapsLeft)

        assertTrue(tapAfter(300))
        assertEquals(0, counter.taps)
    }

    @Test
    fun `a slow tap starts over`() {
        tapAfter(0)
        tapAfter(300)

        assertFalse(tapAfter(800))
        assertEquals(1, counter.taps)
    }

    @Test
    fun `reset clears the count`() {
        tapAfter(0)
        tapAfter(300)

        counter.reset()

        assertEquals(0, counter.taps)
        assertFalse(tapAfter(100))
        assertEquals(1, counter.taps)
    }
}
