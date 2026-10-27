package com.m10.everystory.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.m10.everystory.R
import com.m10.everystory.core.ui.StoryElement
import com.m10.everystory.core.ui.sharedStoryElement
import com.m10.everystory.core.designsystem.theme.EsRadius
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EsSpace
import com.m10.everystory.core.designsystem.theme.EsTheme
import com.m10.everystory.core.ui.relativeTimeText
import com.m10.everystory.domain.model.Article

private const val LEAD_IMAGE_RATIO = 16f / 9f

@Composable
fun LeadStoryCard(
    article: Article,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val time = relativeTimeText(article.publishedAt)
    val description = stringResource(R.string.story_card_description, article.title, article.source.name, time)

    Surface(
        onClick = onClick,
        shape = EsRadius.card,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(EsSize.hairline, MaterialTheme.colorScheme.outlineVariant),
        // TalkBack reads the card as one item, headline first.
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = description },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(EsSpace.s8),
            modifier = Modifier.padding(EsSpace.s12),
        ) {
            article.imageUrl?.let { url ->
                ArticleImage(
                    url = url,
                    contentDescription = null,
                    modifier = Modifier
                        .sharedStoryElement(article.key, StoryElement.Image, clipShape = EsRadius.image)
                        .fillMaxWidth()
                        .aspectRatio(LEAD_IMAGE_RATIO)
                        .clip(EsRadius.image),
                )
            }
            Text(
                text = article.source.name.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = EsTheme.colors.muted,
                maxLines = 1,
            )
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.sharedStoryElement(article.key, StoryElement.Headline),
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodyMedium,
                color = EsTheme.colors.muted,
            )
        }
    }
}
