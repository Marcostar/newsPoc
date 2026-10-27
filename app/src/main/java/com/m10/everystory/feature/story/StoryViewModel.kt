package com.m10.everystory.feature.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.m10.everystory.EveryStoryApplication
import com.m10.everystory.domain.model.Article
import com.m10.everystory.domain.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface StoryUiState {
    data object Loading : StoryUiState
    data class Ready(val article: Article) : StoryUiState
    data object NotFound : StoryUiState
}

class StoryViewModel(
    private val articleKey: String,
    private val newsRepository: NewsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<StoryUiState>(StoryUiState.Loading)
    val uiState: StateFlow<StoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = newsRepository.article(articleKey)
                ?.let { StoryUiState.Ready(it) }
                ?: StoryUiState.NotFound
        }
    }

    companion object {
        fun factory(articleKey: String) = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EveryStoryApplication
                StoryViewModel(articleKey, app.container.newsRepository)
            }
        }
    }
}
