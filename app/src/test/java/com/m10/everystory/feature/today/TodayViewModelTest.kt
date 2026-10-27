package com.m10.everystory.feature.today

import com.m10.everystory.core.analytics.AnalyticsEvent
import com.m10.everystory.core.analytics.AnalyticsScreen
import com.m10.everystory.core.common.AppError
import com.m10.everystory.core.common.AppResult
import com.m10.everystory.core.ui.LoadMoreState
import com.m10.everystory.domain.model.NewsCategory
import com.m10.everystory.testing.FakeAnalyticsTracker
import com.m10.everystory.testing.FakeNewsRepository
import com.m10.everystory.testing.MainDispatcherRule
import com.m10.everystory.testing.testPage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeNewsRepository()
    private val analytics = FakeAnalyticsTracker()
    private val fixedNow = Instant.parse("2026-10-26T09:30:00Z")

    private fun createViewModel() = TodayViewModel(repository, analytics, now = { fixedNow })

    @Test
    fun `first load shows headlines and ends the launch screen`() = runTest {
        repository.headlineResults += testPage(1, listOf("a", "b"))

        val viewModel = createViewModel()
        assertTrue(viewModel.uiState.value.isLaunching)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLaunching)
        assertEquals(FeedStatus.Loaded, state.feedStatus)
        assertEquals(listOf("a", "b"), state.articles.map { it.key })
        assertEquals(fixedNow, state.updatedAt)
    }

    @Test
    fun `first load failure still ends the launch screen and shows the error`() = runTest {
        repository.headlineResults += AppResult.Failure(AppError.Offline)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLaunching)
        assertEquals(FeedStatus.Failed(AppError.Offline), state.feedStatus)
    }

    @Test
    fun `load more appends the next page and drops repeats`() = runTest {
        repository.headlineResults += testPage(1, listOf("a", "b"))
        repository.headlineResults += testPage(2, listOf("b", "c"), isLastPage = true)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onLoadMore()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("a", "b", "c"), state.articles.map { it.key })
        assertEquals(LoadMoreState.EndReached, state.loadMore)
        assertEquals(2, repository.headlineRequests.last().first)
    }

    @Test
    fun `load more failure waits for try again instead of retrying`() = runTest {
        repository.headlineResults += testPage(1, listOf("a"))
        repository.headlineResults += AppResult.Failure(AppError.Server)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onLoadMore()
        advanceUntilIdle()
        viewModel.onLoadMore()
        advanceUntilIdle()

        assertEquals(LoadMoreState.Failed(AppError.Server), viewModel.uiState.value.loadMore)
        assertEquals(2, repository.headlineRequests.size)

        repository.headlineResults += testPage(2, listOf("b"))
        viewModel.onRetryLoadMore()
        advanceUntilIdle()

        assertEquals(listOf("a", "b"), viewModel.uiState.value.articles.map { it.key })
    }

    @Test
    fun `no more pages are requested once the end is reached`() = runTest {
        repository.headlineResults += testPage(1, listOf("a"), isLastPage = true)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onLoadMore()
        advanceUntilIdle()

        assertEquals(1, repository.headlineRequests.size)
    }

    @Test
    fun `refresh counts new stories and replaces the list`() = runTest {
        repository.headlineResults += testPage(1, listOf("a", "b"))
        repository.headlineResults += testPage(1, listOf("x", "y", "a"))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onRefresh()
        assertTrue(viewModel.uiState.value.isRefreshing)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals(listOf("x", "y", "a"), state.articles.map { it.key })
        assertEquals(RefreshOutcome.Updated(newStories = 2), state.lastRefresh)
    }

    @Test
    fun `failed refresh keeps the stories already on screen`() = runTest {
        repository.headlineResults += testPage(1, listOf("a", "b"))
        repository.headlineResults += AppResult.Failure(AppError.Timeout)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onRefresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("a", "b"), state.articles.map { it.key })
        assertEquals(RefreshOutcome.Failed(AppError.Timeout), state.lastRefresh)
        assertEquals(FeedStatus.Loaded, state.feedStatus)
    }

    @Test
    fun `choosing a topic reloads page one for that category`() = runTest {
        repository.headlineResults += testPage(1, listOf("a"))
        repository.headlineResults += testPage(1, listOf("h"))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onCategorySelected(NewsCategory.Health)
        assertEquals(FeedStatus.Loading, viewModel.uiState.value.feedStatus)
        advanceUntilIdle()

        assertEquals(1 to NewsCategory.Health, repository.headlineRequests.last())
        assertEquals(listOf("h"), viewModel.uiState.value.articles.map { it.key })
    }

    @Test
    fun `refresh, topic and a failed load are tracked`() = runTest {
        repository.headlineResults += testPage(1, listOf("a"))
        repository.headlineResults += AppResult.Failure(AppError.Unauthorized)
        repository.headlineResults += testPage(1, listOf("h"))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onRefresh()
        advanceUntilIdle()
        viewModel.onCategorySelected(NewsCategory.Health)
        advanceUntilIdle()

        assertEquals(
            listOf(
                AnalyticsEvent.FeedRefresh,
                AnalyticsEvent.LoadError(AnalyticsScreen.Today, AppError.Unauthorized),
                AnalyticsEvent.TopicSelect(NewsCategory.Health),
            ),
            analytics.events,
        )
    }
}
