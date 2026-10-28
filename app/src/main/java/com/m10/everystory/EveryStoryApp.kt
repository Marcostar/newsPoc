package com.m10.everystory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.m10.everystory.core.ads.AdBanner
import com.m10.everystory.core.analytics.AnalyticsScreen
import com.m10.everystory.core.designsystem.component.EsConnectivityBanner
import com.m10.everystory.core.designsystem.component.EsLaunchScreen
import com.m10.everystory.core.designsystem.component.EsNavBar
import com.m10.everystory.core.designsystem.component.EsNavItem
import com.m10.everystory.core.designsystem.theme.EsMotion
import com.m10.everystory.core.ui.ConnectivityStatus
import com.m10.everystory.core.ui.LocalReducedMotion
import com.m10.everystory.core.ui.LocalTourTargets
import com.m10.everystory.core.ui.TourStep
import com.m10.everystory.core.ui.TourTargets
import com.m10.everystory.core.ui.clockTimeText
import com.m10.everystory.core.ui.rememberSystemReducedMotion
import com.m10.everystory.core.ui.tourTarget
import com.m10.everystory.feature.display.TextAndDisplaySheet
import com.m10.everystory.feature.today.TodayViewModel
import com.m10.everystory.feature.tour.TourOverlay
import com.m10.everystory.feature.tour.TourViewModel
import com.m10.everystory.navigation.EsNavHost
import com.m10.everystory.navigation.TopLevelDestination
import com.m10.everystory.navigation.analyticsScreen
import com.m10.everystory.navigation.navigateToTopLevel
import com.m10.everystory.navigation.topLevelDestination
import kotlinx.coroutines.delay

// Long enough to read the name and tagline on a cold start, short enough not to feel like a wait.
private const val MIN_LAUNCH_SCREEN_MS = 1_600L

@Composable
fun EveryStoryApp(
    openSavedOnStart: Boolean,
    connectivity: ConnectivityStatus,
    onBackOnlineHandled: () -> Unit,
    onScreenViewed: (AnalyticsScreen) -> Unit,
    showAds: Boolean,
    onAdImpression: () -> Unit,
    onAdClick: () -> Unit,
) {
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

    // The cached feed is ready almost at once, so without a floor the logo and tagline would flash
    // past unread. Saveable, so rotating or switching theme doesn't play it again.
    var launchFloorPassed by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(MIN_LAUNCH_SCREEN_MS)
        launchFloorPassed = true
    }
    val showLaunchScreen = todayState.isLaunching || !launchFloorPassed

    val tourViewModel: TourViewModel = viewModel(factory = TourViewModel.Factory)
    val tourState by tourViewModel.uiState.collectAsStateWithLifecycle()
    val tourTargets = remember { TourTargets() }
    // Waits for the launch screen, so the first tip lands on a real feed.
    val tourStep = tourState.step.takeUnless { showLaunchScreen }

    // Opened from Today, Story and You, so it lives here rather than on any one screen.
    var showDisplaySettings by rememberSaveable { mutableStateOf(false) }

    CompositionLocalProvider(
        LocalTourTargets provides tourTargets,
        LocalReducedMotion provides rememberSystemReducedMotion(),
    ) {
        Box {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                // While the tour is up, TalkBack only reads the tour.
                modifier = if (tourStep != null) Modifier.clearAndSetSemantics {} else Modifier,
                // On every screen, as in the prototype: reading a story offline is when it matters most.
                topBar = {
                    EsConnectivityBanner(
                        status = connectivity,
                        updatedAt = todayState.updatedAt?.let { clockTimeText(it) },
                        showSavedAction = currentTab != TopLevelDestination.Saved,
                        onSavedClick = { navController.navigateToTopLevel(TopLevelDestination.Saved) },
                        onRefreshClick = {
                            onBackOnlineHandled()
                            navController.navigateToTopLevel(TopLevelDestination.Today)
                            todayViewModel.onRefresh()
                        },
                    )
                },
                bottomBar = {
                    if (currentTab != null) {
                        Column {
                            // One banner, on Saved only: it has room to spare, while Today stays uncluttered.
                            // Never while reading, never in settings.
                            if (showAds && currentTab == TopLevelDestination.Saved) {
                                AdBanner(onImpression = onAdImpression, onClick = onAdClick)
                            }
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
                    }
                },
            ) { innerPadding ->
                EsNavHost(
                    navController = navController,
                    todayViewModel = todayViewModel,
                    isTourRunning = tourStep != null,
                    onStartTour = tourViewModel::onStart,
                    onOpenDisplaySettings = { showDisplaySettings = true },
                    openSavedOnStart = openSavedOnStart,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding),
                )
            }

            if (showDisplaySettings) {
                TextAndDisplaySheet(onDismiss = { showDisplaySettings = false })
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
                visible = showLaunchScreen,
                enter = EnterTransition.None,
                exit = fadeOut(tween(EsMotion.ENTER_EXIT_MS)),
            ) {
                EsLaunchScreen()
            }
        }
    }
}
