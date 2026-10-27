package com.m10.everystory.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.m10.everystory.core.analytics.AnalyticsScreen
import kotlinx.serialization.Serializable

@Serializable
data object TodayDestination

@Serializable
data object SavedDestination

@Serializable
data object YouDestination

@Serializable
data object SearchDestination

@Serializable
data class StoryDestination(val articleKey: String)

@Serializable
data object DataUsageDestination

fun NavDestination.analyticsScreen(): AnalyticsScreen? = when {
    hasRoute<TodayDestination>() -> AnalyticsScreen.Today
    hasRoute<SavedDestination>() -> AnalyticsScreen.Saved
    hasRoute<YouDestination>() -> AnalyticsScreen.You
    hasRoute<SearchDestination>() -> AnalyticsScreen.Search
    hasRoute<StoryDestination>() -> AnalyticsScreen.Story
    hasRoute<DataUsageDestination>() -> AnalyticsScreen.DataUsage
    else -> null
}
