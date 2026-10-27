package com.m10.everystory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.m10.everystory.core.analytics.AnalyticsEvent
import com.m10.everystory.core.analytics.AnalyticsScreen
import com.m10.everystory.core.analytics.AnalyticsTracker
import com.m10.everystory.domain.model.ThemeSetting
import com.m10.everystory.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    userPreferencesRepository: UserPreferencesRepository,
    private val analytics: AnalyticsTracker,
) : ViewModel() {

    // Null until DataStore answers; the splash stays up until then so the first frame is in the right theme.
    val themeSetting: StateFlow<ThemeSetting?> = userPreferencesRepository.themeSetting
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

    fun onScreenViewed(screen: AnalyticsScreen) {
        analytics.log(AnalyticsEvent.ScreenView(screen))
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EveryStoryApplication
                MainViewModel(app.container.userPreferencesRepository, app.container.analyticsTracker)
            }
        }
    }
}
