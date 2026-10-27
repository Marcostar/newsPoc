package com.m10.everystory.navigation

import kotlinx.serialization.Serializable

@Serializable
data object TodayRoute

@Serializable
data object SavedRoute

@Serializable
data object YouRoute

@Serializable
data object SearchRoute

@Serializable
data class StoryRoute(val articleKey: String)
