package com.m10.everystory.core.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

// A hidden triple tap with no visible feedback, for the logo on Today (developer settings).
// The version row on You counts with a TapCounter of its own and shows how many taps are left.
@Composable
fun Modifier.onTripleTap(onTripleTap: () -> Unit): Modifier {
    // Keyed once, so a recomposition between taps doesn't reset the count.
    val currentOnTripleTap by rememberUpdatedState(onTripleTap)
    val counter = remember { TapCounter() }
    return pointerInput(Unit) {
        detectTapGestures {
            if (counter.tap()) currentOnTripleTap()
        }
    }
}
