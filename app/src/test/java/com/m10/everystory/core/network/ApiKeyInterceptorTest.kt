package com.m10.everystory.core.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ApiKeyInterceptorTest {

    private val server = MockWebServer()

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `adds the key header when a key is set`() {
        assertEquals("abc123", sendWith(apiKey = "abc123"))
    }

    @Test
    fun `sends no key header when the key is blank`() {
        assertNull(sendWith(apiKey = ""))
    }

    private fun sendWith(apiKey: String): String? {
        server.enqueue(MockResponse())
        val client = OkHttpClient.Builder().addInterceptor(ApiKeyInterceptor(apiKey)).build()
        client.newCall(Request.Builder().url(server.url("/")).build()).execute().close()
        return server.takeRequest().getHeader(API_KEY_HEADER)
    }
}
