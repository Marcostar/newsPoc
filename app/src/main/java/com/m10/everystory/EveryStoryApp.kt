package com.m10.everystory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.m10.everystory.core.analytics.AnalyticsScreen
import com.m10.everystory.core.designsystem.component.EsLaunchScreen
import com.m10.everystory.core.designsystem.component.EsNavBar
import com.m10.everystory.core.designsystem.component.EsNavItem
import com.m10.everystory.core.designsystem.theme.EsMotion
import com.m10.everystory.feature.today.TodayViewModel
import com.m10.everystory.navigation.EsNavHost
import com.m10.everystory.navigation.TopLevelDestination
import com.m10.everystory.navigation.analyticsScreen
import com.m10.everystory.navigation.navigateToTopLevel
import com.m10.everystory.navigation.topLevelDestination

@Composable
fun EveryStoryApp(onScreenViewed: (AnalyticsScreen) -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()

    // Keyed on the entry, so coming back to a screen counts as a new view. Uses the entry captured
    // here rather than reading the state inside the effect, which would log the first screen twice.
    val currentEntry = backStackEntry
    LaunchedEffect(currentEntry?.id) {
        currentEntry?.destination?.analyticsScreen()?.let(onScreenViewed)
    }
    // The bar only shows on Today, Saved and You; reading screens get the full height.
    val currentTab = backStackEntry?.destination.topLevelDestination()

    // Scoped to the activity rather than the nav entry: the launch screen waits on its first load,
    // and the feed survives switching tabs.
    val todayViewModel: TodayViewModel = viewModel(factory = TodayViewModel.Factory)
    val todayState by todayViewModel.uiState.collectAsStateWithLifecycle()

    Box {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (currentTab != null) {
                    EsNavBar(
                        items = TopLevelDestination.entries.map { destination ->
                            EsNavItem(
                                icon = destination.icon,
                                label = stringResource(destination.labelRes),
                                selected = destination == currentTab,
                                onClick = { navController.navigateToTopLevel(destination) },
                            )
                        },
                    )
                }
            },
        ) { innerPadding ->
            EsNavHost(
                navController = navController,
                todayViewModel = todayViewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
            )
        }

        AnimatedVisibility(
            visible = todayState.isLaunching,
            enter = EnterTransition.None,
            exit = fadeOut(tween(EsMotion.ENTER_EXIT_MS)),
        ) {
            EsLaunchScreen()
        }
    }
}
