package com.m10.everystory.core.ui

import android.provider.Settings
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.m10.everystory.core.designsystem.theme.EsMotion

// True when the reader turned on "Remove animations" (the system animator scale is then 0).
val LocalReducedMotion = staticCompositionLocalOf { false }

@Composable
fun rememberSystemReducedMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}

// Lets list items fade in and slide to their new place, e.g. when a page is added or a story
// unsaved. Also crossfades skeleton, stories and error, since each is its own keyed item.
@Composable
fun Modifier.animateStoryItem(itemScope: LazyItemScope): Modifier {
    if (LocalReducedMotion.current) return this
    return with(itemScope) {
        this@animateStoryItem.animateItem(
            fadeInSpec = tween(EsMotion.STANDARD_MS, easing = EsMotion.Easing),
            placementSpec = tween(EsMotion.STANDARD_MS, easing = EsMotion.Easing),
            fadeOutSpec = tween(EsMotion.STANDARD_MS, easing = EsMotion.Easing),
        )
    }
}

// The nav host provides both scopes; cards and the story screen tag their headline and image
// with the same key, so those pieces travel between the two screens. Null when motion is reduced.
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }
val LocalNavAnimatedVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

enum class StoryElement { Headline, Image }

/**
 * Marks part of a story card or the story screen as the same element. The headline changes text
 * size between the two, so it's scaled rather than re-laid out while it moves.
 */
@Composable
fun Modifier.sharedStoryElement(
    articleKey: String,
    element: StoryElement,
    clipShape: Shape = RectangleShape,
): Modifier {
    val sharedScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalNavAnimatedVisibilityScope.current ?: return this
    return with(sharedScope) {
        this@sharedStoryElement.sharedBounds(
            sharedContentState = rememberSharedContentState(key = "story-$element-$articleKey"),
            animatedVisibilityScope = visibilityScope,
            boundsTransform = { _, _ -> tween(EsMotion.STANDARD_MS, easing = EsMotion.Easing) },
            resizeMode = when (element) {
                StoryElement.Headline -> SharedTransitionScope.ResizeMode.scaleToBounds(
                    contentScale = ContentScale.FillWidth,
                    alignment = Alignment.TopStart,
                )
                StoryElement.Image -> SharedTransitionScope.ResizeMode.RemeasureToBounds
            },
            // While it travels it is drawn above both screens; keep the image's rounded corners.
            clipInOverlayDuringTransition = OverlayClip(clipShape),
        )
    }
}
