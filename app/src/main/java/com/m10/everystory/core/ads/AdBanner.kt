package com.m10.everystory.core.ads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.m10.everystory.R
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EsSpace
import com.m10.everystory.core.designsystem.theme.EsTheme
import timber.log.Timber

// Google's sample adaptive-banner unit: always fills with a test ad and is safe to tap.
// Swap for the real unit ID at release.
private const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"

/**
 * The anchored banner that sits above the bottom nav, labelled "Ad" so it never passes for a
 * story. If no ad comes back, the bar goes away rather than leaving an empty strip.
 */
@Composable
fun AdBanner(
    onImpression: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var failed by remember { mutableStateOf(false) }
    if (failed) return

    val adDescription = stringResource(R.string.ad_description)
    Column(modifier = modifier.fillMaxWidth().background(EsTheme.colors.adBg)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EsSpace.s8),
            modifier = Modifier
                .heightIn(min = EsSize.adBar)
                .padding(start = EsSpace.s12),
        ) {
            Text(
                text = stringResource(R.string.ad_label),
                style = MaterialTheme.typography.labelMedium,
                color = EsTheme.colors.muted,
                modifier = Modifier.semantics { contentDescription = adDescription },
            )
            // Adaptive banners pick their height from the width they're given.
            BoxWithConstraints(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                BannerView(
                    widthDp = maxWidth.value.toInt(),
                    onImpression = onImpression,
                    onClick = onClick,
                    onFailed = { failed = true },
                )
            }
        }
    }
}

@Composable
private fun BannerView(
    widthDp: Int,
    onImpression: () -> Unit,
    onClick: () -> Unit,
    onFailed: () -> Unit,
) {
    val context = LocalContext.current
    val currentOnImpression by rememberUpdatedState(onImpression)
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnFailed by rememberUpdatedState(onFailed)

    val adView = remember(widthDp) {
        AdView(context).apply {
            setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp))
            adUnitId = BANNER_AD_UNIT_ID
            adListener = object : AdListener() {
                override fun onAdImpression() = currentOnImpression()
                override fun onAdClicked() = currentOnClick()
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Timber.w("Banner failed to load: %d %s", error.code, error.message)
                    currentOnFailed()
                }
            }
            loadAd(AdRequest.Builder().build())
        }
    }
    // The ad SDK keeps timers and a WebView running; pause them in the background, free them on exit.
    LifecycleResumeEffect(adView) {
        adView.resume()
        onPauseOrDispose { adView.pause() }
    }
    DisposableEffect(adView) {
        onDispose { adView.destroy() }
    }
    AndroidView(factory = { adView })
}
