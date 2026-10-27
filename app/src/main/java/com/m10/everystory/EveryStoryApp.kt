package com.m10.everystory

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.m10.everystory.core.designsystem.component.EsNavBar
import com.m10.everystory.core.designsystem.component.EsNavItem
import com.m10.everystory.navigation.EsNavHost
import com.m10.everystory.navigation.TopLevelDestination
import com.m10.everystory.navigation.navigateToTopLevel
import com.m10.everystory.navigation.topLevelDestination

@Composable
fun EveryStoryApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    // The bar only shows on Today, Saved and You; reading screens get the full height.
    val currentTab = backStackEntry?.destination.topLevelDestination()

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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        )
    }
}
