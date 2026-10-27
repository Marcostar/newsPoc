package com.m10.everystory.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.m10.everystory.R
import com.m10.everystory.core.designsystem.theme.EsSpace
import com.m10.everystory.feature.search.SearchRoute
import com.m10.everystory.feature.today.TodayRoute
import com.m10.everystory.feature.today.TodayViewModel

@Composable
fun EsNavHost(
    navController: NavHostController,
    todayViewModel: TodayViewModel,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = TodayRoute,
        modifier = modifier,
    ) {
        composable<TodayRoute> {
            TodayRoute(
                viewModel = todayViewModel,
                onSearchClick = { navController.navigate(SearchRoute) },
                onArticleClick = { article -> navController.navigate(StoryRoute(article.key)) },
            )
        }
        composable<SavedRoute> { PlaceholderScreen(R.string.saved_title) }
        composable<YouRoute> { PlaceholderScreen(R.string.you_title) }
        composable<SearchRoute> {
            SearchRoute(
                onBack = { navController.popBackStack() },
                onArticleClick = { article -> navController.navigate(StoryRoute(article.key)) },
            )
        }
        composable<StoryRoute> { entry ->
            val route = entry.toRoute<StoryRoute>()
            PlaceholderScreen(R.string.story_title) { Text(route.articleKey) }
        }
    }
}

// Stand-in until each feature screen lands.
@Composable
private fun PlaceholderScreen(
    @StringRes titleRes: Int,
    content: @Composable () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = EsSpace.screen, vertical = EsSpace.s24),
        verticalArrangement = Arrangement.spacedBy(EsSpace.s16),
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() },
        )
        content()
    }
}
