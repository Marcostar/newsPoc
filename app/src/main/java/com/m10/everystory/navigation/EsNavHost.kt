package com.m10.everystory.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.m10.everystory.R
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
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = TodayDestination,
        modifier = modifier,
    ) {
        composable<TodayDestination> {
            TodayRoute(
                viewModel = todayViewModel,
                onSearchClick = { navController.navigate(SearchDestination) },
                onArticleClick = { article -> navController.navigate(StoryDestination(article.key)) },
            )
        }
        composable<SavedDestination> {
            SavedRoute(
                onSearchClick = { navController.navigate(SearchDestination) },
                onArticleClick = { article -> navController.navigate(StoryDestination(article.key)) },
            )
        }
        composable<YouDestination> {
            YouRoute(onClose = { navController.navigateToTopLevel(TopLevelDestination.Today) })
        }
        composable<SearchDestination> {
            SearchRoute(
                onBack = { navController.popBackStack() },
                onArticleClick = { article -> navController.navigate(StoryDestination(article.key)) },
            )
        }
        composable<StoryDestination> { entry ->
            val previous = navController.previousBackStackEntry?.destination
            val backLabel = when {
                previous?.hasRoute<SearchDestination>() == true -> R.string.nav_search
                previous?.hasRoute<SavedDestination>() == true -> R.string.nav_saved
                else -> R.string.nav_today
            }
            StoryRoute(
                articleKey = entry.toRoute<StoryDestination>().articleKey,
                backLabel = stringResource(backLabel),
                onBack = { navController.popBackStack() },
            )
        }
    }
}

