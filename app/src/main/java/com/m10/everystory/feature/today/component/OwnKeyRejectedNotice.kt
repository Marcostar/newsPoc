package com.m10.everystory.feature.today.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.m10.everystory.R
import com.m10.everystory.core.designsystem.icon.EsIcons
import com.m10.everystory.core.designsystem.theme.EsRadius
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EsSpace
import com.m10.everystory.core.designsystem.theme.EveryStoryTheme

// Shown above stories that are still on screen when a refresh with the reader's own key gets a 401.
// A problem, so ink outline + icon + sentence, never red.
@Composable
fun OwnKeyRejectedNotice(
    onUseBuiltInKey: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = EsRadius.card,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(EsSize.hairline, MaterialTheme.colorScheme.onSurface),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(start = EsSpace.s16, end = EsSpace.s8, top = EsSpace.s12, bottom = EsSpace.s4)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(EsSpace.s12),
                modifier = Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
            ) {
                Icon(
                    EsIcons.Key,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(EsSize.inlineIcon),
                )
                Text(
                    text = stringResource(R.string.own_key_refresh_failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = EsSpace.s8),
                )
            }
            TextButton(
                onClick = onUseBuiltInKey,
                modifier = Modifier
                    .padding(start = EsSpace.s24)
                    .heightIn(min = EsSize.minTarget),
            ) {
                Text(
                    text = stringResource(R.string.use_builtin_key),
                    style = MaterialTheme.typography.labelLarge,
                    textDecoration = TextDecoration.Underline,
                )
            }
        }
    }
}

@Preview(name = "Day", showBackground = true, widthDp = 360)
@Composable
private fun OwnKeyRejectedNoticePreview() {
    EveryStoryTheme { OwnKeyRejectedNotice(onUseBuiltInKey = {}, modifier = Modifier.padding(EsSpace.s16)) }
}

@Preview(name = "Night", showBackground = true, widthDp = 360)
@Composable
private fun OwnKeyRejectedNoticeNightPreview() {
    EveryStoryTheme(darkTheme = true) { OwnKeyRejectedNotice(onUseBuiltInKey = {}, modifier = Modifier.padding(EsSpace.s16)) }
}

@Preview(name = "Large text", showBackground = true, widthDp = 360, fontScale = 2f)
@Composable
private fun OwnKeyRejectedNoticeLargePreview() {
    EveryStoryTheme { OwnKeyRejectedNotice(onUseBuiltInKey = {}, modifier = Modifier.padding(EsSpace.s16)) }
}
