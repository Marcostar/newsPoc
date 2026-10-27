package com.m10.everystory.data.news.mapper

import com.m10.everystory.data.news.local.ArticleEntity
import com.m10.everystory.domain.model.Article
import com.m10.everystory.domain.model.ArticleSource
import java.time.Instant

fun Article.toEntity(cachedAt: Instant) = ArticleEntity(
    articleKey = key,
    title = title,
    description = description,
    content = content,
    url = url,
    imageUrl = imageUrl,
    sourceId = source.id,
    sourceName = source.name,
    author = author,
    publishedAtMillis = publishedAt.toEpochMilli(),
    cachedAt = cachedAt.toEpochMilli(),
)

fun ArticleEntity.toDomain() = Article(
    key = articleKey,
    title = title,
    description = description,
    content = content,
    url = url,
    imageUrl = imageUrl,
    source = ArticleSource(id = sourceId, name = sourceName),
    author = author,
    publishedAt = Instant.ofEpochMilli(publishedAtMillis),
)
