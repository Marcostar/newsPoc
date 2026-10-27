package com.m10.everystory.core.network

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

// Follows the default network: the one the app's requests actually go out on.
class ConnectivityManagerNetworkMonitor(
    private val connectivityManager: ConnectivityManager,
) : NetworkMonitor {

    override val isOnline: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            private var hasInternet = false

            // Data Saver or a background-data restriction can block us while the phone itself is online.
            private var blocked = false

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                hasInternet = capabilities.hasInternet()
                trySend(hasInternet && !blocked)
            }

            override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
                this.blocked = blocked
                trySend(hasInternet && !blocked)
            }

            override fun onLost(network: Network) {
                hasInternet = false
                trySend(false)
            }
        }
        // activeNetwork is null while we're blocked, so this covers that case too.
        trySend(isOnlineNow())
        connectivityManager.registerDefaultNetworkCallback(callback)
        // Unregistering matters: a leftover callback keeps this flow (and whoever collects it) alive.
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }
        .distinctUntilChanged()
        .conflate()

    private fun isOnlineNow(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        return connectivityManager.getNetworkCapabilities(network)?.hasInternet() == true
    }

    private fun NetworkCapabilities.hasInternet() = hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
