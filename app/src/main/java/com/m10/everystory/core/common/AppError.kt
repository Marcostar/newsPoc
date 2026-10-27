package com.m10.everystory.core.common

sealed interface AppError {
    data object Offline : AppError
    data object Timeout : AppError
    data object Unauthorized : AppError
    data object RateLimited : AppError
    data object Server : AppError
    data class Unknown(val cause: Throwable? = null) : AppError
}
