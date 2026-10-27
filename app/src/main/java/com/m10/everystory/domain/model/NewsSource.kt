package com.m10.everystory.domain.model

data class NewsSource(
    val id: String,
    val name: String,
    val description: String?,
    val url: String?,
    val category: String?,
    val language: String?,
    val country: String?,
)
