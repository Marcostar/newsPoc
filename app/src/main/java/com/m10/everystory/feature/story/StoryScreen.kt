package com.m10.everystory.feature.story

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.m10.everystory.R
import com.m10.everystory.core.designsystem.component.ArticleImage
import com.m10.everystory.core.designsystem.component.EsMessage
import com.m10.everystory.core.designsystem.icon.EsIcons
import com.m10.everystory.core.designsystem.theme.EsRadius
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EsSpace
import com.m10.everystory.core.designsystem.theme.EsTheme
import com.m10.everystory.core.designsystem.theme.EsType
import com.m10.everystory.core.designsystem.theme.EveryStoryTheme
import com.m10.everystory.core.ui.displayDomain
import com.m10.everystory.core.ui.openInCustomTab
import com.m10.everystory.core.ui.sourceInitials
import com.m10.everystory.domain.model.Article
import com.m10.everystory.domain.model.ArticleSource
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private const val IMAGE_RATIO = 16f / 9f

@Composable
fun StoryRoute(
    articleKey: String,
    backLabel: String,
    onBack: () -> Unit,
    viewModel: StoryViewModel = viewModel(factory = StoryViewModel.factory(articleKey)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val toolbarColor = MaterialTheme.colorScheme.primary.toArgb()
    StoryScreen(
        uiState = uiState,
        backLabel = backLabel,
        onBack = onBack,
        onReadFullStory = { url -> context.openInCustomTab(url, toolbarColor) },
    )
}

@Composable
fun StoryScreen(
    uiState: StoryUiState,
    backLabel: String,
    onBack: () -> Unit,
    onReadFullStory: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        StoryTopBar(backLabel = backLabel, onBack = onBack)
        when (uiState) {
            StoryUiState.Loading -> Unit
            is StoryUiState.Ready -> StoryContent(uiState.article, onReadFullStory)
            StoryUiState.NotFound -> EsMessage(
                icon = EsIcons.Info,
                title = stringResource(R.string.story_missing_title),
                body = stringResource(R.string.story_missing_body),
                actionLabel = stringResource(R.string.back_to, backLabel),
                onAction = onBack,
                modifier = Modifier.padding(horizontal = EsSpace.screen),
            )
        }
    }
}

// A labelled Back that names where it goes, never a bare arrow.
@Composable
private fun StoryTopBar(backLabel: String, onBack: () -> Unit) {
    val description = stringResource(R.string.back_to, backLabel)
    TextButton(
        onClick = onBack,
        modifier = Modifier
            .padding(start = EsSpace.s8, top = EsSpace.s4)
            .heightIn(min = EsSize.minTarget)
            .semantics { contentDescription = description },
    ) {
        Icon(EsIcons.Back, contentDescription = null)
        Text(
            text = backLabel,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = EsSpace.s8),
        )
    }
}

@Composable
private fun StoryContent(article: Article, onReadFullStory: (String) -> Unit) {
    val domain = displayDomain(article.url)
    Column(
        verticalArrangement = Arrangement.spacedBy(EsSpace.s16),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = EsSpace.screen, end = EsSpace.screen, top = EsSpace.s8, bottom = EsSpace.s48),
    ) {
        Text(
            text = article.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )
        article.description?.let { standfirst ->
            Text(text = standfirst, style = EsType.standfirst, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        SourceRow(article)
        article.imageUrl?.let { url ->
            Column(verticalArrangement = Arrangement.spacedBy(EsSpace.s8)) {
                ArticleImage(
                    url = url,
                    contentDescription = stringResource(R.string.story_image_description, article.source.name),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(IMAGE_RATIO)
                        .clip(EsRadius.image),
                )
                Text(
                    text = stringResource(R.string.story_image_credit, article.source.name),
                    style = MaterialTheme.typography.bodyMedium,
                    color = EsTheme.colors.muted,
                )
            }
        }
        article.content?.let { excerpt ->
            Text(text = excerpt, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
        Text(
            text = stringResource(R.string.story_preview_note, domain),
            style = MaterialTheme.typography.bodyMedium,
            color = EsTheme.colors.muted,
        )
        ReadFullStoryButton(domain = domain, onClick = { onReadFullStory(article.url) })
    }
}

@Composable
private fun SourceRow(article: Article) {
    val locale = LocalConfiguration.current.locales[0]
    val published = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withLocale(locale)
        .withZone(ZoneId.systemDefault())
        .format(article.publishedAt)

    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EsSpace.s12),
            modifier = Modifier.padding(vertical = EsSpace.s12),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(EsSize.sourceBadge)
                    .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
                    .clearAndSetSemantics { },
            ) {
                Text(
                    text = sourceInitials(article.source.name),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(EsSpace.s4)) {
                Text(
                    text = article.source.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.story_published, published),
                    style = MaterialTheme.typography.bodyMedium,
                    color = EsTheme.colors.muted,
                )
                article.author?.let { author ->
                    Text(
                        text = stringResource(R.string.story_byline, author),
                        style = MaterialTheme.typography.bodyMedium,
                        color = EsTheme.colors.muted,
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun ReadFullStoryButton(domain: String, onClick: () -> Unit) {
    val description = stringResource(R.string.read_full_story_description, domain)
    Button(
        onClick = onClick,
        shape = EsRadius.button,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = EsSize.sourceButton)
            .semantics { contentDescription = description },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(EsSpace.s4),
            modifier = Modifier.weight(1f),
        ) {
            Text(text = stringResource(R.string.read_full_story), style = MaterialTheme.typography.labelLarge)
            Text(text = domain, style = MaterialTheme.typography.bodyMedium)
        }
        Icon(EsIcons.OpenInNew, contentDescription = null)
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun StoryScreenPreview() {
    EveryStoryTheme {
        StoryScreen(
            uiState = StoryUiState.Ready(
                Article(
                    key = "preview",
                    title = "A river town rewrites its flood plan after a third storm in ten years",
                    description = "After three major floods since 2016, Northfield will move homes off the lowest streets.",
                    content = "The council voted 7–2 on Thursday to offer voluntary buyouts…",
                    url = "https://www.northfieldpost.com/flood-plan",
                    imageUrl = null,
                    source = ArticleSource(id = null, name = "Northfield Post"),
                    author = "Dana Ruiz",
                    publishedAt = Instant.parse("2026-10-24T07:40:00Z"),
                ),
            ),
            backLabel = "Today",
            onBack = {},
            onReadFullStory = {},
        )
    }
}
