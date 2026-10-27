package com.m10.everystory.core.analytics

// ViewModels depend on this, not on Firebase, so tests can use a fake that records events.
interface AnalyticsTracker {
    fun log(event: AnalyticsEvent)
}
