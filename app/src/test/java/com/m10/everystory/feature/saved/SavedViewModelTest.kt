package com.m10.everystory.feature.saved

import com.m10.everystory.testing.FakeSavedArticlesRepository
import com.m10.everystory.testing.MainDispatcherRule
import com.m10.everystory.testing.testArticle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SavedViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val saved = FakeSavedArticlesRepository()

    @Test
    fun `starts loading, then shows an empty list`() = runTest {
        val viewModel = SavedViewModel(saved)
        assertTrue(viewModel.uiState.value.isLoading)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.articles.isEmpty())
    }

    @Test
    fun `newly saved stories appear at the top without a refresh`() = runTest {
        val viewModel = SavedViewModel(saved)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        saved.save(testArticle("a"))
        saved.save(testArticle("b"))
        advanceUntilIdle()

        assertEquals(listOf("b", "a"), viewModel.uiState.value.articles.map { it.key })
    }
}
