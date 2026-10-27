package com.m10.everystory.domain.model

data class ArticlePage(
    val articles: List<Article>,
    val page: Int,
    val totalResults: Int,
    val isLastPage: Boolean,
)
