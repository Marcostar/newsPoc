package com.m10.everystory.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

// Android's own font size setting stops at 200%; past that our layouts aren't designed to hold.
private const val MAX_FONT_SCALE = 2f

@Composable
fun EveryStoryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // The reader's in-app text size, multiplied on top of the phone's font scale.
    textScale: Float = 1f,
    highContrast: Boolean = false,
    content: @Composable () -> Unit,
) {
    val esColors = if (darkTheme) NightEsColors else DayEsColors
    val baseScheme = if (darkTheme) NightColors else DayColors
    val colorScheme = if (highContrast) {
        baseScheme.copy(onSurfaceVariant = baseScheme.onSurface, outlineVariant = esColors.muted)
    } else {
        baseScheme
    }

    val density = LocalDensity.current
    val fontScale = (density.fontScale * textScale).coerceAtMost(MAX_FONT_SCALE)

    CompositionLocalProvider(
        LocalEsColors provides esColors,
        LocalDarkTheme provides darkTheme,
        LocalDensity provides Density(density.density, fontScale),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = EsTypography,
            shapes = EsShapes,
            content = content,
        )
    }
}

// The reader can pick a theme that differs from the phone's, so anything with a day/night variant
// (like the logo) asks the app theme, not the -night resource folder.
private val LocalDarkTheme = staticCompositionLocalOf { false }

object EsTheme {
    val colors: EsColors
        @Composable @ReadOnlyComposable get() = LocalEsColors.current

    val isDark: Boolean
        @Composable @ReadOnlyComposable get() = LocalDarkTheme.current
}
