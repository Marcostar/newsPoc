package com.m10.everystory.data.usage

import com.m10.everystory.domain.model.NetworkKind
import com.m10.everystory.domain.model.TrafficCategory
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.buffer

/**
 * Counts what one HTTP client sends and receives. Add it with addNetworkInterceptor so it sees
 * the bytes on the wire: compressed, and skipped entirely when the answer comes from cache.
 */
class CountingInterceptor(
    private val category: TrafficCategory,
    private val recorder: TrafficRecorder,
    // Null when offline; nothing is counted then.
    private val currentNetwork: () -> NetworkKind?,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val network = currentNetwork() ?: return response

        val requestBytes = request.headers.byteCount() + (request.body?.contentLength()?.coerceAtLeast(0) ?: 0)
        val body = response.body ?: run {
            recorder.record(category, network, requestBytes + response.headers.byteCount())
            return response
        }
        // The body streams in later, so its size is only known once the caller has read it all.
        val headerBytes = requestBytes + response.headers.byteCount()
        return response.newBuilder()
            .body(CountingResponseBody(body) { bodyBytes -> recorder.record(category, network, headerBytes + bodyBytes) })
            .build()
    }
}

private class CountingResponseBody(
    private val delegate: ResponseBody,
    private val onDone: (bodyBytes: Long) -> Unit,
) : ResponseBody() {

    private var bytesRead = 0L
    private var reported = false

    private val countingSource: BufferedSource = object : ForwardingSource(delegate.source()) {
        override fun read(sink: Buffer, byteCount: Long): Long {
            val read = super.read(sink, byteCount)
            if (read == -1L) report() else bytesRead += read
            return read
        }

        // Callers that stop early (a cancelled image, say) still count what came down.
        override fun close() {
            report()
            super.close()
        }
    }.buffer()

    private fun report() {
        if (reported) return
        reported = true
        onDone(bytesRead)
    }

    override fun contentType(): MediaType? = delegate.contentType()

    override fun contentLength(): Long = delegate.contentLength()

    override fun source(): BufferedSource = countingSource
}
