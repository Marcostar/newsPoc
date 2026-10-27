package com.m10.everystory.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

enum class ThemePref { Light, Dark, System }

// In-app text size, like Kindle. Multiplies on top of the system font scale.
val TextSizeSteps = listOf(0.85f, 0.92f, 1f, 1.12f, 1.25f, 1.4f, 1.6f)
const val DEFAULT_TEXT_SIZE_INDEX = 2

// Android's own font size setting stops at 200%; past that our layouts aren't designed to hold.
private const val MAX_FONT_SCALE = 2f

@Composable
fun EveryStoryTheme(
    themePref: ThemePref = ThemePref.System,
    textSizeIndex: Int = DEFAULT_TEXT_SIZE_INDEX,
    highContrast: Boolean = false,
    content: @Composable () -> Unit,
) {
    val dark = when (themePref) {
        ThemePref.Light -> false
        ThemePref.Dark -> true
        ThemePref.System -> isSystemInDarkTheme()
    }
    val esColors = if (dark) NightEsColors else DayEsColors
    val baseScheme = if (dark) NightColors else DayColors
    val colorScheme = if (highContrast) {
        baseScheme.copy(onSurfaceVariant = baseScheme.onSurface, outlineVariant = esColors.muted)
    } else {
        baseScheme
    }

    val density = LocalDensity.current
    val step = TextSizeSteps[textSizeIndex.coerceIn(TextSizeSteps.indices)]
    val fontScale = (density.fontScale * step).coerceAtMost(MAX_FONT_SCALE)

    CompositionLocalProvider(
        LocalEsColors provides esColors,
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

object EsTheme {
    val colors: EsColors
        @Composable @ReadOnlyComposable get() = LocalEsColors.current
}
