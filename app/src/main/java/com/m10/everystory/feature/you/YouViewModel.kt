package com.m10.everystory.feature.you

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.m10.everystory.EveryStoryApplication
import com.m10.everystory.domain.model.ThemeSetting
import com.m10.everystory.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class YouUiState(
    val themeSetting: ThemeSetting = ThemeSetting.System,
)

class YouViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<YouUiState> = userPreferencesRepository.themeSetting
        .map { YouUiState(themeSetting = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), YouUiState())

    // The whole app re-themes from the same preference, so there's nothing else to update here.
    fun onThemeSelected(setting: ThemeSetting) {
        viewModelScope.launch { userPreferencesRepository.setThemeSetting(setting) }
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EveryStoryApplication
                YouViewModel(app.container.userPreferencesRepository)
            }
        }
    }
}
