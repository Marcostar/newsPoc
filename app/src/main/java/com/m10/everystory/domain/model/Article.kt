package com.m10.everystory.domain.model

import java.time.Instant

data class Article(
    val key: String,
    val title: String,
    val description: String?,
    val content: String?,
    val url: String,
    val imageUrl: String?,
    val source: ArticleSource,
    val author: String?,
    val publishedAt: Instant,
)

data class ArticleSource(
    val id: String?,
    val name: String,
)
