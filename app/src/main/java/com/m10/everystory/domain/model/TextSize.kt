package com.m10.everystory.domain.model

// The in-app text size, like Kindle: seven steps on top of the phone's own font size.
object TextSize {
    val scales = listOf(0.85f, 0.92f, 1f, 1.12f, 1.25f, 1.4f, 1.6f)
    const val DEFAULT_STEP = 2 // 100%

    fun scaleOf(step: Int): Float = scales[step.coerceIn(scales.indices)]
}
