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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.m10.everystory.core.analytics.AnalyticsScreen
import com.m10.everystory.core.designsystem.component.EsLaunchScreen
import com.m10.everystory.core.designsystem.component.EsNavBar
import com.m10.everystory.core.designsystem.component.EsNavItem
import com.m10.everystory.core.designsystem.theme.EsMotion
import com.m10.everystory.core.ui.LocalTourTargets
import com.m10.everystory.core.ui.TourStep
import com.m10.everystory.core.ui.TourTargets
import com.m10.everystory.core.ui.tourTarget
import com.m10.everystory.feature.today.TodayViewModel
import com.m10.everystory.feature.tour.TourOverlay
import com.m10.everystory.feature.tour.TourViewModel
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

    val tourViewModel: TourViewModel = viewModel(factory = TourViewModel.Factory)
    val tourState by tourViewModel.uiState.collectAsStateWithLifecycle()
    val tourTargets = remember { TourTargets() }
    // Waits for the launch screen, so the first tip lands on a real feed.
    val tourStep = tourState.step.takeUnless { todayState.isLaunching }

    CompositionLocalProvider(LocalTourTargets provides tourTargets) {
        Box {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                // While the tour is up, TalkBack only reads the tour.
                modifier = if (tourStep != null) Modifier.clearAndSetSemantics {} else Modifier,
                bottomBar = {
                    if (currentTab != null) {
                        EsNavBar(
                            items = TopLevelDestination.entries.map { destination ->
                                EsNavItem(
                                    icon = destination.icon,
                                    label = stringResource(destination.labelRes),
                                    selected = destination == currentTab,
                                    onClick = { navController.navigateToTopLevel(destination) },
                                    modifier = when (destination) {
                                        TopLevelDestination.Saved -> Modifier.tourTarget(tourTargets, TourStep.Saved)
                                        TopLevelDestination.You -> Modifier.tourTarget(tourTargets, TourStep.You)
                                        TopLevelDestination.Today -> Modifier
                                    },
                                )
                            },
                        )
                    }
                },
            ) { innerPadding ->
                EsNavHost(
                    navController = navController,
                    todayViewModel = todayViewModel,
                    isTourRunning = tourStep != null,
                    onStartTour = tourViewModel::onStart,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding),
                )
            }

            if (tourStep != null) {
                TourOverlay(
                    step = tourStep,
                    targetBounds = tourTargets.boundsOf(tourStep),
                    onNext = tourViewModel::onNext,
                    onBack = tourViewModel::onBack,
                    onSkip = tourViewModel::onSkip,
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
}
