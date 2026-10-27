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

    @Test
    fun `a request that already carries a key keeps it`() {
        assertEquals("trying-this-one", sendWith(apiKey = "abc123", requestKey = "trying-this-one"))
    }

    @Test
    fun `picks up a new key on the next request`() {
        var key = "first"
        val client = OkHttpClient.Builder().addInterceptor(ApiKeyInterceptor { key }).build()
        server.enqueue(MockResponse())
        server.enqueue(MockResponse())

        client.newCall(Request.Builder().url(server.url("/")).build()).execute().close()
        key = "second"
        client.newCall(Request.Builder().url(server.url("/")).build()).execute().close()

        assertEquals("first", server.takeRequest().getHeader(API_KEY_HEADER))
        assertEquals("second", server.takeRequest().getHeader(API_KEY_HEADER))
    }

    private fun sendWith(apiKey: String, requestKey: String? = null): String? {
        server.enqueue(MockResponse())
        val client = OkHttpClient.Builder().addInterceptor(ApiKeyInterceptor { apiKey }).build()
        val request = Request.Builder().url(server.url("/"))
            .apply { if (requestKey != null) header(API_KEY_HEADER, requestKey) }
            .build()
        client.newCall(request).execute().close()
        return server.takeRequest().getHeader(API_KEY_HEADER)
    }
}
