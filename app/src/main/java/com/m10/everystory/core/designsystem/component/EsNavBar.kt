package com.m10.everystory.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

data class EsNavItem(
    val icon: ImageVector,
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit,
)

// The active tab gets a sage pill, a bold label and brand colour, so it never relies on colour alone.
@Composable
fun EsNavBar(
    items: List<EsNavItem>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 0.dp,
        ) {
            items.forEach { item ->
                NavigationBarItem(
                    selected = item.selected,
                    onClick = item.onClick,
                    icon = { Icon(item.icon, contentDescription = null) },
                    label = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                letterSpacing = TextUnit.Unspecified,
                                fontWeight = if (item.selected) FontWeight.Bold else FontWeight.Normal,
                            ),
                            maxLines = 1,
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}
