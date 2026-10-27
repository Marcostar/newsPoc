package com.m10.everystory.core.network

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    // Emits the current answer straight away, then again whenever it changes.
    val isOnline: Flow<Boolean>
}
