package com.m10.everystory.domain.model

// Read-aloud speeds from the design. The player's Speed button steps through them in order.
object ListenRate {
    val rates = listOf(0.8f, 1f, 1.25f, 1.5f)
    const val DEFAULT = 1f

    fun next(rate: Float): Float = rates[(rates.indexOf(rate) + 1) % rates.size]

    // Anything stored that isn't one of the steps (an older build, a hand-edited file) falls back to 1×.
    fun valid(rate: Float?): Float = rate?.takeIf { it in rates } ?: DEFAULT
}
