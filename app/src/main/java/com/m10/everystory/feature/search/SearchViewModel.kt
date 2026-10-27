package com.m10.everystory.feature.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.m10.everystory.EveryStoryApplication
import com.m10.everystory.core.common.AppResult
import com.m10.everystory.core.ui.LoadMoreState
import com.m10.everystory.domain.repository.NewsRepository
import com.m10.everystory.domain.repository.RecentSearchesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val SEARCH_DEBOUNCE_MS = 300L
const val MIN_QUERY_LENGTH = 2
private const val QUERY_KEY = "query"

class SearchViewModel(
    private val newsRepository: NewsRepository,
    private val recentSearchesRepository: RecentSearchesRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState(query = savedStateHandle[QUERY_KEY] ?: ""))
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var loadMoreJob: Job? = null
    private var currentPage = 0

    init {
        viewModelScope.launch {
            recentSearchesRepository.recentSearches.collect { recent ->
                _uiState.update { it.copy(recentSearches = recent) }
            }
        }
        // Coming back after process death: run the query that was on screen.
        _uiState.value.query.trim().takeIf { it.length >= MIN_QUERY_LENGTH }?.let(::searchNow)
    }

    fun onQueryChange(query: String) {
        savedStateHandle[QUERY_KEY] = query
        _uiState.update { it.copy(query = query) }

        val trimmed = query.trim()
        if (trimmed.length < MIN_QUERY_LENGTH) {
            searchJob?.cancel()
            loadMoreJob?.cancel()
            _uiState.update { it.copy(results = SearchResults.Idle, searchedQuery = "", articles = emptyList()) }
            return
        }
        if (trimmed == _uiState.value.searchedQuery && _uiState.value.results !is SearchResults.Failed) {
            searchJob?.cancel()
            return
        }

        // Wait until typing pauses so every keystroke doesn't spend part of the daily quota.
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            runSearch(trimmed)
        }
    }

    fun onSubmit() {
        val trimmed = _uiState.value.query.trim()
        if (trimmed.length < MIN_QUERY_LENGTH) return
        rememberQuery(trimmed)
        // If the pause already started this exact search, let it finish rather than paying for it twice.
        if (trimmed == _uiState.value.searchedQuery && _uiState.value.results !is SearchResults.Failed) return
        searchNow(trimmed)
    }

    fun onRecentSearchClick(query: String) {
        onQueryChange(query)
        onSubmit()
    }

    fun onClear() = onQueryChange("")

    fun onRetry() {
        val query = _uiState.value.searchedQuery.ifEmpty { _uiState.value.query.trim() }
        if (query.length >= MIN_QUERY_LENGTH) searchNow(query)
    }

    // Opening a result is a good sign the query was worth keeping.
    fun onResultOpened() {
        _uiState.value.searchedQuery.takeIf { it.isNotEmpty() }?.let(::rememberQuery)
    }

    fun onLoadMore() {
        val state = _uiState.value
        if (state.results != SearchResults.Loaded || state.loadMore != LoadMoreState.Idle) return

        val query = state.searchedQuery
        val nextPage = currentPage + 1
        _uiState.update { it.copy(loadMore = LoadMoreState.Loading) }
        loadMoreJob = viewModelScope.launch {
            when (val result = newsRepository.searchArticles(query = query, page = nextPage)) {
                is AppResult.Success -> {
                    currentPage = nextPage
                    _uiState.update {
                        val known = it.articles.mapTo(HashSet()) { article -> article.key }
                        it.copy(
                            articles = it.articles + result.data.articles.filter { article -> article.key !in known },
                            loadMore = if (result.data.isLastPage) LoadMoreState.EndReached else LoadMoreState.Idle,
                        )
                    }
                }
                is AppResult.Failure -> _uiState.update { it.copy(loadMore = LoadMoreState.Failed(result.error)) }
            }
        }
    }

    fun onRetryLoadMore() {
        if (_uiState.value.loadMore !is LoadMoreState.Failed) return
        _uiState.update { it.copy(loadMore = LoadMoreState.Idle) }
        onLoadMore()
    }

    private fun searchNow(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch { runSearch(query) }
    }

    private suspend fun runSearch(query: String) {
        loadMoreJob?.cancel()
        _uiState.update {
            it.copy(
                results = SearchResults.Loading,
                searchedQuery = query,
                articles = emptyList(),
                loadMore = LoadMoreState.Idle,
            )
        }
        when (val result = newsRepository.searchArticles(query = query, page = 1)) {
            is AppResult.Success -> {
                currentPage = 1
                _uiState.update {
                    it.copy(
                        results = SearchResults.Loaded,
                        articles = result.data.articles,
                        totalResults = result.data.totalResults,
                        loadMore = if (result.data.isLastPage) LoadMoreState.EndReached else LoadMoreState.Idle,
                    )
                }
            }
            is AppResult.Failure -> _uiState.update { it.copy(results = SearchResults.Failed(result.error)) }
        }
    }

    private fun rememberQuery(query: String) {
        viewModelScope.launch { recentSearchesRepository.add(query) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EveryStoryApplication
                SearchViewModel(
                    newsRepository = app.container.newsRepository,
                    recentSearchesRepository = app.container.recentSearchesRepository,
                    savedStateHandle = createSavedStateHandle(),
                )
            }
        }
    }
}
