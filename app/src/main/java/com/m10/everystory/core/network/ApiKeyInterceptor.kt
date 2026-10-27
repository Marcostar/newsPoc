package com.m10.everystory.core.network

import okhttp3.Interceptor
import okhttp3.Response

const val API_KEY_HEADER = "X-Api-Key"

// Release builds talk to our proxy, which holds the key itself, so they ship with a blank one.
class ApiKeyInterceptor(private val apiKey: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (apiKey.isBlank()) return chain.proceed(request)
        return chain.proceed(request.newBuilder().header(API_KEY_HEADER, apiKey).build())
    }
}
