package com.m10.everystory.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.m10.everystory.R
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

sealed interface RelativeTime {
    data object JustNow : RelativeTime
    data class MinutesAgo(val minutes: Long) : RelativeTime
    data class HoursAgo(val hours: Long) : RelativeTime
    data object Yesterday : RelativeTime
    data class OnDate(val date: LocalDate) : RelativeTime
}

// Design rule: "3 h ago" under a day, then "Yesterday", then a short date like "Tue 20 Oct".
fun relativeTime(publishedAt: Instant, now: Instant, zone: ZoneId): RelativeTime {
    val elapsed = Duration.between(publishedAt, now)
    val published = publishedAt.atZone(zone).toLocalDate()
    val today = now.atZone(zone).toLocalDate()
    return when {
        elapsed < Duration.ofMinutes(1) -> RelativeTime.JustNow
        elapsed < Duration.ofHours(1) -> RelativeTime.MinutesAgo(elapsed.toMinutes())
        elapsed < Duration.ofDays(1) -> RelativeTime.HoursAgo(elapsed.toHours())
        published == today.minusDays(1) -> RelativeTime.Yesterday
        else -> RelativeTime.OnDate(published)
    }
}

@Composable
fun relativeTimeText(publishedAt: Instant): String {
    val locale = LocalConfiguration.current.locales[0]
    return when (val time = relativeTime(publishedAt, Instant.now(), ZoneId.systemDefault())) {
        RelativeTime.JustNow -> stringResource(R.string.time_just_now)
        is RelativeTime.MinutesAgo -> stringResource(R.string.time_minutes_ago, time.minutes)
        is RelativeTime.HoursAgo -> stringResource(R.string.time_hours_ago, time.hours)
        RelativeTime.Yesterday -> stringResource(R.string.time_yesterday)
        is RelativeTime.OnDate -> time.date.format(DateTimeFormatter.ofPattern("EEE d MMM", locale))
    }
}

fun clockTimeText(instant: Instant): String =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault())
        .format(instant)
