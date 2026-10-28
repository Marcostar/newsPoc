package com.m10.everystory.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.m10.everystory.EveryStoryApplication
import com.m10.everystory.core.analytics.AnalyticsEvent
import com.m10.everystory.core.analytics.AnalyticsScreen
import com.m10.everystory.core.analytics.AnalyticsTracker
import com.m10.everystory.core.common.AppResult
import com.m10.everystory.core.network.NetworkMonitor
import com.m10.everystory.core.ui.LoadMoreState
import com.m10.everystory.domain.model.ArticlePage
import com.m10.everystory.domain.model.NewsCategory
import com.m10.everystory.domain.model.NewsLanguage
import com.m10.everystory.domain.repository.ApiKeyRepository
import com.m10.everystory.domain.repository.NewsRepository
import com.m10.everystory.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

// How long a cached feed counts as current when Today opens it.
private val FRESH_FOR: Duration = Duration.ofMinutes(15)

class TodayViewModel(
    private val newsRepository: NewsRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val networkMonitor: NetworkMonitor,
    private val apiKeyRepository: ApiKeyRepository,
    private val analytics: AnalyticsTracker,
    private val now: () -> Instant = Instant::now,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    private var feedJob: Job? = null
    private var loadMoreJob: Job? = null
    private var currentPage = 0

    init {
        // The first value starts the feed; a new language later starts it over on All.
        // distinctUntilChanged because DataStore re-emits when any other preference changes.
        viewModelScope.launch {
            userPreferencesRepository.newsLanguage.distinctUntilChanged().collect { language ->
                _uiState.update {
                    it.copy(
                        newsLanguage = language,
                        selectedCategory = null,
                        articles = emptyList(),
                        feedStatus = FeedStatus.Loading,
                        lastRefresh = null,
                        loadMore = LoadMoreState.Idle,
                    )
                }
                loadFirstPage()
            }
        }
        // Only changes the status line; coming back online never reloads by itself.
        viewModelScope.launch {
            networkMonitor.isOnline.collect { online -> _uiState.update { it.copy(isOffline = !online) } }
        }
        viewModelScope.launch {
            apiKeyRepository.ownKey.collect { key -> _uiState.update { it.copy(usingOwnKey = key != null) } }
        }
    }

    fun onLanguageSelected(language: NewsLanguage) {
        if (language == _uiState.value.newsLanguage) return
        analytics.log(AnalyticsEvent.LanguageChange(language))
        viewModelScope.launch { userPreferencesRepository.setNewsLanguage(language) }
    }

    fun onCategorySelected(category: NewsCategory?) {
        if (category == _uiState.value.selectedCategory) return
        analytics.log(AnalyticsEvent.TopicSelect(category))
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
        analytics.log(AnalyticsEvent.FeedRefresh)
        if (state.articles.isEmpty()) return onRetry()

        cancelLoading()
        val previousKeys = state.articles.mapTo(HashSet()) { it.key }
        // The list stays on screen while refreshing; only the status line changes.
        _uiState.update { it.copy(isRefreshing = true, loadMore = it.loadMore.settled()) }

        feedJob = viewModelScope.launch {
            refreshFirstPage(state.selectedCategory, state.newsLanguage, previousKeys)
        }
    }

    // Offered when NewsAPI turns down the reader's own key: drop it and try again with ours.
    fun onUseBuiltInKey() {
        analytics.log(AnalyticsEvent.OwnApiKey(saved = false))
        viewModelScope.launch {
            apiKeyRepository.removeOwnKey()
            onRefresh()
        }
    }

    fun onLoadMore() {
        val state = _uiState.value
        if (state.feedStatus != FeedStatus.Loaded || state.isRefreshing || state.loadMore != LoadMoreState.Idle) return

        val nextPage = currentPage + 1
        _uiState.update { it.copy(loadMore = LoadMoreState.Loading) }
        loadMoreJob = viewModelScope.launch {
            when (val result = newsRepository.topHeadlines(page = nextPage, category = state.selectedCategory, language = state.newsLanguage)) {
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
        val state = _uiState.value
        feedJob = viewModelScope.launch {
            // Offline-first: show this feed as it was last time straight away, then check for new stories.
            val cached = newsRepository.cachedHeadlines(category = state.selectedCategory, language = state.newsLanguage)
            if (cached != null) {
                // A feed fetched a few minutes ago is still news; don't spend a request (the free plan
                // allows 50 per 12 hours) just because the app was reopened or a topic tapped again.
                // Refresh and pull to refresh always ask NewsAPI.
                val isFresh = Duration.between(cached.updatedAt, now()) < FRESH_FOR
                currentPage = 1
                _uiState.update {
                    it.copy(
                        isLaunching = false,
                        articles = cached.articles,
                        feedStatus = FeedStatus.Loaded,
                        isRefreshing = !isFresh,
                        updatedAt = cached.updatedAt,
                        loadMore = LoadMoreState.Idle,
                    )
                }
                if (!isFresh) {
                    refreshFirstPage(state.selectedCategory, state.newsLanguage, cached.articles.mapTo(HashSet()) { it.key })
                }
                return@launch
            }
            when (val result = newsRepository.topHeadlines(page = 1, category = state.selectedCategory, language = state.newsLanguage)) {
                is AppResult.Success -> showFirstPage(result.data, refresh = null)
                is AppResult.Failure -> {
                    analytics.log(AnalyticsEvent.LoadError(AnalyticsScreen.Today, result.error))
                    _uiState.update {
                        it.copy(isLaunching = false, feedStatus = FeedStatus.Failed(result.error))
                    }
                }
            }
        }
    }

    // Swaps in page one if it arrives; if not, the stories already on screen stay put.
    private suspend fun refreshFirstPage(category: NewsCategory?, language: NewsLanguage, previousKeys: Set<String>) {
        when (val result = newsRepository.topHeadlines(page = 1, category = category, language = language)) {
            is AppResult.Success -> {
                val newStories = result.data.articles.count { it.key !in previousKeys }
                showFirstPage(result.data, RefreshOutcome.Updated(newStories))
            }
            is AppResult.Failure -> {
                analytics.log(AnalyticsEvent.LoadError(AnalyticsScreen.Today, result.error))
                _uiState.update {
                    it.copy(isRefreshing = false, lastRefresh = RefreshOutcome.Failed(result.error))
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
                TodayViewModel(
                    newsRepository = app.container.newsRepository,
                    userPreferencesRepository = app.container.userPreferencesRepository,
                    networkMonitor = app.container.networkMonitor,
                    apiKeyRepository = app.container.apiKeyRepository,
                    analytics = app.container.analyticsTracker,
                )
            }
        }
    }
}
