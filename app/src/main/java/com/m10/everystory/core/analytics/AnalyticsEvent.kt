package com.m10.everystory.core.analytics

import com.m10.everystory.core.common.AppError
import com.m10.everystory.core.ui.TourStep
import com.m10.everystory.domain.model.NewsCategory
import com.m10.everystory.domain.model.TextSize
import com.m10.everystory.domain.model.ThemeSetting
import kotlin.math.roundToInt

/**
 * Every event the app sends, in one place. `screen_view` and `search` use Firebase's standard
 * names and params so they show up in the built-in reports.
 */
sealed class AnalyticsEvent(
    val name: String,
    val params: Map<String, String> = emptyMap(),
) {
    data class ScreenView(val screen: AnalyticsScreen) :
        AnalyticsEvent("screen_view", mapOf("screen_name" to screen.value))

    data class Search(val query: String) :
        AnalyticsEvent("search", mapOf("search_term" to query))

    data class TopicSelect(val category: NewsCategory?) :
        AnalyticsEvent("topic_select", mapOf("topic" to (category?.apiValue ?: "all")))

    data object FeedRefresh : AnalyticsEvent("feed_refresh")

    data class LoadError(val screen: AnalyticsScreen, val error: AppError) :
        AnalyticsEvent("load_error", mapOf("screen_name" to screen.value, "error_type" to error.analyticsValue()))

    data class StoryOpen(val source: String) :
        AnalyticsEvent("story_open", mapOf("source" to source))

    data class StoryLinkOut(val source: String) :
        AnalyticsEvent("story_link_out", mapOf("source" to source))

    data class StorySave(val source: String) :
        AnalyticsEvent("story_save", mapOf("source" to source))

    data class StoryUnsave(val source: String) :
        AnalyticsEvent("story_unsave", mapOf("source" to source))

    // Sent as a percentage (85–160) so reports read naturally.
    data class TextSizeChange(val step: Int) :
        AnalyticsEvent("text_size_change", mapOf("text_size" to (TextSize.scaleOf(step) * 100).roundToInt().toString()))

    data object TourComplete : AnalyticsEvent("tour_complete")

    // Which tip people give up on says more than the skip itself.
    data class TourSkip(val step: TourStep) :
        AnalyticsEvent("tour_skip", mapOf("step" to (step.ordinal + 1).toString()))

    data class ThemeChange(val theme: ThemeSetting) :
        AnalyticsEvent("theme_change", mapOf("theme" to theme.name.lowercase()))
}

// Spelled out because R8 renames classes in release, so simpleName would be gibberish.
private fun AppError.analyticsValue(): String = when (this) {
    AppError.Offline -> "offline"
    AppError.Timeout -> "timeout"
    AppError.Unauthorized -> "unauthorized"
    AppError.RateLimited -> "rate_limited"
    AppError.Server -> "server"
    is AppError.Unknown -> "unknown"
}
