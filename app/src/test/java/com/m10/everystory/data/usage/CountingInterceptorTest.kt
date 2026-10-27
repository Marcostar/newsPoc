package com.m10.everystory.data.usage

import com.m10.everystory.domain.model.NetworkKind
import com.m10.everystory.domain.model.TrafficCategory
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CountingInterceptorTest {

    private val server = MockWebServer().apply { start() }
    private val recorded = mutableListOf<Triple<TrafficCategory, NetworkKind, Long>>()
    private val recorder = TrafficRecorder { category, network, bytes -> recorded += Triple(category, network, bytes) }

    private fun client(network: NetworkKind?) = OkHttpClient.Builder()
        .addNetworkInterceptor(CountingInterceptor(TrafficCategory.Images, recorder) { network })
        .build()

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `a full download is counted once, body plus headers, against the current network`() {
        server.enqueue(MockResponse().setBody(Buffer().write(ByteArray(5_000))))

        client(NetworkKind.WiFi).newCall(Request.Builder().url(server.url("/a.jpg")).build()).execute()
            .use { it.body?.bytes() }

        val (category, network, bytes) = recorded.single()
        assertEquals(TrafficCategory.Images, category)
        assertEquals(NetworkKind.WiFi, network)
        assertTrue("headers should be on top of the 5,000 body bytes, got $bytes", bytes > 5_000)
    }

    @Test
    fun `a download closed part way counts only what came down`() {
        server.enqueue(MockResponse().setBody(Buffer().write(ByteArray(50_000))))

        client(NetworkKind.Mobile).newCall(Request.Builder().url(server.url("/big.jpg")).build()).execute()
            .use { it.body?.source()?.require(1_000) }

        val bytes = recorded.single().third
        assertTrue("expected far less than 50,000, got $bytes", bytes < 50_000)
    }

    @Test
    fun `nothing is counted while offline`() {
        server.enqueue(MockResponse().setBody("{}"))

        client(network = null).newCall(Request.Builder().url(server.url("/")).build()).execute()
            .use { it.body?.string() }

        assertTrue(recorded.isEmpty())
    }
}
