package com.m10.everystory.feature.story

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.m10.everystory.R
import com.m10.everystory.core.ui.StoryElement
import com.m10.everystory.core.ui.sharedStoryElement
import com.m10.everystory.core.designsystem.component.ArticleImage
import com.m10.everystory.core.designsystem.component.EsAaButton
import com.m10.everystory.core.designsystem.component.EsMessage
import com.m10.everystory.core.designsystem.component.EsSnackbarHost
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
    onAaClick: () -> Unit,
    viewModel: StoryViewModel = viewModel(factory = StoryViewModel.factory(articleKey)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val toolbarColor = MaterialTheme.colorScheme.primary.toArgb()

    // Whatever the answer, it's only asked once; a "no" just means no reminders.
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.onNotificationPermissionAsked()
    }
    LaunchedEffect(uiState.askNotificationPermission) {
        if (!uiState.askNotificationPermission) return@LaunchedEffect
        val needsAsking = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (needsAsking) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.onNotificationPermissionAsked()
        }
    }

    StoryScreen(
        uiState = uiState,
        backLabel = backLabel,
        onBack = onBack,
        onToggleSave = viewModel::onToggleSave,
        onAaClick = onAaClick,
        onUndo = viewModel::onUndo,
        onMessageShown = viewModel::onMessageShown,
        onReadFullStory = { url ->
            viewModel.onReadFullStory()
            context.openInCustomTab(url, toolbarColor)
        },
    )
}

@Composable
fun StoryScreen(
    uiState: StoryUiState,
    backLabel: String,
    onBack: () -> Unit,
    onToggleSave: () -> Unit,
    onAaClick: () -> Unit,
    onUndo: (StoryMessage) -> Unit,
    onMessageShown: () -> Unit,
    onReadFullStory: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val savedText = stringResource(R.string.snackbar_saved)
    val removedText = stringResource(R.string.snackbar_removed)
    val undoLabel = stringResource(R.string.undo)

    // Long is 10 seconds, the design's minimum so nobody has to race the Undo button.
    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = if (message == StoryMessage.Saved) savedText else removedText,
            actionLabel = undoLabel,
            duration = SnackbarDuration.Long,
        )
        onMessageShown()
        if (result == SnackbarResult.ActionPerformed) onUndo(message)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            StoryTopBar(
                backLabel = backLabel,
                onBack = onBack,
                isSaved = uiState.isSaved.takeIf { uiState.content is StoryContent.Ready },
                onToggleSave = onToggleSave,
                onAaClick = onAaClick,
            )
            StoryBody(content = uiState.content, backLabel = backLabel, onBack = onBack, onReadFullStory = onReadFullStory)
        }
        EsSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(EsSpace.s12),
        )
    }
}

@Composable
private fun StoryBody(
    content: StoryContent,
    backLabel: String,
    onBack: () -> Unit,
    onReadFullStory: (String) -> Unit,
) {
    when (content) {
        StoryContent.Loading -> Unit
        is StoryContent.Ready -> StoryContent(content.article, onReadFullStory)
        StoryContent.NotFound -> EsMessage(
            icon = EsIcons.Info,
            title = stringResource(R.string.story_missing_title),
            body = stringResource(R.string.story_missing_body),
            actionLabel = stringResource(R.string.back_to, backLabel),
            onAction = onBack,
            modifier = Modifier.padding(horizontal = EsSpace.screen),
        )
    }
}

// A labelled Back that names where it goes, never a bare arrow. Save shows once the story is loaded.
@Composable
private fun StoryTopBar(
    backLabel: String,
    onBack: () -> Unit,
    isSaved: Boolean?,
    onToggleSave: () -> Unit,
    onAaClick: () -> Unit,
) {
    val backDescription = stringResource(R.string.back_to, backLabel)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = EsSpace.s8, end = EsSpace.s8, top = EsSpace.s4),
    ) {
        TextButton(
            onClick = onBack,
            modifier = Modifier
                .heightIn(min = EsSize.minTarget)
                .semantics { contentDescription = backDescription },
        ) {
            Icon(EsIcons.Back, contentDescription = null)
            Text(
                text = backLabel,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = EsSpace.s8),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (isSaved != null) SaveButton(isSaved = isSaved, onClick = onToggleSave)
        EsAaButton(onClick = onAaClick)
    }
}

// Icon and word both change, so the state never relies on the icon alone.
@Composable
private fun SaveButton(isSaved: Boolean, onClick: () -> Unit) {
    val state = stringResource(if (isSaved) R.string.save_state_saved else R.string.save_state_not_saved)
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .heightIn(min = EsSize.minTarget)
            .semantics { stateDescription = state },
    ) {
        Icon(if (isSaved) EsIcons.Saved else EsIcons.Save, contentDescription = null)
        Text(
            text = stringResource(if (isSaved) R.string.saved_action else R.string.save_action),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = EsSpace.s4),
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
            // The Save snackbar sits at the bottom for 10 seconds; this lets the reader scroll
            // Read the full story clear of it.
            .padding(start = EsSpace.screen, end = EsSpace.screen, top = EsSpace.s8, bottom = EsSize.snackbarClearance),
    ) {
        Text(
            text = article.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .sharedStoryElement(article.key, StoryElement.Headline)
                .semantics { heading() },
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
                        .sharedStoryElement(article.key, StoryElement.Image, clipShape = EsRadius.image)
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
    val article = Article(
        key = "preview",
        title = "A river town rewrites its flood plan after a third storm in ten years",
        description = "After three major floods since 2016, Northfield will move homes off the lowest streets.",
        content = "The council voted 7–2 on Thursday to offer voluntary buyouts…",
        url = "https://www.northfieldpost.com/flood-plan",
        imageUrl = null,
        source = ArticleSource(id = null, name = "Northfield Post"),
        author = "Dana Ruiz",
        publishedAt = Instant.parse("2026-10-24T07:40:00Z"),
    )
    EveryStoryTheme {
        StoryScreen(
            uiState = StoryUiState(content = StoryContent.Ready(article), isSaved = true),
            backLabel = "Today",
            onBack = {},
            onToggleSave = {},
            onAaClick = {},
            onUndo = {},
            onMessageShown = {},
            onReadFullStory = {},
        )
    }
}
