package com.m10.everystory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.m10.everystory.core.analytics.AnalyticsEvent
import com.m10.everystory.core.analytics.AnalyticsScreen
import com.m10.everystory.core.analytics.AnalyticsTracker
import com.m10.everystory.domain.model.TextSize
import com.m10.everystory.domain.model.ThemeSetting
import com.m10.everystory.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

// What the whole app is drawn with, chosen by the reader.
data class Appearance(
    val themeSetting: ThemeSetting = ThemeSetting.System,
    val textScale: Float = TextSize.scaleOf(TextSize.DEFAULT_STEP),
)

class MainViewModel(
    userPreferencesRepository: UserPreferencesRepository,
    private val analytics: AnalyticsTracker,
) : ViewModel() {

    // Null until DataStore answers; the splash stays up until then so the first frame is drawn
    // in the right theme and at the right text size.
    val appearance: StateFlow<Appearance?> =
        combine(userPreferencesRepository.themeSetting, userPreferencesRepository.textSizeStep) { theme, step ->
            Appearance(themeSetting = theme, textScale = TextSize.scaleOf(step))
        }.stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

    fun onScreenViewed(screen: AnalyticsScreen) {
        analytics.log(AnalyticsEvent.ScreenView(screen))
    }

    fun onNotificationOpened() = analytics.log(AnalyticsEvent.NotificationOpen)

    fun onAdImpression() = analytics.log(AnalyticsEvent.AdImpression)

    fun onAdClick() = analytics.log(AnalyticsEvent.AdClick)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EveryStoryApplication
                MainViewModel(app.container.userPreferencesRepository, app.container.analyticsTracker)
            }
        }
    }
}
