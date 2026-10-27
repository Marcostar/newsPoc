package com.m10.everystory.feature.tour

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateRectAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.m10.everystory.R
import com.m10.everystory.core.designsystem.theme.EsMotion
import com.m10.everystory.core.designsystem.theme.EsRadius
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EsSpace
import com.m10.everystory.core.designsystem.theme.EsTheme
import com.m10.everystory.core.designsystem.theme.EveryStoryTheme
import com.m10.everystory.core.ui.TourStep

/**
 * Dims the app, rings the current target in coral and explains it in a card. The overlay swallows
 * touches, so the screen underneath can't scroll the target away mid-tour.
 */
@Composable
fun TourOverlay(
    step: TourStep,
    targetBounds: Rect?,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // System back leaves the tour, like closing any other dialog.
    BackHandler(onBack = onSkip)

    val density = LocalDensity.current
    val spotlight = targetBounds?.inflate(with(density) { EsSpace.s4.toPx() })
    val animatedSpotlight by animateRectAsState(
        targetValue = spotlight ?: Rect.Zero,
        animationSpec = tween(EsMotion.ENTER_EXIT_MS),
        label = "spotlight",
    )
    val paneTitle = stringResource(R.string.tour_pane_title)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures {} }
            .semantics { this.paneTitle = paneTitle },
    ) {
        val scrim = EsTheme.colors.scrim
        val ring = MaterialTheme.colorScheme.tertiary
        Canvas(Modifier.fillMaxSize()) {
            if (spotlight == null) {
                drawRect(scrim)
                return@Canvas
            }
            val corner = CornerRadius(EsSize.spotlightCorner.toPx())
            val cutout = Path().apply { addRoundRect(RoundRect(animatedSpotlight, corner)) }
            clipPath(cutout, ClipOp.Difference) { drawRect(scrim) }
            drawRoundRect(
                color = ring,
                topLeft = animatedSpotlight.topLeft,
                size = animatedSpotlight.size,
                cornerRadius = corner,
                style = Stroke(EsSize.spotlightRing.toPx()),
            )
        }

        // The card goes on whichever side of the target has more room.
        val heightPx = constraints.maxHeight.toFloat()
        val cardBelow = spotlight == null || spotlight.top < heightPx - spotlight.bottom
        val area = with(density) {
            when {
                spotlight == null -> Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
                cardBelow -> Modifier
                    .padding(top = spotlight.bottom.toDp() + EsSpace.s12)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                else -> Modifier
                    .padding(bottom = (heightPx - spotlight.top).toDp() + EsSpace.s12)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            }
        }
        Box(
            contentAlignment = if (spotlight != null && cardBelow) Alignment.TopCenter else Alignment.BottomCenter,
            modifier = area
                .fillMaxSize()
                .padding(horizontal = EsSpace.s16)
                .then(if (spotlight == null) Modifier.padding(vertical = EsSpace.s24) else Modifier),
        ) {
            TourCard(step = step, onNext = onNext, onBack = onBack, onSkip = onSkip)
        }
    }
}

@Composable
private fun TourCard(
    step: TourStep,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tip = step.tip()
    val isLast = step.ordinal == TourStep.entries.lastIndex
    Surface(
        shape = EsRadius.card,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(EsSpace.s8),
            modifier = Modifier.padding(start = EsSpace.s16, end = EsSpace.s8, top = EsSpace.s8, bottom = EsSpace.s12),
        ) {
            // Read as one announcement each time the tip changes: count, title, then text. At the
            // largest text sizes only this part scrolls, so Back and Next never leave the card.
            Column(
                verticalArrangement = Arrangement.spacedBy(EsSpace.s8),
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.tour_count, step.ordinal + 1, TourStep.entries.size).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = EsTheme.colors.coralInk,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onSkip, modifier = Modifier.heightIn(min = EsSize.minTarget)) {
                        Text(
                            text = stringResource(R.string.tour_skip),
                            style = MaterialTheme.typography.labelLarge,
                            textDecoration = TextDecoration.Underline,
                        )
                    }
                }
                Text(
                    text = stringResource(tip.title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = EsSpace.s8),
                )
                Text(
                    text = stringResource(tip.text),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = EsSpace.s8),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EsSpace.s4),
                modifier = Modifier.padding(top = EsSpace.s4),
            ) {
                StepDots(current = step)
                Spacer(Modifier.weight(1f))
                if (step.ordinal > 0) {
                    TextButton(onClick = onBack, modifier = Modifier.heightIn(min = EsSize.minTarget)) {
                        Text(text = stringResource(R.string.tour_back), style = MaterialTheme.typography.labelLarge)
                    }
                }
                Button(
                    onClick = onNext,
                    shape = EsRadius.button,
                    modifier = Modifier.heightIn(min = EsSize.minTarget),
                ) {
                    Text(
                        text = stringResource(if (isLast) R.string.tour_finish else R.string.tour_next),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

// Position is already spoken in the count, so the dots are hidden from TalkBack.
@Composable
private fun StepDots(current: TourStep) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(EsSpace.s4),
        modifier = Modifier.clearAndSetSemantics {},
    ) {
        TourStep.entries.forEach { step ->
            val isCurrent = step == current
            Box(
                Modifier
                    .size(height = EsSize.tourDot, width = if (isCurrent) EsSize.tourDotActive else EsSize.tourDot)
                    .background(
                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        shape = EsRadius.pill,
                    ),
            )
        }
    }
}

private class TourTip(@param:StringRes val title: Int, @param:StringRes val text: Int)

private fun TourStep.tip(): TourTip = when (this) {
    TourStep.Search -> TourTip(R.string.tour_search_title, R.string.tour_search_text)
    TourStep.TextSize -> TourTip(R.string.tour_text_size_title, R.string.tour_text_size_text)
    TourStep.Language -> TourTip(R.string.tour_language_title, R.string.tour_language_text)
    TourStep.Topics -> TourTip(R.string.tour_topics_title, R.string.tour_topics_text)
    TourStep.LeadStory -> TourTip(R.string.tour_story_title, R.string.tour_story_text)
    TourStep.Saved -> TourTip(R.string.tour_saved_title, R.string.tour_saved_text)
    TourStep.You -> TourTip(R.string.tour_you_title, R.string.tour_you_text)
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun TourOverlayPreview() {
    EveryStoryTheme {
        TourOverlay(
            step = TourStep.Topics,
            targetBounds = Rect(left = 40f, top = 420f, right = 1040f, bottom = 540f),
            onNext = {},
            onBack = {},
            onSkip = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TourOverlayNightPreview() {
    EveryStoryTheme(darkTheme = true) {
        TourOverlay(step = TourStep.You, targetBounds = null, onNext = {}, onBack = {}, onSkip = {})
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760, fontScale = 2f)
@Composable
private fun TourOverlayLargeTextPreview() {
    EveryStoryTheme {
        TourOverlay(
            step = TourStep.Search,
            targetBounds = Rect(left = 40f, top = 180f, right = 1040f, bottom = 320f),
            onNext = {},
            onBack = {},
            onSkip = {},
        )
    }
}
