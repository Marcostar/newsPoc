package com.m10.everystory.core.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class RelativeTimeTest {

    private val zone = ZoneOffset.UTC
    private val now = Instant.parse("2026-10-26T09:30:00Z")

    private fun at(text: String) = relativeTime(Instant.parse(text), now, zone)

    @Test
    fun `under a minute is just now`() {
        assertEquals(RelativeTime.JustNow, at("2026-10-26T09:29:30Z"))
    }

    @Test
    fun `future timestamps from skewed clocks read as just now`() {
        assertEquals(RelativeTime.JustNow, at("2026-10-26T09:35:00Z"))
    }

    @Test
    fun `under an hour is minutes`() {
        assertEquals(RelativeTime.MinutesAgo(12), at("2026-10-26T09:18:00Z"))
    }

    @Test
    fun `under a day is hours`() {
        assertEquals(RelativeTime.HoursAgo(3), at("2026-10-26T06:10:00Z"))
        assertEquals(RelativeTime.HoursAgo(23), at("2026-10-25T10:00:00Z"))
    }

    @Test
    fun `more than a day but on the previous date is yesterday`() {
        assertEquals(RelativeTime.Yesterday, at("2026-10-25T08:00:00Z"))
    }

    @Test
    fun `older stories show their date`() {
        assertEquals(RelativeTime.OnDate(LocalDate.of(2026, 10, 20)), at("2026-10-20T12:00:00Z"))
    }
}
