package com.m10.everystory.data.news.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ArticlesResponseDto(
    val status: String,
    val totalResults: Int = 0,
    val articles: List<ArticleDto> = emptyList(),
)

@Serializable
data class ArticleDto(
    val source: ArticleSourceDto = ArticleSourceDto(),
    val author: String? = null,
    val title: String? = null,
    val description: String? = null,
    val url: String? = null,
    val urlToImage: String? = null,
    val publishedAt: String? = null,
    val content: String? = null,
)

@Serializable
data class ArticleSourceDto(
    val id: String? = null,
    val name: String? = null,
)
