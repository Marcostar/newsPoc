package com.m10.everystory.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.m10.everystory.R
import com.m10.everystory.core.common.AppError
import com.m10.everystory.core.ui.animateStoryItem
import com.m10.everystory.core.designsystem.component.EsMessage
import com.m10.everystory.core.designsystem.component.PagingFooter
import com.m10.everystory.core.designsystem.component.StoryRow
import com.m10.everystory.core.designsystem.component.StorySkeleton
import com.m10.everystory.core.designsystem.icon.EsIcons
import com.m10.everystory.core.designsystem.theme.EsRadius
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EsSpace
import com.m10.everystory.core.designsystem.theme.EsTheme
import com.m10.everystory.core.designsystem.theme.EveryStoryTheme
import com.m10.everystory.core.ui.LoadMoreWhenNearEnd
import com.m10.everystory.core.ui.searchTerms
import com.m10.everystory.core.ui.toErrorMessage
import com.m10.everystory.domain.model.Article
import com.m10.everystory.domain.repository.MAX_RESULTS
import java.text.NumberFormat

@Composable
fun SearchRoute(
    onBack: () -> Unit,
    onArticleClick: (Article) -> Unit,
    viewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SearchScreen(
        uiState = uiState,
        onBack = onBack,
        onQueryChange = viewModel::onQueryChange,
        onSubmit = viewModel::onSubmit,
        onClear = viewModel::onClear,
        onRecentSearchClick = viewModel::onRecentSearchClick,
        onArticleClick = { article ->
            viewModel.onResultOpened()
            onArticleClick(article)
        },
        onRetry = viewModel::onRetry,
        onUseBuiltInKey = viewModel::onUseBuiltInKey,
        onLoadMore = viewModel::onLoadMore,
        onRetryLoadMore = viewModel::onRetryLoadMore,
    )
}

@Composable
fun SearchScreen(
    uiState: SearchUiState,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClear: () -> Unit,
    onRecentSearchClick: (String) -> Unit,
    onArticleClick: (Article) -> Unit,
    onRetry: () -> Unit,
    onUseBuiltInKey: () -> Unit,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LoadMoreWhenNearEnd(listState, onLoadMore)

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        SearchTopBar(
            query = uiState.query,
            onBack = onBack,
            onQueryChange = onQueryChange,
            onSubmit = onSubmit,
            onClear = onClear,
        )
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(bottom = EsSpace.s48),
            modifier = Modifier.fillMaxSize(),
        ) {
            searchContent(uiState, onRecentSearchClick, onArticleClick, onRetry, onUseBuiltInKey, onRetryLoadMore)
        }
    }
}

@Composable
private fun SearchTopBar(
    query: String,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClear: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    // Opening Search focuses the field and brings up the keyboard.
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = EsSpace.s4, end = EsSpace.s8, top = EsSpace.s4, bottom = EsSpace.s8),
    ) {
        IconButton(onClick = onBack) {
            Icon(EsIcons.Back, contentDescription = stringResource(R.string.back))
        }
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text(stringResource(R.string.search_hint)) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            shape = EsRadius.button,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                onSubmit()
                keyboard?.hide()
            }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
        )
        if (query.isNotEmpty()) {
            TextButton(onClick = onClear, modifier = Modifier.heightIn(min = EsSize.minTarget)) {
                Text(stringResource(R.string.clear), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

private fun LazyListScope.searchContent(
    uiState: SearchUiState,
    onRecentSearchClick: (String) -> Unit,
    onArticleClick: (Article) -> Unit,
    onRetry: () -> Unit,
    onUseBuiltInKey: () -> Unit,
    onRetryLoadMore: () -> Unit,
) {
    val itemModifier = Modifier.padding(horizontal = EsSpace.screen)
    when (val results = uiState.results) {
        SearchResults.Idle -> recentSearches(uiState.recentSearches, onRecentSearchClick, itemModifier)

        SearchResults.Loading -> item(key = "skeleton") {
            StorySkeleton(showLead = false, modifier = itemModifier.animateStoryItem(this).padding(top = EsSpace.s16))
        }

        is SearchResults.Failed -> item(key = "error") {
            val message = results.error.toErrorMessage()
            // Retrying a rejected key of the reader's own would only fail again.
            val ownKeyRejected = results.error == AppError.Unauthorized && uiState.usingOwnKey
            EsMessage(
                icon = message.icon,
                title = stringResource(message.title),
                body = stringResource(if (ownKeyRejected) R.string.error_own_key_body else message.body),
                actionLabel = stringResource(if (ownKeyRejected) R.string.use_builtin_key else R.string.try_again),
                onAction = if (ownKeyRejected) onUseBuiltInKey else onRetry,
                modifier = itemModifier.animateStoryItem(this),
            )
        }

        SearchResults.Loaded -> {
            if (uiState.articles.isEmpty()) {
                item(key = "empty") {
                    EsMessage(
                        icon = EsIcons.SearchOff,
                        title = stringResource(R.string.search_empty_title, uiState.searchedQuery),
                        body = stringResource(R.string.search_empty_body),
                        modifier = itemModifier.animateStoryItem(this),
                    )
                }
                return
            }
            item(key = "count") {
                ResultCount(total = uiState.totalResults, modifier = itemModifier)
            }
            val highlight = searchTerms(uiState.searchedQuery)
            items(uiState.articles, key = { it.key }) { article ->
                StoryRow(
                    article = article,
                    onClick = { onArticleClick(article) },
                    highlight = highlight,
                    modifier = itemModifier.animateStoryItem(this),
                )
            }
            item(key = "footer") {
                PagingFooter(state = uiState.loadMore, onRetry = onRetryLoadMore, modifier = itemModifier)
            }
        }
    }
}

private fun LazyListScope.recentSearches(
    recent: List<String>,
    onClick: (String) -> Unit,
    itemModifier: Modifier,
) {
    if (recent.isNotEmpty()) {
        item(key = "recent-heading") {
            Text(
                text = stringResource(R.string.recent_searches).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = EsTheme.colors.muted,
                modifier = itemModifier
                    .padding(top = EsSpace.s8, bottom = EsSpace.s4)
                    .semantics { heading() },
            )
        }
        items(recent, key = { "recent-$it" }) { query ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EsSpace.s12),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = EsSize.minTarget)
                    .clickable { onClick(query) }
                    .then(itemModifier),
            ) {
                Icon(EsIcons.History, contentDescription = null, tint = EsTheme.colors.muted)
                Text(text = query, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
    item(key = "hint") {
        Text(
            text = stringResource(R.string.search_hint_body),
            style = MaterialTheme.typography.bodyMedium,
            color = EsTheme.colors.muted,
            modifier = itemModifier.padding(top = EsSpace.s16),
        )
    }
}

@Composable
private fun ResultCount(total: Int, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = EsSpace.s4, bottom = EsSpace.s4),
    ) {
        val count = NumberFormat.getIntegerInstance(LocalConfiguration.current.locales[0]).format(total)
        Text(
            // Only the newest MAX_RESULTS are reachable on the developer plan; say so up front.
            text = if (total > MAX_RESULTS) {
                stringResource(R.string.search_result_count_capped, MAX_RESULTS, count)
            } else {
                pluralStringResource(R.plurals.search_result_count, total, count)
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.search_sorted_newest),
            style = MaterialTheme.typography.bodyMedium,
            color = EsTheme.colors.muted,
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun SearchIdlePreview() {
    EveryStoryTheme {
        SearchScreen(
            uiState = SearchUiState(recentSearches = listOf("levee repairs", "night shift", "fare cap")),
            onBack = {},
            onQueryChange = {},
            onSubmit = {},
            onClear = {},
            onRecentSearchClick = {},
            onArticleClick = {},
            onRetry = {},
            onUseBuiltInKey = {},
            onLoadMore = {},
            onRetryLoadMore = {},
        )
    }
}
