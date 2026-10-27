package com.m10.everystory.data.news.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SourcesResponseDto(
    val status: String,
    val sources: List<SourceDto> = emptyList(),
)

@Serializable
data class SourceDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val url: String? = null,
    val category: String? = null,
    val language: String? = null,
    val country: String? = null,
)
