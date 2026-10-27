package com.m10.everystory.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.m10.everystory.R
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EsSpace
import com.m10.everystory.core.designsystem.theme.EsTheme
import com.m10.everystory.core.ui.LoadMoreState

// Last item in the list, so it scrolls with the content and never covers it.
@Composable
fun PagingFooter(
    state: LoadMoreState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(EsSpace.s12, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = EsSize.minTarget)
            .padding(vertical = EsSpace.s16)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
    ) {
        when (state) {
            LoadMoreState.Idle -> Unit
            LoadMoreState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.size(EsSize.progressSmall))
                FooterText(stringResource(R.string.feed_loading_more))
            }
            is LoadMoreState.Failed -> {
                FooterText(stringResource(R.string.feed_load_more_failed))
                TextButton(onClick = onRetry) {
                    Text(stringResource(R.string.try_again), style = MaterialTheme.typography.labelLarge)
                }
            }
            LoadMoreState.EndReached -> FooterText(stringResource(R.string.feed_end))
        }
    }
}

@Composable
private fun FooterText(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodyMedium, color = EsTheme.colors.muted)
}
