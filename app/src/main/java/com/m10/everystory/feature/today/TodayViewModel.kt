package com.m10.everystory.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.m10.everystory.EveryStoryApplication
import com.m10.everystory.core.common.AppResult
import com.m10.everystory.core.ui.LoadMoreState
import com.m10.everystory.domain.model.ArticlePage
import com.m10.everystory.domain.model.NewsCategory
import com.m10.everystory.domain.repository.NewsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

class TodayViewModel(
    private val newsRepository: NewsRepository,
    private val now: () -> Instant = Instant::now,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    private var feedJob: Job? = null
    private var loadMoreJob: Job? = null
    private var currentPage = 0

    init {
        loadFirstPage()
    }

    fun onCategorySelected(category: NewsCategory?) {
        if (category == _uiState.value.selectedCategory) return
        _uiState.update {
            it.copy(
                selectedCategory = category,
                articles = emptyList(),
                feedStatus = FeedStatus.Loading,
                lastRefresh = null,
                loadMore = LoadMoreState.Idle,
            )
        }
        loadFirstPage()
    }

    fun onRetry() {
        _uiState.update { it.copy(feedStatus = FeedStatus.Loading) }
        loadFirstPage()
    }

    fun onRefresh() {
        val state = _uiState.value
        if (state.isRefreshing) return
        if (state.articles.isEmpty()) return onRetry()

        cancelLoading()
        val previousKeys = state.articles.mapTo(HashSet()) { it.key }
        // The list stays on screen while refreshing; only the status line changes.
        _uiState.update { it.copy(isRefreshing = true, loadMore = it.loadMore.settled()) }

        feedJob = viewModelScope.launch {
            when (val result = newsRepository.topHeadlines(page = 1, category = state.selectedCategory)) {
                is AppResult.Success -> {
                    val newStories = result.data.articles.count { it.key !in previousKeys }
                    showFirstPage(result.data, RefreshOutcome.Updated(newStories))
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(isRefreshing = false, lastRefresh = RefreshOutcome.Failed(result.error))
                }
            }
        }
    }

    fun onLoadMore() {
        val state = _uiState.value
        if (state.feedStatus != FeedStatus.Loaded || state.isRefreshing || state.loadMore != LoadMoreState.Idle) return

        val nextPage = currentPage + 1
        _uiState.update { it.copy(loadMore = LoadMoreState.Loading) }
        loadMoreJob = viewModelScope.launch {
            when (val result = newsRepository.topHeadlines(page = nextPage, category = state.selectedCategory)) {
                is AppResult.Success -> {
                    currentPage = nextPage
                    _uiState.update {
                        // NewsAPI can repeat a story across pages; keep the first copy.
                        val known = it.articles.mapTo(HashSet()) { article -> article.key }
                        it.copy(
                            articles = it.articles + result.data.articles.filter { article -> article.key !in known },
                            loadMore = result.data.loadMoreState(),
                        )
                    }
                }
                // Stop and wait for Try again; never retry in a loop.
                is AppResult.Failure -> _uiState.update { it.copy(loadMore = LoadMoreState.Failed(result.error)) }
            }
        }
    }

    fun onRetryLoadMore() {
        if (_uiState.value.loadMore !is LoadMoreState.Failed) return
        _uiState.update { it.copy(loadMore = LoadMoreState.Idle) }
        onLoadMore()
    }

    private fun loadFirstPage() {
        cancelLoading()
        val category = _uiState.value.selectedCategory
        feedJob = viewModelScope.launch {
            when (val result = newsRepository.topHeadlines(page = 1, category = category)) {
                is AppResult.Success -> showFirstPage(result.data, refresh = null)
                is AppResult.Failure -> _uiState.update {
                    it.copy(isLaunching = false, feedStatus = FeedStatus.Failed(result.error))
                }
            }
        }
    }

    private fun showFirstPage(page: ArticlePage, refresh: RefreshOutcome?) {
        currentPage = 1
        _uiState.update {
            it.copy(
                isLaunching = false,
                articles = page.articles,
                feedStatus = FeedStatus.Loaded,
                isRefreshing = false,
                lastRefresh = refresh,
                updatedAt = now(),
                loadMore = page.loadMoreState(),
            )
        }
    }

    private fun cancelLoading() {
        feedJob?.cancel()
        loadMoreJob?.cancel()
    }

    private fun ArticlePage.loadMoreState(): LoadMoreState =
        if (isLastPage) LoadMoreState.EndReached else LoadMoreState.Idle

    private fun LoadMoreState.settled(): LoadMoreState =
        if (this == LoadMoreState.Loading) LoadMoreState.Idle else this

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EveryStoryApplication
                TodayViewModel(app.container.newsRepository)
            }
        }
    }
}
