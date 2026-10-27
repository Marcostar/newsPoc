package com.m10.everystory.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DataUsageTest {

    private fun usage(appBytes: Long?, stories: Long, images: Long) = DataUsage(
        network = NetworkKind.WiFi,
        appBytes = appBytes,
        storiesBytes = stories,
        imagesBytes = images,
        lastSevenDays = listOf(DailyBytes(LocalDate.of(2026, 10, 27), 0)),
    )

    @Test
    fun `ads and other is whatever Android counted beyond our own numbers`() {
        val usage = usage(appBytes = 10_000, stories = 1_000, images = 6_000)

        assertEquals(10_000, usage.totalBytes)
        assertEquals(3_000, usage.adsAndOtherBytes)
    }

    @Test
    fun `while Android's count lags behind ours, the total never shows less than we measured`() {
        val usage = usage(appBytes = 5_000, stories = 1_000, images = 6_000)

        assertEquals(7_000, usage.totalBytes)
        assertEquals(0, usage.adsAndOtherBytes)
    }

    @Test
    fun `without Android's count the total is our own`() {
        assertEquals(7_000, usage(appBytes = null, stories = 1_000, images = 6_000).totalBytes)
    }
}
