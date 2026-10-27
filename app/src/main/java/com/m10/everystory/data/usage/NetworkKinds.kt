package com.m10.everystory.data.usage

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.m10.everystory.domain.model.NetworkKind

// Which kind of network requests are going over right now. Wi-Fi and Ethernet count as Wi-Fi
// (not metered by a carrier); anything else that's online counts as mobile.
fun ConnectivityManager.currentNetworkKind(): NetworkKind? {
    val capabilities = getNetworkCapabilities(activeNetwork) ?: return null
    return when {
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkKind.WiFi
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkKind.WiFi
        else -> NetworkKind.Mobile
    }
}
