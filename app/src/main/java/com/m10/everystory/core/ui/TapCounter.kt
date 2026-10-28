package com.m10.everystory.core.ui

import android.os.SystemClock
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

// The handoff's easter eggs: three taps, each within 700ms of the last.
const val TRIPLE_TAP_WINDOW_MS = 700L

/**
 * Counts quick taps towards a hidden action. A tap that comes too long after the previous one
 * starts the count again. [taps] is Compose state, so a screen can show how many are left.
 */
@Stable
class TapCounter(
    private val target: Int = 3,
    private val windowMillis: Long = TRIPLE_TAP_WINDOW_MS,
    private val now: () -> Long = SystemClock::uptimeMillis,
) {
    var taps by mutableIntStateOf(0)
        private set

    val tapsLeft: Int get() = target - taps

    private var lastTapAt = 0L

    // True on the tap that completes the run; the count then starts again from zero.
    fun tap(): Boolean {
        val time = now()
        taps = if (taps > 0 && time - lastTapAt < windowMillis) taps + 1 else 1
        lastTapAt = time
        if (taps < target) return false
        taps = 0
        return true
    }

    fun reset() {
        taps = 0
    }
}
