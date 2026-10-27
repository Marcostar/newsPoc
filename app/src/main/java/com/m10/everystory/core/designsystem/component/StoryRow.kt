package com.m10.everystory.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextOverflow
import com.m10.everystory.R
import com.m10.everystory.core.ui.StoryElement
import com.m10.everystory.core.ui.sharedStoryElement
import com.m10.everystory.core.designsystem.theme.EsRadius
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EsSpace
import com.m10.everystory.core.designsystem.theme.EsTheme
import com.m10.everystory.core.ui.highlightTerms
import com.m10.everystory.core.ui.relativeTimeText
import com.m10.everystory.domain.model.Article

private const val HEADLINE_MAX_LINES = 4
private const val STANDFIRST_MAX_LINES = 2

@Composable
fun StoryRow(
    article: Article,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlight: List<String> = emptyList(),
) {
    val time = relativeTimeText(article.publishedAt)
    val description = stringResource(R.string.story_card_description, article.title, article.source.name, time)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
            },
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(EsSpace.s16),
            modifier = Modifier.padding(vertical = EsSpace.s16),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(EsSpace.s4),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(R.string.meta_source_time, article.source.name, time),
                    style = MaterialTheme.typography.bodyMedium,
                    color = EsTheme.colors.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val highlightStyle = SpanStyle(background = EsTheme.colors.highlightWord)
                Text(
                    text = remember(article.title, highlight, highlightStyle) {
                        highlightTerms(article.title, highlight, highlightStyle)
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = HEADLINE_MAX_LINES,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.sharedStoryElement(article.key, StoryElement.Headline),
                )
                if (article.imageUrl == null && article.description != null) {
                    Text(
                        text = article.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = STANDFIRST_MAX_LINES,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            article.imageUrl?.let { url ->
                ArticleImage(
                    url = url,
                    contentDescription = null,
                    modifier = Modifier
                        .sharedStoryElement(article.key, StoryElement.Image, clipShape = EsRadius.image)
                        .size(EsSize.thumbnail)
                        .clip(EsRadius.image),
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
