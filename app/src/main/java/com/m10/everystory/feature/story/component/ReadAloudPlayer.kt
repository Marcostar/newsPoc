package com.m10.everystory.feature.story.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.m10.everystory.R
import com.m10.everystory.core.designsystem.icon.EsIcons
import com.m10.everystory.core.designsystem.theme.EsRadius
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EsSpace
import com.m10.everystory.core.designsystem.theme.EsTheme
import com.m10.everystory.core.designsystem.theme.EveryStoryTheme
import com.m10.everystory.feature.story.Playback
import com.m10.everystory.feature.story.ReadAloudUiState
import com.m10.everystory.feature.story.SpokenWord
import com.m10.everystory.feature.story.StoryPart
import java.text.NumberFormat

private const val RATE_DIGITS = 2

// Sits under the story while it's being read: where we are, how fast, and the three controls.
@Composable
fun ReadAloudPlayer(
    uiState: ReadAloudUiState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onSpeedClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val paused = uiState.playback == Playback.Paused
    val partName = stringResource(
        when (uiState.spokenKind) {
            StoryPart.Headline, null -> R.string.read_aloud_part_headline
            StoryPart.Standfirst -> R.string.read_aloud_part_standfirst
            StoryPart.Excerpt -> R.string.read_aloud_part_excerpt
        },
    )
    val locale = LocalConfiguration.current.locales[0]
    val rate = remember(uiState.rate, locale) {
        NumberFormat.getNumberInstance(locale).apply { maximumFractionDigits = RATE_DIGITS }.format(uiState.rate)
    }
    val rateText = stringResource(R.string.read_aloud_rate, rate)
    val speedDescription = stringResource(R.string.read_aloud_speed_description, rateText)
    val playerName = stringResource(R.string.read_aloud_player)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .semantics { paneTitle = playerName },
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(
            verticalArrangement = Arrangement.spacedBy(EsSpace.s8),
            modifier = Modifier.padding(start = EsSpace.s12, end = EsSpace.s12, top = EsSpace.s8, bottom = EsSpace.s8),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Changes once per part, so TalkBack hears "Reading · summary" without word-by-word chatter.
                Text(
                    text = stringResource(if (paused) R.string.read_aloud_paused else R.string.read_aloud_reading, partName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = EsTheme.colors.muted,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { liveRegion = LiveRegionMode.Polite },
                )
                Text(
                    text = rateText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            LinearProgressIndicator(
                progress = { uiState.progress },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                gapSize = Dp.Hairline,
                drawStopIndicator = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(EsSize.playerProgress),
            )
            // Wraps at very large text sizes instead of squeezing Stop into one letter per line.
            FlowRow(
                itemVerticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(EsSpace.s4),
                modifier = Modifier.fillMaxWidth(),
            ) {
                PlayerTextButton(
                    icon = EsIcons.Speed,
                    label = stringResource(R.string.read_aloud_speed),
                    onClick = onSpeedClick,
                    modifier = Modifier.semantics { contentDescription = speedDescription },
                )
                Button(
                    onClick = if (paused) onResume else onPause,
                    shape = EsRadius.pill,
                    modifier = Modifier.heightIn(min = EsSize.minTarget),
                ) {
                    Icon(if (paused) EsIcons.Play else EsIcons.Pause, contentDescription = null)
                    Text(
                        text = stringResource(if (paused) R.string.read_aloud_resume else R.string.read_aloud_pause),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = EsSpace.s4),
                    )
                }
                PlayerTextButton(icon = EsIcons.Stop, label = stringResource(R.string.read_aloud_stop), onClick = onStop)
            }
        }
    }
}

@Composable
private fun PlayerTextButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        shape = EsRadius.button,
        modifier = modifier.heightIn(min = EsSize.minTarget),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(EsSize.inlineIcon))
        Text(text = label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(start = EsSpace.s4))
    }
}

private val previewState = ReadAloudUiState(
    playback = Playback.Playing,
    rate = 1.25f,
    spoken = SpokenWord(part = 1, start = 0, end = 5),
    spokenKind = StoryPart.Standfirst,
    progress = 0.4f,
)

@Preview(name = "Reading, day", showBackground = true, widthDp = 360)
@Composable
private fun ReadAloudPlayerPreview() {
    EveryStoryTheme { ReadAloudPlayer(previewState, onPause = {}, onResume = {}, onStop = {}, onSpeedClick = {}) }
}

@Preview(name = "Paused, night", showBackground = true, widthDp = 360, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReadAloudPlayerNightPreview() {
    EveryStoryTheme(darkTheme = true) {
        ReadAloudPlayer(previewState.copy(playback = Playback.Paused), onPause = {}, onResume = {}, onStop = {}, onSpeedClick = {})
    }
}

@Preview(name = "Large text", showBackground = true, widthDp = 360, fontScale = 2f)
@Composable
private fun ReadAloudPlayerLargeTextPreview() {
    EveryStoryTheme { ReadAloudPlayer(previewState, onPause = {}, onResume = {}, onStop = {}, onSpeedClick = {}) }
}
