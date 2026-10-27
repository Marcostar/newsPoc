package com.m10.everystory.core.analytics

// Fixed lowercase names so reports stay readable and stable across releases.
enum class AnalyticsScreen(val value: String) {
    Today("today"),
    Search("search"),
    Story("story"),
    Saved("saved"),
    You("you"),
}
