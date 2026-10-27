package com.m10.everystory.feature.developer

import com.m10.everystory.core.common.AppError

data class DeveloperSettingsUiState(
    // Last four characters of the reader's own key; null while the built-in key is in use.
    val ownKeyEnding: String? = null,
    val input: String = "",
    val isChecking: Boolean = false,
    val inputError: KeyInputError? = null,
    val message: DeveloperMessage? = null,
)

sealed interface KeyInputError {
    data object Empty : KeyInputError
    data object WrongFormat : KeyInputError
    data object Rejected : KeyInputError
    data class CouldNotCheck(val error: AppError) : KeyInputError
}

enum class DeveloperMessage { KeySaved, KeyRemoved }
