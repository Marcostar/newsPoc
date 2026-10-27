package com.m10.everystory.domain.model

import java.time.LocalDate

enum class NetworkKind { Mobile, WiFi }

enum class TrafficCategory { Stories, Images }

data class DailyBytes(val date: LocalDate, val bytes: Long)

/**
 * One network's data use this month. [appBytes] is Android's own count for the whole app (ads and
 * Google services included); stories and images are what EveryStory measured itself.
 * [appBytes] is null when the phone won't share its count (mobile data on Android 9 and older).
 */
data class DataUsage(
    val network: NetworkKind,
    val appBytes: Long?,
    val storiesBytes: Long,
    val imagesBytes: Long,
    val lastSevenDays: List<DailyBytes>,
) {
    // Android adds up its figures in batches, so for a while our own counts can run ahead of it.
    val totalBytes: Long get() = maxOf(appBytes ?: 0L, storiesBytes + imagesBytes)

    // Whatever Android counted that we didn't: ads, Firebase, Play services.
    val adsAndOtherBytes: Long get() = totalBytes - storiesBytes - imagesBytes
}
