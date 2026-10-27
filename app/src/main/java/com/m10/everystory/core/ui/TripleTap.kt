package com.m10.everystory.core.ui

import android.os.SystemClock
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

// The handoff's easter eggs: three taps within 700ms of each other. The logo on Today opens the
// developer settings, the version row on You opens the profile sheet.
private const val TRIPLE_TAP_WINDOW_MS = 700L

@Composable
fun Modifier.onTripleTap(onTripleTap: () -> Unit): Modifier {
    // Keyed once, so a recomposition between taps doesn't reset the count.
    val currentOnTripleTap by rememberUpdatedState(onTripleTap)
    return pointerInput(Unit) {
        var taps = 0
        var lastTapAt = 0L
        detectTapGestures {
            val now = SystemClock.uptimeMillis()
            taps = if (now - lastTapAt < TRIPLE_TAP_WINDOW_MS) taps + 1 else 1
            lastTapAt = now
            if (taps == 3) {
                taps = 0
                currentOnTripleTap()
            }
        }
    }
}
