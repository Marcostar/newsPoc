package com.m10.everystory.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.m10.everystory.R
import com.m10.everystory.core.designsystem.icon.EsIcons
import kotlin.reflect.KClass

enum class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    val icon: ImageVector,
    @param:StringRes val labelRes: Int,
) {
    Today(TodayDestination, TodayDestination::class, EsIcons.Today, R.string.nav_today),
    Saved(SavedDestination, SavedDestination::class, EsIcons.SavedTab, R.string.nav_saved),
    You(YouDestination, YouDestination::class, EsIcons.You, R.string.nav_you),
}

fun NavDestination?.topLevelDestination(): TopLevelDestination? =
    TopLevelDestination.entries.firstOrNull { destination ->
        this?.hierarchy?.any { it.hasRoute(destination.routeClass) } == true
    }

fun NavController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        // Each tab keeps its own back stack and scroll position, like the system apps.
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
