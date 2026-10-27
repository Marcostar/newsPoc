package com.m10.everystory.core.ui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

/**
 * Where each tour target sits on screen. Targets report their own bounds as they're laid out,
 * so the overlay doesn't need to know which screen or component they live in.
 */
@Stable
class TourTargets {
    private val bounds = mutableStateMapOf<TourStep, Rect>()

    fun boundsOf(step: TourStep): Rect? = bounds[step]

    fun report(step: TourStep, rect: Rect) {
        bounds[step] = rect
    }
}

val LocalTourTargets = staticCompositionLocalOf { TourTargets() }

fun Modifier.tourTarget(targets: TourTargets, step: TourStep): Modifier =
    onGloballyPositioned { targets.report(step, it.boundsInRoot()) }
