package com.m10.everystory.core.analytics

import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber

class FirebaseAnalyticsTracker(
    private val firebaseAnalytics: FirebaseAnalytics,
) : AnalyticsTracker {

    override fun log(event: AnalyticsEvent) {
        // Name only: params can hold what the user typed into search.
        Timber.d("Analytics event: %s", event.name)
        firebaseAnalytics.logEvent(event.name, bundleOf(*event.params.toList().toTypedArray()))
    }
}
