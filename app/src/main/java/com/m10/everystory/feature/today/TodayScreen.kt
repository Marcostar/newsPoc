package com.m10.everystory.feature.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.m10.everystory.R
import com.m10.everystory.core.designsystem.component.EsAaButton
import com.m10.everystory.core.designsystem.component.EsLogoLockup
import com.m10.everystory.core.designsystem.component.EsMessage
import com.m10.everystory.core.designsystem.component.EsSearchBarButton
import com.m10.everystory.core.designsystem.component.EsTopicChip
import com.m10.everystory.core.designsystem.component.LeadStoryCard
import com.m10.everystory.core.designsystem.component.PagingFooter
import com.m10.everystory.core.designsystem.component.StoryRow
import com.m10.everystory.core.designsystem.component.StorySkeleton
import com.m10.everystory.core.designsystem.icon.EsIcons
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EsSpace
import com.m10.everystory.core.designsystem.theme.EsTheme
import com.m10.everystory.core.designsystem.theme.EveryStoryTheme
import com.m10.everystory.core.ui.LoadMoreWhenNearEnd
import com.m10.everystory.core.ui.LocalTourTargets
import com.m10.everystory.core.ui.TourStep
import com.m10.everystory.core.ui.clockTimeText
import com.m10.everystory.core.ui.toErrorMessage
import com.m10.everystory.core.ui.tourTarget
import com.m10.everystory.domain.model.Article
import com.m10.everystory.domain.model.ArticleSource
import com.m10.everystory.domain.model.NewsCategory
import java.time.Duration
import java.time.Instant

private val topics: List<Pair<NewsCategory?, Int>> = listOf(
    null to R.string.topic_all,
    NewsCategory.Business to R.string.topic_business,
    NewsCategory.Health to R.string.topic_health,
    NewsCategory.Science to R.string.topic_science,
    NewsCategory.Sports to R.string.topic_sports,
    NewsCategory.Technology to R.string.topic_technology,
    NewsCategory.Entertainment to R.string.topic_entertainment,
)

@Composable
fun TodayRoute(
    viewModel: TodayViewModel,
    isTourRunning: Boolean,
    onSearchClick: () -> Unit,
    onAaClick: () -> Unit,
    onArticleClick: (Article) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    // The tour points at the chips and the lead story, so start it from the top of the feed.
    LaunchedEffect(isTourRunning) {
        if (isTourRunning) listState.scrollToItem(0)
    }
    TodayScreen(
        uiState = uiState,
        onSearchClick = onSearchClick,
        onAaClick = onAaClick,
        onArticleClick = onArticleClick,
        onRefresh = viewModel::onRefresh,
        onRetry = viewModel::onRetry,
        onLoadMore = viewModel::onLoadMore,
        onRetryLoadMore = viewModel::onRetryLoadMore,
        onCategorySelected = viewModel::onCategorySelected,
        listState = listState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    uiState: TodayUiState,
    onSearchClick: () -> Unit,
    onAaClick: () -> Unit,
    onArticleClick: (Article) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    onCategorySelected: (NewsCategory?) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    val tourTargets = LocalTourTargets.current
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = EsSize.minTarget)
                .padding(start = EsSpace.screen, end = EsSpace.s8, top = EsSpace.s4),
        ) {
            EsLogoLockup(modifier = Modifier.weight(1f))
            EsAaButton(onClick = onAaClick, modifier = Modifier.tourTarget(tourTargets, TourStep.TextSize))
        }
        EsSearchBarButton(
            onClick = onSearchClick,
            modifier = Modifier
                .padding(start = EsSpace.s16, end = EsSpace.s16, top = EsSpace.s4, bottom = EsSpace.s8)
                .tourTarget(tourTargets, TourStep.Search),
        )

        LoadMoreWhenNearEnd(listState, onLoadMore)

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(bottom = EsSpace.s48),
                modifier = Modifier.fillMaxSize(),
            ) {
                item(key = "heading") {
                    TodayHeading(uiState = uiState, onRefresh = onRefresh)
                }
                item(key = "topics") {
                    TopicChips(
                        selected = uiState.selectedCategory,
                        onSelected = onCategorySelected,
                        modifier = Modifier.tourTarget(tourTargets, TourStep.Topics),
                    )
                }
                feedItems(
                    uiState = uiState,
                    onArticleClick = onArticleClick,
                    onRetry = onRetry,
                    onRetryLoadMore = onRetryLoadMore,
                    onCategorySelected = onCategorySelected,
                    leadModifier = Modifier.tourTarget(tourTargets, TourStep.LeadStory),
                )
            }
        }
    }
}

private fun LazyListScope.feedItems(
    uiState: TodayUiState,
    onArticleClick: (Article) -> Unit,
    onRetry: () -> Unit,
    onRetryLoadMore: () -> Unit,
    onCategorySelected: (NewsCategory?) -> Unit,
    leadModifier: Modifier,
) {
    val itemModifier = Modifier.padding(horizontal = EsSpace.screen)
    when (val status = uiState.feedStatus) {
        FeedStatus.Loading -> item(key = "skeleton") { StorySkeleton(itemModifier) }

        is FeedStatus.Failed -> item(key = "error") {
            val message = status.error.toErrorMessage()
            EsMessage(
                icon = message.icon,
                title = stringResource(message.title),
                body = stringResource(message.body),
                actionLabel = stringResource(R.string.try_again),
                onAction = onRetry,
                modifier = itemModifier,
            )
        }

        FeedStatus.Loaded -> {
            if (uiState.articles.isEmpty()) {
                item(key = "empty") {
                    EsMessage(
                        icon = EsIcons.SearchOff,
                        title = stringResource(R.string.feed_empty_title),
                        body = stringResource(R.string.feed_empty_body),
                        actionLabel = stringResource(R.string.topic_all),
                        onAction = { onCategorySelected(null) },
                        modifier = itemModifier,
                    )
                }
                return
            }
            val lead = uiState.articles.first()
            item(key = lead.key) {
                LeadStoryCard(
                    article = lead,
                    onClick = { onArticleClick(lead) },
                    modifier = itemModifier
                        .padding(bottom = EsSpace.s8)
                        .then(leadModifier),
                )
            }
            items(uiState.articles.drop(1), key = { it.key }) { article ->
                StoryRow(article = article, onClick = { onArticleClick(article) }, modifier = itemModifier)
            }
            item(key = "footer") {
                PagingFooter(state = uiState.loadMore, onRetry = onRetryLoadMore, modifier = itemModifier)
            }
        }
    }
}


@Composable
private fun TodayHeading(uiState: TodayUiState, onRefresh: () -> Unit) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(EsSpace.s8),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = EsSpace.screen, vertical = EsSpace.s8),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(EsSpace.s4),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = stringResource(R.string.today_title),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics { heading() },
            )
            statusLine(uiState)?.let { status ->
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodyMedium,
                    color = EsTheme.colors.muted,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
        }
        TextButton(
            onClick = onRefresh,
            enabled = !uiState.isRefreshing && uiState.feedStatus != FeedStatus.Loading,
            modifier = Modifier.heightIn(min = EsSize.minTarget),
        ) {
            Icon(EsIcons.Refresh, contentDescription = null, modifier = Modifier.size(EsSize.inlineIcon))
            Text(
                // A disabled button changes its words as well as its colour.
                text = stringResource(if (uiState.isRefreshing) R.string.refreshing else R.string.refresh),
                style = MaterialTheme.typography.labelLarge,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.padding(start = EsSpace.s4),
            )
        }
    }
}

@Composable
private fun statusLine(uiState: TodayUiState): String? {
    if (uiState.isRefreshing) return stringResource(R.string.status_refreshing)
    val updated = uiState.updatedAt?.let(::clockTimeText) ?: return null
    return when (val refresh = uiState.lastRefresh) {
        null -> stringResource(R.string.status_updated, updated)
        is RefreshOutcome.Updated ->
            if (refresh.newStories > 0) {
                pluralStringResource(R.plurals.status_new_stories, refresh.newStories, updated, refresh.newStories)
            } else {
                stringResource(R.string.status_no_new_stories, updated)
            }
        is RefreshOutcome.Failed -> stringResource(R.string.status_refresh_failed, updated)
    }
}

@Composable
private fun TopicChips(
    selected: NewsCategory?,
    onSelected: (NewsCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Vertical space sits outside the caller's modifier so the tour ring hugs the chips.
    LazyRow(
        modifier = Modifier
            .padding(top = EsSpace.s4, bottom = EsSpace.s16)
            .then(modifier),
        horizontalArrangement = Arrangement.spacedBy(EsSpace.s8),
        contentPadding = PaddingValues(horizontal = EsSpace.screen),
    ) {
        items(topics, key = { (category, _) -> category?.name ?: "all" }) { (category, label) ->
            EsTopicChip(
                label = stringResource(label),
                selected = category == selected,
                onClick = { onSelected(category) },
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun TodayScreenPreview() {
    val now = Instant.now()
    val articles = List(4) { index ->
        Article(
            key = "key$index",
            title = "A river town rewrites its flood plan after a third storm in ten years",
            description = "After three floods since 2016, the town is moving homes instead of raising the levee.",
            content = null,
            url = "https://example.com/$index",
            imageUrl = null,
            source = ArticleSource(id = null, name = "Northfield Post"),
            author = null,
            publishedAt = now.minus(Duration.ofHours(index + 2L)),
        )
    }
    EveryStoryTheme {
        TodayScreen(
            uiState = TodayUiState(
                isLaunching = false,
                articles = articles,
                feedStatus = FeedStatus.Loaded,
                updatedAt = now,
            ),
            onSearchClick = {},
            onAaClick = {},
            onArticleClick = {},
            onRefresh = {},
            onRetry = {},
            onLoadMore = {},
            onRetryLoadMore = {},
            onCategorySelected = {},
        )
    }
}
