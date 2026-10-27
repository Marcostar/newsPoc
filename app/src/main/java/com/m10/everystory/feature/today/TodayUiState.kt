package com.m10.everystory.feature.today

import com.m10.everystory.core.common.AppError
import com.m10.everystory.core.ui.LoadMoreState
import com.m10.everystory.domain.model.Article
import com.m10.everystory.domain.model.NewsCategory
import com.m10.everystory.domain.model.NewsLanguage
import java.time.Instant

data class TodayUiState(
    // True until the very first load finishes; drives the branded launch screen.
    val isLaunching: Boolean = true,
    val newsLanguage: NewsLanguage = NewsLanguage.Default,
    val selectedCategory: NewsCategory? = null,
    val articles: List<Article> = emptyList(),
    val feedStatus: FeedStatus = FeedStatus.Loading,
    val isRefreshing: Boolean = false,
    val lastRefresh: RefreshOutcome? = null,
    val updatedAt: Instant? = null,
    val loadMore: LoadMoreState = LoadMoreState.Idle,
    val isOffline: Boolean = false,
    // The reader pasted their own key on the developer screen; a 401 then means that key.
    val usingOwnKey: Boolean = false,
)

sealed interface FeedStatus {
    data object Loading : FeedStatus
    data object Loaded : FeedStatus
    data class Failed(val error: AppError) : FeedStatus
}

sealed interface RefreshOutcome {
    data class Updated(val newStories: Int) : RefreshOutcome
    data class Failed(val error: AppError) : RefreshOutcome
}
