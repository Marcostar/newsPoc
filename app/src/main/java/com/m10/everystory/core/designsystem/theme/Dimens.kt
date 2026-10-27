package com.m10.everystory.core.designsystem.theme

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object EsSpace {
    val s4 = 4.dp
    val s8 = 8.dp
    val s12 = 12.dp
    val s16 = 16.dp
    val s20 = 20.dp
    val s24 = 24.dp
    val s32 = 32.dp
    val s48 = 48.dp
    val screen = 20.dp // 16.dp below 360dp wide, 24.dp from 412dp
}

object EsSize {
    val minTarget = 48.dp
    val primaryButton = 56.dp
    val appBar = 64.dp
    val bottomNav = 64.dp
    val navPillWidth = 56.dp
    val navPillHeight = 30.dp
    val thumbnail = 64.dp
    val chip = 40.dp // sits inside a 48dp touch target
    val adBar = 56.dp // holds a 320×50 AdMob banner
    val focusRing = 2.dp
    val beyondBand = 4.dp
    val logoMark = 34.dp
    val logoMarkInline = 22.dp
    val launchMark = 64.dp
    val searchField = 52.dp
    val messageIcon = 40.dp
    val inlineIcon = 20.dp
    val chipIcon = 18.dp
    val progressSmall = 20.dp
    val skeletonLine = 16.dp
    val hairline = 1.dp
    val sourceBadge = 40.dp
    val sourceButton = 64.dp
    val snackbarClearance = 96.dp // room to scroll content clear of a snackbar
    val spotlightRing = 3.dp
    val spotlightCorner = 16.dp
    val tourDot = 8.dp
    val tourDotActive = 20.dp
}

object EsRadius {
    val badge = RoundedCornerShape(4.dp)
    val image = RoundedCornerShape(12.dp)
    val button = RoundedCornerShape(14.dp)
    val card = RoundedCornerShape(16.dp)
    val sheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val pill = RoundedCornerShape(percent = 50)
}

val EsShapes = Shapes(
    extraSmall = EsRadius.badge,
    small = RoundedCornerShape(10.dp),
    medium = EsRadius.button,
    large = EsRadius.card,
    extraLarge = RoundedCornerShape(28.dp),
)

object EsMotion {
    const val STANDARD_MS = 200
    const val ENTER_EXIT_MS = 240
    const val REDUCED_MS = 150 // plain crossfades when the reader has asked for less motion

    // Starts quickly and settles gently. No bounce or overshoot anywhere in the app.
    val Easing: Easing = LinearOutSlowInEasing
}
