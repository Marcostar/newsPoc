package com.m10.everystory.feature.search

import androidx.lifecycle.SavedStateHandle
import com.m10.everystory.core.common.AppError
import com.m10.everystory.core.common.AppResult
import com.m10.everystory.core.ui.LoadMoreState
import com.m10.everystory.testing.FakeNewsRepository
import com.m10.everystory.testing.FakeRecentSearchesRepository
import com.m10.everystory.testing.MainDispatcherRule
import com.m10.everystory.testing.testPage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val news = FakeNewsRepository()
    private val recent = FakeRecentSearchesRepository()

    private fun createViewModel(savedState: SavedStateHandle = SavedStateHandle()) =
        SearchViewModel(news, recent, savedState)

    @Test
    fun `typing waits for a pause before searching`() = runTest {
        news.searchResults += testPage(1, listOf("a"))
        val viewModel = createViewModel()

        viewModel.onQueryChange("levee")
        advanceTimeBy(SEARCH_DEBOUNCE_MS - 1)
        runCurrent()
        assertTrue(news.searchRequests.isEmpty())

        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf("levee" to 1), news.searchRequests)
        assertEquals(SearchResults.Loaded, viewModel.uiState.value.results)
    }

    @Test
    fun `only the last query in a burst of typing is sent`() = runTest {
        news.searchResults += testPage(1, listOf("a"))
        val viewModel = createViewModel()

        viewModel.onQueryChange("le")
        advanceTimeBy(100)
        viewModel.onQueryChange("lev")
        advanceTimeBy(100)
        viewModel.onQueryChange("levee")
        advanceUntilIdle()

        assertEquals(listOf("levee" to 1), news.searchRequests)
    }

    @Test
    fun `fewer than two characters shows recent searches without a request`() = runTest {
        val viewModel = createViewModel()

        viewModel.onQueryChange("l")
        advanceUntilIdle()

        assertTrue(news.searchRequests.isEmpty())
        assertEquals(SearchResults.Idle, viewModel.uiState.value.results)
    }

    @Test
    fun `enter searches at once and remembers the query`() = runTest {
        news.searchResults += testPage(1, listOf("a"))
        val viewModel = createViewModel()

        viewModel.onQueryChange("  night shift ")
        viewModel.onSubmit()
        runCurrent()

        assertEquals(listOf("night shift" to 1), news.searchRequests)
        advanceUntilIdle()
        assertEquals(listOf("night shift"), viewModel.uiState.value.recentSearches)
        assertEquals(1, news.searchRequests.size)
    }

    @Test
    fun `the same query is not searched twice`() = runTest {
        news.searchResults += testPage(1, listOf("a"))
        val viewModel = createViewModel()
        viewModel.onQueryChange("levee")
        advanceUntilIdle()

        viewModel.onQueryChange("levee ")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(1, news.searchRequests.size)
    }

    @Test
    fun `enter during an in-flight search for the same text does not restart it`() = runTest {
        news.searchResults += testPage(1, listOf("a"))
        val viewModel = createViewModel()
        viewModel.onQueryChange("levee")
        advanceTimeBy(SEARCH_DEBOUNCE_MS)
        runCurrent()
        assertEquals(1, news.searchRequests.size)

        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(1, news.searchRequests.size)
        assertEquals(listOf("levee"), viewModel.uiState.value.recentSearches)
    }

    @Test
    fun `clear goes back to recent searches`() = runTest {
        news.searchResults += testPage(1, listOf("a"))
        val viewModel = createViewModel()
        viewModel.onQueryChange("levee")
        advanceUntilIdle()

        viewModel.onClear()

        val state = viewModel.uiState.value
        assertEquals("", state.query)
        assertEquals(SearchResults.Idle, state.results)
        assertTrue(state.articles.isEmpty())
    }

    @Test
    fun `no matches is a loaded state with no articles`() = runTest {
        news.searchResults += testPage(1, emptyList(), isLastPage = true)
        val viewModel = createViewModel()

        viewModel.onQueryChange("zzqx")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(SearchResults.Loaded, state.results)
        assertTrue(state.articles.isEmpty())
        assertEquals("zzqx", state.searchedQuery)
    }

    @Test
    fun `a failed search can be retried`() = runTest {
        news.searchResults += AppResult.Failure(AppError.Offline)
        news.searchResults += testPage(1, listOf("a"))
        val viewModel = createViewModel()
        viewModel.onQueryChange("levee")
        advanceUntilIdle()
        assertEquals(SearchResults.Failed(AppError.Offline), viewModel.uiState.value.results)

        viewModel.onRetry()
        advanceUntilIdle()

        assertEquals(SearchResults.Loaded, viewModel.uiState.value.results)
        assertEquals(listOf("levee" to 1, "levee" to 1), news.searchRequests)
    }

    @Test
    fun `load more pages through the same query`() = runTest {
        news.searchResults += testPage(1, listOf("a", "b"))
        news.searchResults += testPage(2, listOf("c"), isLastPage = true)
        val viewModel = createViewModel()
        viewModel.onQueryChange("levee")
        advanceUntilIdle()

        viewModel.onLoadMore()
        advanceUntilIdle()

        assertEquals(listOf("a", "b", "c"), viewModel.uiState.value.articles.map { it.key })
        assertEquals(LoadMoreState.EndReached, viewModel.uiState.value.loadMore)
        assertEquals("levee" to 2, news.searchRequests.last())
    }

    @Test
    fun `query survives process death and reruns`() = runTest {
        // The first view model's own debounced search still runs here, so answer both.
        news.searchResults += testPage(1, listOf("a"))
        news.searchResults += testPage(1, listOf("a"))
        val savedState = SavedStateHandle()
        createViewModel(savedState).onQueryChange("levee")

        val restored = createViewModel(savedState)
        advanceUntilIdle()

        assertEquals("levee", restored.uiState.value.query)
        assertEquals(SearchResults.Loaded, restored.uiState.value.results)
    }
}
