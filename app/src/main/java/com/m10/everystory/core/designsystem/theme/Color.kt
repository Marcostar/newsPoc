package com.m10.everystory.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// EveryStory design tokens. Composables read roles, never these hex values.

val DayColors = lightColorScheme(
    primary = Color(0xFF0F3B3A), onPrimary = Color(0xFFF7F1E6),
    secondaryContainer = Color(0xFFC9D6C3), onSecondaryContainer = Color(0xFF0F3B3A),
    tertiary = Color(0xFFE07A5F), tertiaryContainer = Color(0xFFFBEEE8), onTertiaryContainer = Color(0xFFA8432A),
    background = Color(0xFFF7F1E6), onBackground = Color(0xFF1A2422),
    surface = Color(0xFFF7F1E6), onSurface = Color(0xFF1A2422), onSurfaceVariant = Color(0xFF34413E),
    surfaceContainer = Color(0xFFFFFBF4), surfaceContainerHigh = Color(0xFFE9EEE4),
    outline = Color(0xFF1A2422), outlineVariant = Color(0xFFDDD4C3),
    error = Color(0xFF1A2422), onError = Color(0xFFF7F1E6), // no red: problems are ink + icon + words
)

val NightColors = darkColorScheme(
    primary = Color(0xFF8CCBBE), onPrimary = Color(0xFF0F1B1A),
    secondaryContainer = Color(0xFF30453F), onSecondaryContainer = Color(0xFF8CCBBE),
    tertiary = Color(0xFFE9876C), tertiaryContainer = Color(0xFF1A2A27), onTertiaryContainer = Color(0xFFF2A58E),
    background = Color(0xFF0F1B1A), onBackground = Color(0xFFF2EBDD),
    surface = Color(0xFF0F1B1A), onSurface = Color(0xFFF2EBDD), onSurfaceVariant = Color(0xFFDDD5C6),
    surfaceContainer = Color(0xFF162523), surfaceContainerHigh = Color(0xFF1E312E),
    outline = Color(0xFFF2EBDD), outlineVariant = Color(0xFF2C403C),
    error = Color(0xFFF2EBDD), onError = Color(0xFF0F1B1A),
)

// Roles Material 3 has no slot for. Read them through EsTheme.colors.
@Immutable
data class EsColors(
    val muted: Color,
    val brandPressed: Color,
    val coralInk: Color,
    val beyondBg: Color,
    val beyondLine: Color,
    val beyondMark: Color,
    val highlightWord: Color,
    val highlightSentence: Color,
    val notice: Color,
    val adBg: Color,
    val disabledFill: Color,
    val disabledText: Color,
    val placeholderA: Color,
    val placeholderB: Color,
    val sageMark: Color,
)

val DayEsColors = EsColors(
    muted = Color(0xFF56635F),
    brandPressed = Color(0xFF1C5551),
    coralInk = Color(0xFFA8432A),
    beyondBg = Color(0xFFFFFBF4),
    beyondLine = Color(0xFFEBCABD),
    beyondMark = Color(0xFFE07A5F),
    highlightWord = Color(0xFFC9D6C3),
    highlightSentence = Color(0xFFE9EEE4),
    notice = Color(0xFFF3E7C9),
    adBg = Color(0xFFFFFFFF),
    disabledFill = Color(0xFFE6E1D6),
    disabledText = Color(0xFF56635F),
    placeholderA = Color(0xFFE4E9DC),
    placeholderB = Color(0xFFD9E1D0),
    sageMark = Color(0xFF8FAE9F),
)

val NightEsColors = EsColors(
    muted = Color(0xFFA9B5AF),
    brandPressed = Color(0xFFA9DACF),
    coralInk = Color(0xFFF2A58E),
    beyondBg = Color(0xFF1A2A27),
    beyondLine = Color(0xFF6E4A3F),
    beyondMark = Color(0xFFE9876C),
    highlightWord = Color(0xFF3B554E),
    highlightSentence = Color(0xFF1E312E),
    notice = Color(0xFF3A3320),
    adBg = Color(0xFF162523),
    disabledFill = Color(0xFF26332F),
    disabledText = Color(0xFFA9B5AF),
    placeholderA = Color(0xFF1C2D2A),
    placeholderB = Color(0xFF223532),
    sageMark = Color(0xFF8FAE9F),
)

val LocalEsColors = staticCompositionLocalOf { DayEsColors }
