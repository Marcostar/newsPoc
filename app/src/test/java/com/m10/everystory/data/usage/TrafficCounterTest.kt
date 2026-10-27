package com.m10.everystory.data.usage

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.m10.everystory.domain.model.NetworkKind
import com.m10.everystory.domain.model.TrafficCategory
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.YearMonth

class TrafficCounterTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val october = YearMonth.of(2026, 10)

    @Test
    fun `bytes add up per month, network and category`() = runTest {
        val counter = TrafficCounter(
            dataStore = PreferenceDataStoreFactory.create(scope = backgroundScope) {
                folder.newFile("usage.preferences_pb")
            },
            scope = backgroundScope,
        )

        counter.add(october, NetworkKind.WiFi, TrafficCategory.Images, 1_000)
        counter.add(october, NetworkKind.WiFi, TrafficCategory.Images, 500)
        counter.add(october, NetworkKind.WiFi, TrafficCategory.Stories, 40)
        counter.add(october, NetworkKind.Mobile, TrafficCategory.Images, 7)
        counter.add(october.plusMonths(1), NetworkKind.WiFi, TrafficCategory.Images, 99)

        assertEquals(1_500, counter.bytes(october, NetworkKind.WiFi, TrafficCategory.Images))
        assertEquals(40, counter.bytes(october, NetworkKind.WiFi, TrafficCategory.Stories))
        assertEquals(7, counter.bytes(october, NetworkKind.Mobile, TrafficCategory.Images))
        assertEquals(99, counter.bytes(october.plusMonths(1), NetworkKind.WiFi, TrafficCategory.Images))
    }
}
