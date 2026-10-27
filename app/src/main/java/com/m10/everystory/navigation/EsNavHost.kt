package com.m10.everystory.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.m10.everystory.R
import com.m10.everystory.core.designsystem.theme.EsMotion
import com.m10.everystory.core.ui.LocalNavAnimatedVisibilityScope
import com.m10.everystory.core.ui.LocalReducedMotion
import com.m10.everystory.core.ui.LocalSharedTransitionScope
import com.m10.everystory.feature.datausage.DataUsageRoute
import com.m10.everystory.feature.saved.SavedRoute
import com.m10.everystory.feature.search.SearchRoute
import com.m10.everystory.feature.story.StoryRoute
import com.m10.everystory.feature.today.TodayRoute
import com.m10.everystory.feature.today.TodayViewModel
import com.m10.everystory.feature.you.YouRoute

@Composable
fun EsNavHost(
    navController: NavHostController,
    todayViewModel: TodayViewModel,
    isTourRunning: Boolean,
    onStartTour: () -> Unit,
    onOpenDisplaySettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Short, calm fades between screens (the library default is 700ms). Reduced motion keeps a
    // plain crossfade and drops the travelling headline.
    val reducedMotion = LocalReducedMotion.current
    val duration = if (reducedMotion) EsMotion.REDUCED_MS else EsMotion.STANDARD_MS
    val enter = fadeIn(tween(duration, easing = EsMotion.Easing))
    val exit = fadeOut(tween(duration, easing = EsMotion.Easing))

    SharedTransitionLayout(modifier = modifier) {
        CompositionLocalProvider(LocalSharedTransitionScope provides this.takeUnless { reducedMotion }) {
            NavHost(
                navController = navController,
                startDestination = TodayDestination,
                enterTransition = { enter },
                exitTransition = { exit },
                popEnterTransition = { enter },
                popExitTransition = { exit },
            ) {
                composable<TodayDestination> {
                    WithSharedElements {
                        TodayRoute(
                            viewModel = todayViewModel,
                            isTourRunning = isTourRunning,
                            onSearchClick = { navController.navigate(SearchDestination) },
                            onAaClick = onOpenDisplaySettings,
                            onArticleClick = { article -> navController.navigate(StoryDestination(article.key)) },
                        )
                    }
                }
                composable<SavedDestination> {
                    WithSharedElements {
                        SavedRoute(
                            onSearchClick = { navController.navigate(SearchDestination) },
                            onArticleClick = { article -> navController.navigate(StoryDestination(article.key)) },
                        )
                    }
                }
                composable<YouDestination> {
                    YouRoute(
                        onClose = { navController.navigateToTopLevel(TopLevelDestination.Today) },
                        // The tour runs on Today, so replaying it takes you there.
                        onStartTour = {
                            onStartTour()
                            navController.navigateToTopLevel(TopLevelDestination.Today)
                        },
                        onOpenDisplaySettings = onOpenDisplaySettings,
                        onOpenDataUsage = { navController.navigate(DataUsageDestination) },
                    )
                }
                composable<DataUsageDestination> {
                    DataUsageRoute(onBack = { navController.popBackStack() })
                }
                composable<SearchDestination> {
                    WithSharedElements {
                        SearchRoute(
                            onBack = { navController.popBackStack() },
                            onArticleClick = { article -> navController.navigate(StoryDestination(article.key)) },
                        )
                    }
                }
                composable<StoryDestination> { entry ->
                    val previous = navController.previousBackStackEntry?.destination
                    val backLabel = when {
                        previous?.hasRoute<SearchDestination>() == true -> R.string.nav_search
                        previous?.hasRoute<SavedDestination>() == true -> R.string.nav_saved
                        else -> R.string.nav_today
                    }
                    WithSharedElements {
                        StoryRoute(
                            articleKey = entry.toRoute<StoryDestination>().articleKey,
                            backLabel = stringResource(backLabel),
                            onBack = { navController.popBackStack() },
                            onAaClick = onOpenDisplaySettings,
                        )
                    }
                }
            }
        }
    }
}

// Hands this destination's enter/exit animation to the shared headline and image inside it.
@Composable
private fun AnimatedVisibilityScope.WithSharedElements(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this, content = content)
}
