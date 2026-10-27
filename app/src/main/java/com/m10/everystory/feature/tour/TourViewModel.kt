package com.m10.everystory.feature.tour

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.m10.everystory.EveryStoryApplication
import com.m10.everystory.core.analytics.AnalyticsEvent
import com.m10.everystory.core.analytics.AnalyticsTracker
import com.m10.everystory.core.ui.TourStep
import com.m10.everystory.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// step is null while the tour isn't showing.
data class TourUiState(
    val step: TourStep? = null,
)

class TourViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val analytics: AnalyticsTracker,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TourUiState())
    val uiState: StateFlow<TourUiState> = _uiState.asStateFlow()

    init {
        // Shown once on first launch; finishing and skipping both count as seen.
        viewModelScope.launch {
            if (!userPreferencesRepository.tourSeen.first()) onStart()
        }
    }

    fun onStart() {
        _uiState.value = TourUiState(step = TourStep.entries.first())
    }

    fun onNext() {
        val step = _uiState.value.step ?: return
        val next = TourStep.entries.getOrNull(step.ordinal + 1)
        if (next != null) {
            _uiState.value = TourUiState(step = next)
        } else {
            analytics.log(AnalyticsEvent.TourComplete)
            finish()
        }
    }

    fun onBack() {
        val step = _uiState.value.step ?: return
        TourStep.entries.getOrNull(step.ordinal - 1)?.let { _uiState.value = TourUiState(step = it) }
    }

    fun onSkip() {
        val step = _uiState.value.step ?: return
        analytics.log(AnalyticsEvent.TourSkip(step))
        finish()
    }

    private fun finish() {
        _uiState.value = TourUiState()
        viewModelScope.launch { userPreferencesRepository.setTourSeen() }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EveryStoryApplication
                TourViewModel(app.container.userPreferencesRepository, app.container.analyticsTracker)
            }
        }
    }
}
