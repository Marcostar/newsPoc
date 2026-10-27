package com.m10.everystory.feature.profile.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.m10.everystory.R
import com.m10.everystory.core.designsystem.icon.EsIcons
import com.m10.everystory.core.designsystem.theme.EsMotion
import com.m10.everystory.core.designsystem.theme.EsRadius
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EsSpace
import com.m10.everystory.core.designsystem.theme.EsTheme
import com.m10.everystory.core.designsystem.theme.EveryStoryTheme
import com.m10.everystory.core.ui.LocalReducedMotion

// One fact at a time; "Another one" deals the next, round and round.
@Composable
fun FunFactsCard(modifier: Modifier = Modifier) {
    val facts = stringArrayResource(R.array.profile_facts)
    if (facts.isEmpty()) return
    var index by rememberSaveable { mutableIntStateOf(0) }
    val reducedMotion = LocalReducedMotion.current
    val duration = if (reducedMotion) EsMotion.REDUCED_MS else EsMotion.STANDARD_MS

    Column(
        verticalArrangement = Arrangement.spacedBy(EsSpace.s12),
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, EsRadius.card)
            .border(BorderStroke(EsSize.hairline, MaterialTheme.colorScheme.outlineVariant), EsRadius.card)
            .padding(EsSpace.s20),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.profile_facts_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
            )
            Text(
                text = stringResource(R.string.profile_fact_count, index + 1, facts.size),
                style = MaterialTheme.typography.bodyMedium,
                color = EsTheme.colors.muted,
            )
        }
        AnimatedContent(
            targetState = index,
            transitionSpec = {
                if (reducedMotion) {
                    fadeIn(tween(duration)) togetherWith fadeOut(tween(duration))
                } else {
                    (slideInHorizontally(tween(duration)) { it / 4 } + fadeIn(tween(duration))) togetherWith fadeOut(tween(duration))
                }
            },
            label = "fact",
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        ) { shown ->
            Text(
                text = facts[shown],
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (facts.size > 1) {
            TextButton(
                onClick = { index = (index + 1) % facts.size },
                modifier = Modifier.heightIn(min = EsSize.minTarget),
            ) {
                Text(text = stringResource(R.string.profile_next_fact), style = MaterialTheme.typography.labelLarge)
                Icon(EsIcons.Forward, contentDescription = null, modifier = Modifier.padding(start = EsSpace.s4))
            }
        }
    }
}

@Preview(name = "Day", showBackground = true, widthDp = 360)
@Composable
private fun FunFactsCardPreview() {
    EveryStoryTheme { FunFactsCard(Modifier.padding(EsSpace.s16)) }
}

@Preview(name = "Night", showBackground = true, widthDp = 360, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FunFactsCardNightPreview() {
    EveryStoryTheme(darkTheme = true) { FunFactsCard(Modifier.padding(EsSpace.s16)) }
}

@Preview(name = "Large text", showBackground = true, widthDp = 360, fontScale = 2f)
@Composable
private fun FunFactsCardLargeTextPreview() {
    EveryStoryTheme { FunFactsCard(Modifier.padding(EsSpace.s16)) }
}
