package com.m10.everystory.feature.story

import com.m10.everystory.core.analytics.AnalyticsEvent
import com.m10.everystory.testing.FakeAnalyticsTracker
import com.m10.everystory.testing.FakeNewsRepository
import com.m10.everystory.testing.FakeSavedArticlesRepository
import com.m10.everystory.testing.FakeUserPreferencesRepository
import com.m10.everystory.testing.MainDispatcherRule
import com.m10.everystory.testing.testArticle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val news = FakeNewsRepository()
    private val saved = FakeSavedArticlesRepository()
    private val analytics = FakeAnalyticsTracker()
    private val preferences = FakeUserPreferencesRepository()
    private var remindersScheduled = 0
    private val article = testArticle("a")

    // uiState is WhileSubscribed, so collect it the way the screen does.
    private fun TestScope.createViewModel(key: String = "a") = StoryViewModel(key, news, saved, preferences, { remindersScheduled++ }, analytics).also { viewModel ->
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    @Test
    fun `a cached article is shown`() = runTest {
        news.cachedArticles["a"] = article

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(StoryContent.Ready(article), viewModel.uiState.value.content)
        assertFalse(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `a saved copy is used once the cache has dropped the article`() = runTest {
        saved.save(article)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(StoryContent.Ready(article), viewModel.uiState.value.content)
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `an article nowhere on the phone shows the not found state`() = runTest {
        val viewModel = createViewModel(key = "gone")
        advanceUntilIdle()

        assertEquals(StoryContent.NotFound, viewModel.uiState.value.content)
    }

    @Test
    fun `save stores the article and confirms it`() = runTest {
        news.cachedArticles["a"] = article
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onToggleSave()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSaved)
        assertEquals(StoryMessage.Saved, viewModel.uiState.value.message)
        assertEquals(listOf(article), saved.savedArticles.first())
    }

    @Test
    fun `undo after saving removes it again`() = runTest {
        news.cachedArticles["a"] = article
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onToggleSave()
        advanceUntilIdle()

        viewModel.onMessageShown()
        viewModel.onUndo(StoryMessage.Saved)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSaved)
        assertNull(viewModel.uiState.value.message)
    }

    @Test
    fun `unsaving can be undone`() = runTest {
        news.cachedArticles["a"] = article
        saved.save(article)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onToggleSave()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isSaved)
        assertEquals(StoryMessage.Removed, viewModel.uiState.value.message)

        viewModel.onUndo(StoryMessage.Removed)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `opening, saving and reading in full are tracked with the source`() = runTest {
        news.cachedArticles["a"] = article
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onToggleSave()
        advanceUntilIdle()
        viewModel.onReadFullStory()

        val source = article.source.name
        assertEquals(
            listOf(
                AnalyticsEvent.StoryOpen(source),
                AnalyticsEvent.StorySave(source),
                AnalyticsEvent.StoryLinkOut(source),
            ),
            analytics.events,
        )
    }

    @Test
    fun `saving schedules the read-later reminder, removing does not`() = runTest {
        news.cachedArticles["a"] = article
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onToggleSave()
        advanceUntilIdle()
        viewModel.onToggleSave()
        advanceUntilIdle()

        assertEquals(1, remindersScheduled)
    }

    @Test
    fun `notification permission is asked after the first save only`() = runTest {
        news.cachedArticles["a"] = article
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onToggleSave()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.askNotificationPermission)

        viewModel.onNotificationPermissionAsked()
        viewModel.onToggleSave()
        viewModel.onToggleSave()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.askNotificationPermission)
    }
}
