package com.m10.everystory.core.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.annotation.ColorInt
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import timber.log.Timber

// Opens the publisher's page in a Custom Tab, so Back returns straight to the story.
fun Context.openInCustomTab(url: String, @ColorInt toolbarColor: Int): Boolean {
    val colors = CustomTabColorSchemeParams.Builder().setToolbarColor(toolbarColor).build()
    val intent = CustomTabsIntent.Builder()
        .setDefaultColorSchemeParams(colors)
        .setShowTitle(true)
        .build()
    return try {
        intent.launchUrl(this, url.toUri())
        true
    } catch (e: ActivityNotFoundException) {
        Timber.w(e, "No browser available to open %s", url)
        false
    }
}

// Opens the reader's email app with the address filled in; false when there's no email app.
fun Context.composeEmail(address: String): Boolean {
    val intent = Intent(Intent.ACTION_SENDTO, "mailto:$address".toUri())
    return try {
        startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        Timber.w(e, "No email app available")
        false
    }
}
