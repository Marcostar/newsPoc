package com.m10.everystory.core.ui

enum class ConnectivityStatus {
    Online,
    Offline,

    // Shown for a few seconds after the connection comes back, then it's plain Online again.
    BackOnline,
}
