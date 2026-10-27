package com.m10.everystory.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.m10.everystory.R

// google/fonts only ships variable files for both families, so each weight is pinned on the wght axis.
@OptIn(ExperimentalTextApi::class)
private fun variableFont(resId: Int, weight: FontWeight, style: FontStyle = FontStyle.Normal) = Font(
    resId = resId,
    weight = weight,
    style = style,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val Newsreader = FontFamily(
    variableFont(R.font.newsreader, FontWeight.Normal),
    variableFont(R.font.newsreader, FontWeight.Medium),
    variableFont(R.font.newsreader_italic, FontWeight.Normal, FontStyle.Italic),
)

val Atkinson = FontFamily(
    variableFont(R.font.atkinson_hyperlegible_next, FontWeight.Normal),
    variableFont(R.font.atkinson_hyperlegible_next, FontWeight.Bold),
)

// Every paragraph takes its direction from its own script, so an Arabic headline reads right to
// left in an English layout while the menus around it stay put.
private fun serif(size: Int, line: Int, weight: FontWeight = FontWeight.Medium) = TextStyle(
    fontFamily = Newsreader,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    textDirection = TextDirection.Content,
)

private fun sans(size: Int, line: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = Atkinson,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    textDirection = TextDirection.Content,
)

val EsTypography = Typography(
    displaySmall = serif(34, 40).copy(letterSpacing = (-0.01).em), // "Today"
    headlineMedium = serif(28, 32), // settings sub-screen title
    headlineSmall = serif(26, 32), // story headline
    titleLarge = serif(22, 28), // lead card, sheet titles
    titleMedium = serif(19, 25), // feed row headline
    titleSmall = serif(18, 24),
    bodyLarge = sans(17, 26),
    bodyMedium = sans(15, 20), // meta: source · time
    labelLarge = sans(16, 20, FontWeight.Bold), // buttons
    labelMedium = sans(14, 18, FontWeight.Bold).copy(letterSpacing = 0.08.em), // uppercase at the call site
)

object EsType {
    val standfirst = serif(19, 28, FontWeight.Normal)
    val quote = serif(18, 26, FontWeight.Normal).copy(fontStyle = FontStyle.Italic)

    fun body(serifBody: Boolean, spacing: Float, bold: Boolean) = TextStyle(
        fontFamily = if (serifBody) Newsreader else Atkinson,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = (17 * spacing).sp,
        textDirection = TextDirection.Content,
    )
}
