package com.m10.everystory.feature.display

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.m10.everystory.EveryStoryApplication
import com.m10.everystory.core.analytics.AnalyticsEvent
import com.m10.everystory.core.analytics.AnalyticsTracker
import com.m10.everystory.domain.model.TextSize
import com.m10.everystory.domain.model.ThemeSetting
import com.m10.everystory.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DisplaySettingsUiState(
    val textSizeStep: Int = TextSize.DEFAULT_STEP,
    val themeSetting: ThemeSetting = ThemeSetting.System,
)

class DisplaySettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val analytics: AnalyticsTracker,
) : ViewModel() {

    val uiState: StateFlow<DisplaySettingsUiState> =
        combine(userPreferencesRepository.textSizeStep, userPreferencesRepository.themeSetting) { step, theme ->
            DisplaySettingsUiState(textSizeStep = step, themeSetting = theme)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DisplaySettingsUiState())

    // The theme re-reads the same preference, so the whole app, this sheet included, resizes live.
    fun onTextSizeChange(step: Int) {
        val clamped = step.coerceIn(TextSize.scales.indices)
        if (clamped == uiState.value.textSizeStep) return
        analytics.log(AnalyticsEvent.TextSizeChange(clamped))
        viewModelScope.launch { userPreferencesRepository.setTextSizeStep(clamped) }
    }

    fun onThemeSelected(setting: ThemeSetting) {
        if (setting == uiState.value.themeSetting) return
        analytics.log(AnalyticsEvent.ThemeChange(setting))
        viewModelScope.launch { userPreferencesRepository.setThemeSetting(setting) }
    }

    fun onReset() {
        onTextSizeChange(TextSize.DEFAULT_STEP)
        onThemeSelected(ThemeSetting.System)
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EveryStoryApplication
                DisplaySettingsViewModel(app.container.userPreferencesRepository, app.container.analyticsTracker)
            }
        }
    }
}
