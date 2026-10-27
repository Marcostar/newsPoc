package com.m10.everystory.core.network

import com.m10.everystory.core.common.AppError
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_TOO_MANY_REQUESTS = 429
private val HTTP_SERVER_ERRORS = 500..599

fun Throwable.toAppError(): AppError = when (this) {
    is SocketTimeoutException -> AppError.Timeout
    is IOException -> AppError.Offline
    is HttpException -> when (code()) {
        HTTP_UNAUTHORIZED -> AppError.Unauthorized
        HTTP_TOO_MANY_REQUESTS -> AppError.RateLimited
        in HTTP_SERVER_ERRORS -> AppError.Server
        else -> AppError.Unknown(this)
    }
    else -> AppError.Unknown(this)
}
