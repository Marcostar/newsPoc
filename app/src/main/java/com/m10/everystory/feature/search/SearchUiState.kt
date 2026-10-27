package com.m10.everystory.feature.search

import com.m10.everystory.core.common.AppError
import com.m10.everystory.core.ui.LoadMoreState
import com.m10.everystory.domain.model.Article

data class SearchUiState(
    val query: String = "",
    val recentSearches: List<String> = emptyList(),
    val results: SearchResults = SearchResults.Idle,
    // The query the results on screen belong to; it lags `query` while the reader is typing.
    val searchedQuery: String = "",
    val articles: List<Article> = emptyList(),
    val totalResults: Int = 0,
    val loadMore: LoadMoreState = LoadMoreState.Idle,
    val usingOwnKey: Boolean = false,
)

sealed interface SearchResults {
    data object Idle : SearchResults
    data object Loading : SearchResults
    data object Loaded : SearchResults
    data class Failed(val error: AppError) : SearchResults
}
