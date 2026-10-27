package com.m10.everystory.core.common

// Kotlin's Result only carries a Throwable; we want the UI to switch on a typed AppError.
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}
