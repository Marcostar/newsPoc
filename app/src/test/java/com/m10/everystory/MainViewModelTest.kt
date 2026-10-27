package com.m10.everystory

import com.m10.everystory.core.ui.ConnectivityStatus
import com.m10.everystory.testing.FakeAnalyticsTracker
import com.m10.everystory.testing.FakeNetworkMonitor
import com.m10.everystory.testing.FakeUserPreferencesRepository
import com.m10.everystory.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val network = FakeNetworkMonitor()

    private fun createViewModel() = MainViewModel(FakeUserPreferencesRepository(), network, FakeAnalyticsTracker())

    @Test
    fun `no banner while online from the start`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(ConnectivityStatus.Online, viewModel.connectivity.value)
    }

    @Test
    fun `back online shows for six seconds after being offline`() = runTest {
        val viewModel = createViewModel()
        network.isOnline.value = false
        runCurrent()
        assertEquals(ConnectivityStatus.Offline, viewModel.connectivity.value)

        network.isOnline.value = true
        runCurrent()
        assertEquals(ConnectivityStatus.BackOnline, viewModel.connectivity.value)

        advanceTimeBy(5_999)
        assertEquals(ConnectivityStatus.BackOnline, viewModel.connectivity.value)
        advanceTimeBy(2)
        assertEquals(ConnectivityStatus.Online, viewModel.connectivity.value)
    }

    @Test
    fun `losing the connection again cancels the back online timer`() = runTest {
        val viewModel = createViewModel()
        network.isOnline.value = false
        runCurrent()
        network.isOnline.value = true
        runCurrent()

        network.isOnline.value = false
        advanceTimeBy(10_000)

        assertEquals(ConnectivityStatus.Offline, viewModel.connectivity.value)
    }

    @Test
    fun `acting on back online hides it`() = runTest {
        val viewModel = createViewModel()
        network.isOnline.value = false
        runCurrent()
        network.isOnline.value = true
        runCurrent()

        viewModel.onBackOnlineHandled()

        assertEquals(ConnectivityStatus.Online, viewModel.connectivity.value)
    }
}
