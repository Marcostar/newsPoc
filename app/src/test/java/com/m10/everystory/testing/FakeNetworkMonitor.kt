package com.m10.everystory.testing

import com.m10.everystory.core.network.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow

class FakeNetworkMonitor(online: Boolean = true) : NetworkMonitor {
    override val isOnline = MutableStateFlow(online)
}
