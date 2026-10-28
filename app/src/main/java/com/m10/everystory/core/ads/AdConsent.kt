package com.m10.everystory.core.ads

import android.app.Activity
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.m10.everystory.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Asks for ad consent with Google's User Messaging Platform before any ad loads, following
 * Google's own consent-manager sample. The form only appears where the law needs it (EEA, UK,
 * some US states); everywhere else it answers straight away.
 */
class AdConsent(context: Context) {

    private val appContext = context.applicationContext
    // StrictMode flags a disk read here: UMP loading its stored consent. Google's API is main-thread
    // only and the answer is needed before the first frame, so it stays.
    private val consentInformation: ConsentInformation = UserMessagingPlatform.getConsentInformation(appContext)
    private val adsStarted = AtomicBoolean(false)

    // Consent from an earlier session counts at once, so returning readers see ads without waiting.
    private val _canShowAds = MutableStateFlow(consentInformation.canRequestAds())
    val canShowAds: StateFlow<Boolean> = _canShowAds.asStateFlow()

    fun gather(activity: ComponentActivity) {
        consentInformation.requestConsentInfoUpdate(
            activity,
            requestParameters(activity),
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    formError?.let { Timber.w("Consent form: %s", it.message) }
                    onConsentKnown(activity)
                }
            },
            { requestError ->
                // No answer (offline, say): fall back to whatever consent we already had.
                Timber.w("Consent info update: %s", requestError.message)
                onConsentKnown(activity)
            },
        )
        if (consentInformation.canRequestAds()) onConsentKnown(activity)
    }

    private fun onConsentKnown(activity: ComponentActivity) {
        if (!consentInformation.canRequestAds()) return
        _canShowAds.value = true
        // Google asks for initialize() off the main thread; it reads disk and can take a moment.
        if (adsStarted.compareAndSet(false, true)) {
            activity.lifecycleScope.launch(Dispatchers.IO) { MobileAds.initialize(appContext) }
        }
    }

    // Debug builds pretend to be in the EEA so the consent form can be seen from anywhere.
    private fun requestParameters(activity: Activity): ConsentRequestParameters =
        ConsentRequestParameters.Builder().apply {
            if (BuildConfig.DEBUG) {
                setConsentDebugSettings(
                    ConsentDebugSettings.Builder(activity)
                        .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                        // Applies the pretend geography on any phone, without registering it first.
                        .setForceTesting(true)
                        .build(),
                )
            }
        }.build()
}
