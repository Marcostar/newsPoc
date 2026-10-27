package com.m10.everystory.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRuleTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun mainDispatcherRunsOnTestScheduler() = runTest {
        var ran = false
        launch(Dispatchers.Main) { ran = true }

        assertFalse(ran)
        advanceUntilIdle()
        assertTrue(ran)
    }
}
