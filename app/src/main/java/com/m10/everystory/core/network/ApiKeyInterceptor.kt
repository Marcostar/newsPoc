package com.m10.everystory.core.network

import okhttp3.Interceptor
import okhttp3.Response

const val API_KEY_HEADER = "X-Api-Key"

// Asks for the key on every request, so a key the reader pastes in applies straight away.
// A request that already carries a key (checking a new one) keeps it.
class ApiKeyInterceptor(private val apiKey: () -> String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val key = apiKey()
        if (key.isBlank() || request.header(API_KEY_HEADER) != null) return chain.proceed(request)
        return chain.proceed(request.newBuilder().header(API_KEY_HEADER, key).build())
    }
}
