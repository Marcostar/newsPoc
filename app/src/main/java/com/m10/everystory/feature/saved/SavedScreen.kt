package com.m10.everystory.feature.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.m10.everystory.R
import com.m10.everystory.core.ui.animateStoryItem
import com.m10.everystory.core.designsystem.component.EsMessage
import com.m10.everystory.core.designsystem.component.EsSearchBarButton
import com.m10.everystory.core.designsystem.component.StoryRow
import com.m10.everystory.core.designsystem.icon.EsIcons
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EsSpace
import com.m10.everystory.core.designsystem.theme.EsTheme
import com.m10.everystory.core.designsystem.theme.EveryStoryTheme
import com.m10.everystory.domain.model.Article

@Composable
fun SavedRoute(
    onSearchClick: () -> Unit,
    onArticleClick: (Article) -> Unit,
    viewModel: SavedViewModel = viewModel(factory = SavedViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Runs each time the tab comes into view, including when opened from the reminder.
    LaunchedEffect(Unit) { viewModel.onShown() }
    SavedScreen(uiState = uiState, onSearchClick = onSearchClick, onArticleClick = onArticleClick)
}

@Composable
fun SavedScreen(
    uiState: SavedUiState,
    onSearchClick: () -> Unit,
    onArticleClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.saved_title),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(start = EsSpace.screen, end = EsSpace.screen, top = EsSpace.s8, bottom = EsSpace.s4)
                .semantics { heading() },
        )
        EsSearchBarButton(
            onClick = onSearchClick,
            modifier = Modifier.padding(start = EsSpace.s16, end = EsSpace.s16, top = EsSpace.s4, bottom = EsSpace.s8),
        )
        if (uiState.isLoading) return@Column

        val itemModifier = Modifier.padding(horizontal = EsSpace.screen)
        LazyColumn(
            contentPadding = PaddingValues(bottom = EsSpace.s48),
            modifier = Modifier.fillMaxSize(),
        ) {
            if (uiState.articles.isEmpty()) {
                item(key = "empty") {
                    EsMessage(
                        icon = EsIcons.SavedTab,
                        title = stringResource(R.string.saved_empty_title),
                        body = stringResource(R.string.saved_empty_body),
                        modifier = itemModifier.animateStoryItem(this),
                    )
                }
            } else {
                item(key = "status") {
                    SavedStatus(count = uiState.articles.size, modifier = itemModifier)
                }
                items(uiState.articles, key = { it.key }) { article ->
                    StoryRow(
                        article = article,
                        onClick = { onArticleClick(article) },
                        modifier = itemModifier.animateStoryItem(this),
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedStatus(count: Int, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(EsSpace.s8),
        modifier = modifier.padding(top = EsSpace.s4, bottom = EsSpace.s4),
    ) {
        Icon(
            imageVector = EsIcons.DownloadDone,
            contentDescription = null,
            tint = EsTheme.colors.muted,
            modifier = Modifier.size(EsSize.inlineIcon),
        )
        Text(
            text = pluralStringResource(R.plurals.saved_count, count, count),
            style = MaterialTheme.typography.bodyMedium,
            color = EsTheme.colors.muted,
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun SavedEmptyPreview() {
    EveryStoryTheme {
        SavedScreen(uiState = SavedUiState(isLoading = false), onSearchClick = {}, onArticleClick = {})
    }
}
