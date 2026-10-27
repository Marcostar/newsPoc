package com.m10.everystory.core.ui

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.m10.everystory.R
import com.m10.everystory.core.common.AppError
import com.m10.everystory.core.designsystem.icon.EsIcons

// No error codes on screen: say what happened and what to do next. Codes go to the logs.
data class ErrorMessage(
    val icon: ImageVector,
    @param:StringRes val title: Int,
    @param:StringRes val body: Int,
)

fun AppError.toErrorMessage(): ErrorMessage = when (this) {
    AppError.Offline -> ErrorMessage(EsIcons.Offline, R.string.error_offline_title, R.string.error_offline_body)
    AppError.Unauthorized -> ErrorMessage(EsIcons.Error, R.string.error_cant_load_title, R.string.error_unauthorized_body)
    AppError.Server -> ErrorMessage(EsIcons.Error, R.string.error_cant_load_title, R.string.error_server_body)
    AppError.Timeout -> ErrorMessage(EsIcons.Schedule, R.string.error_cant_load_title, R.string.error_timeout_body)
    AppError.RateLimited -> ErrorMessage(EsIcons.Schedule, R.string.error_rate_limited_title, R.string.error_rate_limited_body)
    is AppError.Unknown -> ErrorMessage(EsIcons.Error, R.string.error_cant_load_title, R.string.error_unknown_body)
}
