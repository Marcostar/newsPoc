package com.m10.everystory.domain.model

import java.time.Instant

// The first page of a feed as it was the last time it loaded.
data class CachedHeadlines(
    val articles: List<Article>,
    val updatedAt: Instant,
)
