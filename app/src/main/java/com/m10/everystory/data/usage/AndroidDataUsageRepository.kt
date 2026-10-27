package com.m10.everystory.data.usage

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.net.ConnectivityManager
import android.os.Process
import com.m10.everystory.domain.model.DailyBytes
import com.m10.everystory.domain.model.DataUsage
import com.m10.everystory.domain.model.NetworkKind
import com.m10.everystory.domain.model.TrafficCategory
import com.m10.everystory.domain.repository.DataUsageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

private const val DAYS_SHOWN = 7L

/**
 * Android's own per-app traffic count (the figure in Settings › Apps › Data usage), split by
 * network, plus the stories and images EveryStory counted itself. An app may read its own count
 * without any special permission.
 */
class AndroidDataUsageRepository(
    private val networkStatsManager: NetworkStatsManager,
    private val trafficCounter: TrafficCounter,
    private val zone: ZoneId = ZoneId.systemDefault(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : DataUsageRepository {

    override suspend fun usage(network: NetworkKind, today: LocalDate): DataUsage = withContext(ioDispatcher) {
        val month = YearMonth.from(today)
        val tomorrow = today.plusDays(1)
        DataUsage(
            network = network,
            appBytes = appBytes(network, from = month.atDay(1), until = tomorrow),
            storiesBytes = trafficCounter.bytes(month, network, TrafficCategory.Stories),
            imagesBytes = trafficCounter.bytes(month, network, TrafficCategory.Images),
            lastSevenDays = (DAYS_SHOWN - 1 downTo 0).map { daysAgo ->
                val day = today.minusDays(daysAgo)
                DailyBytes(day, appBytes(network, from = day, until = day.plusDays(1)) ?: 0L)
            },
        )
    }

    private fun appBytes(network: NetworkKind, from: LocalDate, until: LocalDate): Long? = try {
        networkStatsManager.queryDetailsForUid(
            network.connectivityType(),
            null, // all SIMs; Android 10+ accepts this without the phone-state permission
            from.atStartOfDay(zone).toInstant().toEpochMilli(),
            until.atStartOfDay(zone).toInstant().toEpochMilli(),
            Process.myUid(),
        ).use { it.totalBytes() }
    } catch (e: SecurityException) {
        // Android 9 and older want a SIM id for mobile data, which needs a permission we don't ask for.
        Timber.w(e, "No %s usage from the system", network)
        null
    }

    private fun NetworkStats.totalBytes(): Long {
        val bucket = NetworkStats.Bucket()
        var total = 0L
        while (hasNextBucket()) {
            getNextBucket(bucket)
            total += bucket.rxBytes + bucket.txBytes
        }
        return total
    }

    // NetworkStatsManager still speaks the old ConnectivityManager network types.
    @Suppress("DEPRECATION")
    private fun NetworkKind.connectivityType(): Int = when (this) {
        NetworkKind.Mobile -> ConnectivityManager.TYPE_MOBILE
        NetworkKind.WiFi -> ConnectivityManager.TYPE_WIFI
    }
}
