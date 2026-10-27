package com.m10.everystory.core.ui

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

// Ask for the next page while a few stories are still below the fold.
private const val PREFETCH_DISTANCE = 5

@Composable
fun LoadMoreWhenNearEnd(listState: LazyListState, onLoadMore: () -> Unit) {
    val nearEnd by remember(listState) {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && lastVisible >= info.totalItemsCount - PREFETCH_DISTANCE
        }
    }
    LaunchedEffect(nearEnd) {
        if (nearEnd) onLoadMore()
    }
}
