package com.m10.everystory.feature.today

import com.m10.everystory.core.analytics.AnalyticsEvent
import com.m10.everystory.core.analytics.AnalyticsScreen
import com.m10.everystory.core.common.AppError
import com.m10.everystory.core.common.AppResult
import com.m10.everystory.core.ui.LoadMoreState
import com.m10.everystory.domain.model.NewsCategory
import com.m10.everystory.domain.model.NewsLanguage
import com.m10.everystory.domain.model.CachedHeadlines
import com.m10.everystory.testing.FakeAnalyticsTracker
import com.m10.everystory.testing.FakeApiKeyRepository
import com.m10.everystory.testing.FakeNetworkMonitor
import com.m10.everystory.testing.FakeNewsRepository
import com.m10.everystory.testing.FakeUserPreferencesRepository
import com.m10.everystory.testing.MainDispatcherRule
import com.m10.everystory.testing.testArticle
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
    private val preferences = FakeUserPreferencesRepository()
    private val network = FakeNetworkMonitor()
    private val keys = FakeApiKeyRepository()
    private val fixedNow = Instant.parse("2026-10-26T09:30:00Z")

    private fun createViewModel() = TodayViewModel(repository, preferences, network, keys, analytics, now = { fixedNow })

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
    fun `cached feed shows at once and is replaced when page one arrives`() = runTest {
        val cachedAt = Instant.parse("2026-10-26T07:00:00Z")
        repository.cachedFeeds[null to NewsLanguage.English] = CachedHeadlines(listOf(testArticle("a")), cachedAt)
        repository.headlineResults += testPage(1, listOf("b", "a"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLaunching)
        assertEquals(listOf("b", "a"), state.articles.map { it.key })
        assertEquals(RefreshOutcome.Updated(newStories = 1), state.lastRefresh)
        assertEquals(fixedNow, state.updatedAt)
    }

    @Test
    fun `a feed cached minutes ago is shown without asking NewsAPI`() = runTest {
        val cachedAt = fixedNow.minusSeconds(5 * 60)
        repository.cachedFeeds[null to NewsLanguage.English] = CachedHeadlines(listOf(testArticle("a")), cachedAt)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(repository.headlineRequests.isEmpty())
        assertEquals(listOf("a"), state.articles.map { it.key })
        assertEquals(cachedAt, state.updatedAt)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun `refresh still asks NewsAPI when the cached feed is fresh`() = runTest {
        repository.cachedFeeds[null to NewsLanguage.English] =
            CachedHeadlines(listOf(testArticle("a")), fixedNow.minusSeconds(60))
        repository.headlineResults += testPage(1, listOf("b", "a"))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onRefresh()
        advanceUntilIdle()

        assertEquals(listOf(1 to null), repository.headlineRequests)
        assertEquals(listOf("b", "a"), viewModel.uiState.value.articles.map { it.key })
    }

    @Test
    fun `cached feed stays on screen when the network fails`() = runTest {
        val cachedAt = Instant.parse("2026-10-26T07:00:00Z")
        repository.cachedFeeds[null to NewsLanguage.English] = CachedHeadlines(listOf(testArticle("a")), cachedAt)
        repository.headlineResults += AppResult.Failure(AppError.Offline)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(FeedStatus.Loaded, state.feedStatus)
        assertEquals(listOf("a"), state.articles.map { it.key })
        assertEquals(cachedAt, state.updatedAt)
        assertEquals(RefreshOutcome.Failed(AppError.Offline), state.lastRefresh)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun `offline flag follows the network without reloading`() = runTest {
        repository.headlineResults += testPage(1, listOf("a"))
        val viewModel = createViewModel()
        advanceUntilIdle()

        network.isOnline.value = false
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isOffline)

        network.isOnline.value = true
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isOffline)
        assertEquals(1, repository.headlineRequests.size)
    }

    @Test
    fun `a rejected own key can be swapped for the built-in one, which refreshes`() = runTest {
        keys.ownKey.value = "0123456789abcdef0123456789abcdef"
        repository.headlineResults += testPage(1, listOf("a"))
        repository.headlineResults += AppResult.Failure(AppError.Unauthorized)
        repository.headlineResults += testPage(1, listOf("b", "a"))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onRefresh()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.usingOwnKey)
        assertEquals(RefreshOutcome.Failed(AppError.Unauthorized), viewModel.uiState.value.lastRefresh)

        viewModel.onUseBuiltInKey()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.usingOwnKey)
        assertEquals(listOf("b", "a"), state.articles.map { it.key })
        assertEquals(RefreshOutcome.Updated(newStories = 1), state.lastRefresh)
        assertTrue(AnalyticsEvent.OwnApiKey(saved = false) in analytics.events)
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

    @Test
    fun `choosing a language saves it and reloads page one on All in that language`() = runTest {
        repository.headlineResults += testPage(1, listOf("a"))
        repository.headlineResults += testPage(1, listOf("h"))
        repository.headlineResults += testPage(1, listOf("d"))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onCategorySelected(NewsCategory.Health)
        advanceUntilIdle()

        viewModel.onLanguageSelected(NewsLanguage.German)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(NewsLanguage.German, state.newsLanguage)
        assertEquals(null, state.selectedCategory)
        assertEquals(listOf("d"), state.articles.map { it.key })
        assertEquals(1 to null, repository.headlineRequests.last())
        assertEquals(NewsLanguage.German, repository.headlineLanguages.last())
        assertTrue(AnalyticsEvent.LanguageChange(NewsLanguage.German) in analytics.events)
    }

    @Test
    fun `another preference changing does not reload the feed`() = runTest {
        repository.headlineResults += testPage(1, listOf("a"))
        createViewModel()
        advanceUntilIdle()

        preferences.setTextSizeStep(5)
        advanceUntilIdle()

        assertEquals(1, repository.headlineRequests.size)
    }

    @Test
    fun `adding an own key after the free requests ran out refreshes the stories on screen`() = runTest {
        repository.headlineResults += testPage(1, listOf("a"))
        repository.headlineResults += AppResult.Failure(AppError.RateLimited)
        repository.headlineResults += testPage(1, listOf("b", "a"))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onRefresh()
        advanceUntilIdle()
        assertEquals(RefreshOutcome.Failed(AppError.RateLimited), viewModel.uiState.value.lastRefresh)

        keys.saveOwnKey("0123456789abcdef0123456789abcdef")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.usingOwnKey)
        assertEquals(listOf("b", "a"), state.articles.map { it.key })
        assertEquals(3, repository.headlineRequests.size)
    }

    @Test
    fun `a key that was already saved doesn't trigger an extra load`() = runTest {
        keys.ownKey.value = "0123456789abcdef0123456789abcdef"
        repository.headlineResults += AppResult.Failure(AppError.RateLimited)
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(FeedStatus.Failed(AppError.RateLimited), viewModel.uiState.value.feedStatus)
        assertEquals(1, repository.headlineRequests.size)
    }
}
