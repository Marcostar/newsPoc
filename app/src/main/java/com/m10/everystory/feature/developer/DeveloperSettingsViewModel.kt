package com.m10.everystory.feature.developer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.m10.everystory.EveryStoryApplication
import com.m10.everystory.core.analytics.AnalyticsEvent
import com.m10.everystory.core.analytics.AnalyticsTracker
import com.m10.everystory.domain.repository.ApiKeyRepository
import com.m10.everystory.domain.usecase.SaveOwnApiKeyUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val KEY_ENDING_LENGTH = 4

// The typed key stays in memory only: it's a secret, so it never goes into SavedStateHandle.
class DeveloperSettingsViewModel(
    private val apiKeyRepository: ApiKeyRepository,
    private val saveOwnApiKey: SaveOwnApiKeyUseCase,
    private val analytics: AnalyticsTracker,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeveloperSettingsUiState())
    val uiState: StateFlow<DeveloperSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            apiKeyRepository.ownKey.collect { key ->
                _uiState.update { it.copy(ownKeyEnding = key?.takeLast(KEY_ENDING_LENGTH)) }
            }
        }
    }

    fun onInputChange(input: String) {
        _uiState.update { it.copy(input = input, inputError = null) }
    }

    fun onTestAndSave() {
        val state = _uiState.value
        if (state.isChecking) return
        _uiState.update { it.copy(isChecking = true, inputError = null) }
        viewModelScope.launch {
            val result = saveOwnApiKey(state.input)
            if (result == SaveOwnApiKeyUseCase.Result.Saved) analytics.log(AnalyticsEvent.OwnApiKey(saved = true))
            _uiState.update {
                when (result) {
                    SaveOwnApiKeyUseCase.Result.Saved ->
                        it.copy(isChecking = false, input = "", message = DeveloperMessage.KeySaved)
                    SaveOwnApiKeyUseCase.Result.Empty -> it.copy(isChecking = false, inputError = KeyInputError.Empty)
                    SaveOwnApiKeyUseCase.Result.WrongFormat -> it.copy(isChecking = false, inputError = KeyInputError.WrongFormat)
                    SaveOwnApiKeyUseCase.Result.Rejected -> it.copy(isChecking = false, inputError = KeyInputError.Rejected)
                    is SaveOwnApiKeyUseCase.Result.CouldNotCheck ->
                        it.copy(isChecking = false, inputError = KeyInputError.CouldNotCheck(result.error))
                }
            }
        }
    }

    fun onRemoveKey() {
        if (_uiState.value.ownKeyEnding == null) return
        analytics.log(AnalyticsEvent.OwnApiKey(saved = false))
        viewModelScope.launch {
            apiKeyRepository.removeOwnKey()
            _uiState.update { it.copy(message = DeveloperMessage.KeyRemoved) }
        }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EveryStoryApplication
                DeveloperSettingsViewModel(
                    apiKeyRepository = app.container.apiKeyRepository,
                    saveOwnApiKey = SaveOwnApiKeyUseCase(app.container.newsRepository, app.container.apiKeyRepository),
                    analytics = app.container.analyticsTracker,
                )
            }
        }
    }
}
