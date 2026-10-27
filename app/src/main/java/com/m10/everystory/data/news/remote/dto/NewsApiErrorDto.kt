package com.m10.everystory.data.news.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NewsApiErrorDto(
    val status: String? = null,
    val code: String? = null,
    val message: String? = null,
)
