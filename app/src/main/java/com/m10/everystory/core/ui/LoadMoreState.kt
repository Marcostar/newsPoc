package com.m10.everystory.core.ui

import com.m10.everystory.core.common.AppError

sealed interface LoadMoreState {
    data object Idle : LoadMoreState
    data object Loading : LoadMoreState
    data class Failed(val error: AppError) : LoadMoreState
    data object EndReached : LoadMoreState
}
