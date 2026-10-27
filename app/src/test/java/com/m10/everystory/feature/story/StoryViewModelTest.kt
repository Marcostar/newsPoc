package com.m10.everystory.feature.story

import com.m10.everystory.testing.FakeNewsRepository
import com.m10.everystory.testing.MainDispatcherRule
import com.m10.everystory.testing.testArticle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeNewsRepository()

    @Test
    fun `a cached article is shown`() = runTest {
        val article = testArticle("a")
        repository.cachedArticles["a"] = article

        val viewModel = StoryViewModel("a", repository)
        advanceUntilIdle()

        assertEquals(StoryUiState.Ready(article), viewModel.uiState.value)
    }

    @Test
    fun `an article that is no longer cached shows the not found state`() = runTest {
        val viewModel = StoryViewModel("gone", repository)
        advanceUntilIdle()

        assertEquals(StoryUiState.NotFound, viewModel.uiState.value)
    }
}
