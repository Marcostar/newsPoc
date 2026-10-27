package com.m10.everystory.feature.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.m10.everystory.EveryStoryApplication
import com.m10.everystory.domain.model.Article
import com.m10.everystory.domain.repository.SavedArticlesRepository
import com.m10.everystory.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant

data class SavedUiState(
    val isLoading: Boolean = true,
    val articles: List<Article> = emptyList(),
)

class SavedViewModel(
    savedArticlesRepository: SavedArticlesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val now: () -> Instant = Instant::now,
) : ViewModel() {

    // Room re-emits on every save or removal, so the list stays current without a refresh.
    val uiState: StateFlow<SavedUiState> = savedArticlesRepository.savedArticles
        .map { SavedUiState(isLoading = false, articles = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), SavedUiState())

    // Looking at Saved counts as having seen what's in it, so the reminder won't nag about those.
    fun onShown() {
        viewModelScope.launch { userPreferencesRepository.setSavedLastViewedAt(now()) }
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EveryStoryApplication
                SavedViewModel(app.container.savedArticlesRepository, app.container.userPreferencesRepository)
            }
        }
    }
}
