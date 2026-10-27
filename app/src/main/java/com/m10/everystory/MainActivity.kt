package com.m10.everystory

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.m10.everystory.core.designsystem.theme.EveryStoryTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        // The system splash shows the mark, then hands straight over to EsLaunchScreen.
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { viewModel.appearance.value == null }

        // The consent form needs an Activity, so the activity starts it; ads wait for the answer.
        val adConsent = (application as EveryStoryApplication).container.adConsent
        adConsent.gather(this)

        setContent {
            val appearance = viewModel.appearance.collectAsStateWithLifecycle().value ?: Appearance()
            val darkTheme = appearance.themeSetting.isDark(isSystemInDarkTheme())

            // Status bar icons follow the app's theme, which may differ from the phone's.
            DisposableEffect(darkTheme) {
                val style = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose {}
            }

            EveryStoryTheme(darkTheme = darkTheme, textScale = appearance.textScale) {
                val canShowAds by adConsent.canShowAds.collectAsStateWithLifecycle()
                EveryStoryApp(
                    onScreenViewed = viewModel::onScreenViewed,
                    showAds = canShowAds,
                    onAdImpression = viewModel::onAdImpression,
                    onAdClick = viewModel::onAdClick,
                )
            }
        }
    }
}
