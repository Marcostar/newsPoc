package com.m10.everystory.navigation

import kotlinx.serialization.Serializable

@Serializable
data object TodayDestination

@Serializable
data object SavedDestination

@Serializable
data object YouDestination

@Serializable
data object SearchDestination

@Serializable
data class StoryDestination(val articleKey: String)
