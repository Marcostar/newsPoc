package com.m10.everystory.data.saved

import com.m10.everystory.domain.model.Article
import com.m10.everystory.domain.model.ArticleSource
import java.time.Instant

fun Article.toSavedEntity(savedAt: Instant) = SavedArticleEntity(
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
    savedAt = savedAt.toEpochMilli(),
)

fun SavedArticleEntity.toDomain() = Article(
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
