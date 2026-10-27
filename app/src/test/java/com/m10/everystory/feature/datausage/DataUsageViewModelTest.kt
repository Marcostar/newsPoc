package com.m10.everystory.feature.datausage

import com.m10.everystory.domain.model.DailyBytes
import com.m10.everystory.domain.model.DataUsage
import com.m10.everystory.domain.model.NetworkKind
import com.m10.everystory.domain.repository.DataUsageRepository
import com.m10.everystory.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class DataUsageViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 10, 27)
    private val requests = mutableListOf<NetworkKind>()

    private val repository = object : DataUsageRepository {
        override suspend fun usage(network: NetworkKind, today: LocalDate): DataUsage {
            requests += network
            return DataUsage(network, appBytes = 1_000, storiesBytes = 10, imagesBytes = 20, listOf(DailyBytes(today, 1_000)))
        }
    }

    @Test
    fun `opens on mobile data`() = runTest {
        val viewModel = DataUsageViewModel(repository, today = { today })
        advanceUntilIdle()

        assertEquals(NetworkKind.Mobile, viewModel.uiState.value.network)
        assertEquals(NetworkKind.Mobile, viewModel.uiState.value.usage?.network)
    }

    @Test
    fun `switching to Wi-Fi clears the old numbers and loads Wi-Fi`() = runTest {
        val viewModel = DataUsageViewModel(repository, today = { today })
        advanceUntilIdle()

        viewModel.onNetworkSelected(NetworkKind.WiFi)
        assertNull(viewModel.uiState.value.usage)
        advanceUntilIdle()

        assertEquals(NetworkKind.WiFi, viewModel.uiState.value.usage?.network)
        assertEquals(listOf(NetworkKind.Mobile, NetworkKind.WiFi), requests)
    }

    @Test
    fun `choosing the tab already shown does not reload`() = runTest {
        val viewModel = DataUsageViewModel(repository, today = { today })
        advanceUntilIdle()

        viewModel.onNetworkSelected(NetworkKind.Mobile)
        advanceUntilIdle()

        assertEquals(listOf(NetworkKind.Mobile), requests)
    }
}
