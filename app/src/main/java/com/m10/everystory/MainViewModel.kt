package com.m10.everystory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.m10.everystory.domain.model.ThemeSetting
import com.m10.everystory.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(userPreferencesRepository: UserPreferencesRepository) : ViewModel() {

    // Null until DataStore answers; the splash stays up until then so the first frame is in the right theme.
    val themeSetting: StateFlow<ThemeSetting?> = userPreferencesRepository.themeSetting
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EveryStoryApplication
                MainViewModel(app.container.userPreferencesRepository)
            }
        }
    }
}
