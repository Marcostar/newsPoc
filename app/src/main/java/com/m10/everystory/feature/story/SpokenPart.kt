package com.m10.everystory.feature.story

import com.m10.everystory.domain.model.Article

enum class StoryPart { Headline, Standfirst, Excerpt }

data class SpokenPart(val kind: StoryPart, val text: String)

// What Listen reads, in screen order. NewsAPI only gives us a preview, so this is all there is.
fun spokenParts(article: Article): List<SpokenPart> = listOfNotNull(
    SpokenPart(StoryPart.Headline, article.title),
    article.description?.let { SpokenPart(StoryPart.Standfirst, it) },
    article.content?.let { SpokenPart(StoryPart.Excerpt, it) },
)
