package com.m10.everystory.core.ui

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.m10.everystory.R
import com.m10.everystory.core.common.AppError
import com.m10.everystory.core.designsystem.icon.EsIcons

// What the reader can do about it; each message offers exactly one.
enum class ErrorAction { TryAgain, UseBuiltInKey, UseOwnKey }

// No error codes on screen: say what happened and what to do next. Codes go to the logs.
data class ErrorMessage(
    val icon: ImageVector,
    @param:StringRes val title: Int,
    @param:StringRes val body: Int,
    val action: ErrorAction = ErrorAction.TryAgain,
)

// The reader's own key changes the advice: retrying a rejected key of theirs would only fail again,
// and when the built-in key's free requests run out, their own key is the way to keep going.
fun AppError.toErrorMessage(usingOwnKey: Boolean = false): ErrorMessage = when (this) {
    AppError.Offline -> ErrorMessage(EsIcons.Offline, R.string.error_offline_title, R.string.error_offline_body)
    AppError.Unauthorized ->
        if (usingOwnKey) {
            ErrorMessage(EsIcons.Key, R.string.error_cant_load_title, R.string.error_own_key_body, ErrorAction.UseBuiltInKey)
        } else {
            ErrorMessage(EsIcons.Error, R.string.error_cant_load_title, R.string.error_unauthorized_body)
        }
    AppError.Server -> ErrorMessage(EsIcons.Error, R.string.error_cant_load_title, R.string.error_server_body)
    AppError.Timeout -> ErrorMessage(EsIcons.Schedule, R.string.error_cant_load_title, R.string.error_timeout_body)
    AppError.RateLimited ->
        if (usingOwnKey) {
            ErrorMessage(EsIcons.Schedule, R.string.error_rate_limited_title, R.string.error_own_key_rate_limited_body)
        } else {
            ErrorMessage(EsIcons.Schedule, R.string.error_rate_limited_title, R.string.error_rate_limited_body, ErrorAction.UseOwnKey)
        }
    is AppError.Unknown -> ErrorMessage(EsIcons.Error, R.string.error_cant_load_title, R.string.error_unknown_body)
}

@StringRes
fun ErrorAction.labelRes(): Int = when (this) {
    ErrorAction.TryAgain -> R.string.try_again
    ErrorAction.UseBuiltInKey -> R.string.use_builtin_key
    ErrorAction.UseOwnKey -> R.string.use_own_key
}
