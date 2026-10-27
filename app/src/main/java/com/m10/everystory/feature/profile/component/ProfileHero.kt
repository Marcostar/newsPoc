package com.m10.everystory.feature.profile.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.m10.everystory.R
import com.m10.everystory.core.designsystem.theme.EsMotion
import com.m10.everystory.core.designsystem.theme.EsRadius
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EsSpace
import com.m10.everystory.core.designsystem.theme.EsTheme
import com.m10.everystory.core.designsystem.theme.EveryStoryTheme
import com.m10.everystory.core.ui.LocalReducedMotion
import kotlinx.coroutines.launch

private const val WIGGLE_MS = 480
private const val NO_GREETING = -1

// Name, role and a photo that waves back: each tap wiggles it and says hello in the next of
// the app's news languages.
@Composable
fun ProfileHero(modifier: Modifier = Modifier) {
    val greetings = stringArrayResource(R.array.profile_greetings)
    var greeting by rememberSaveable { mutableIntStateOf(NO_GREETING) }
    val wiggle = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val reducedMotion = LocalReducedMotion.current

    val bigBlob = EsTheme.colors.sageMark
    val smallBlob = MaterialTheme.colorScheme.tertiary
    val dot = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(EsRadius.card)
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            // A few soft shapes in the corner, drawn rather than shipped as an image.
            .drawBehind {
                drawCircle(bigBlob, radius = size.minDimension * 0.42f, center = Offset(size.width * 0.98f, size.height * 0.05f))
                drawCircle(smallBlob, radius = size.minDimension * 0.1f, center = Offset(size.width * 0.8f, size.height * 0.82f))
                drawCircle(dot, radius = size.minDimension * 0.035f, center = Offset(size.width * 0.93f, size.height * 0.6f))
            }
            .padding(EsSpace.s24),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(EsSpace.s16)) {
            Text(
                text = stringResource(R.string.profile_eyebrow).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = EsTheme.colors.coralInk,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(EsSpace.s16)) {
                val avatarDescription = stringResource(R.string.profile_avatar_description)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(EsSize.profileAvatar)
                        .graphicsLayer { rotationZ = wiggle.value }
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(role = Role.Button) {
                            greeting = (greeting + 1) % greetings.size
                            if (!reducedMotion) {
                                scope.launch {
                                    wiggle.animateTo(
                                        targetValue = 0f,
                                        animationSpec = keyframes {
                                            durationMillis = WIGGLE_MS
                                            -14f at 80
                                            11f at 180
                                            -7f at 280
                                            3f at 380
                                        },
                                    )
                                }
                            }
                        }
                        .semantics { contentDescription = avatarDescription },
                ) {
                    Image(
                        painter = painterResource(R.drawable.profile_avatar),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                if (greeting != NO_GREETING) {
                    AnimatedContent(
                        targetState = greeting,
                        transitionSpec = { fadeIn(tween(EsMotion.STANDARD_MS)) togetherWith fadeOut(tween(EsMotion.STANDARD_MS)) },
                        label = "greeting",
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    ) { index ->
                        Text(
                            text = greetings[index],
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceContainer, EsRadius.pill)
                                .padding(horizontal = EsSpace.s16, vertical = EsSpace.s8),
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(EsSpace.s4)) {
                Text(
                    text = stringResource(R.string.profile_name),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = stringResource(R.string.profile_role),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(name = "Day", showBackground = true, widthDp = 360)
@Composable
private fun ProfileHeroPreview() {
    EveryStoryTheme { ProfileHero() }
}

@Preview(name = "Night", showBackground = true, widthDp = 360, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileHeroNightPreview() {
    EveryStoryTheme(darkTheme = true) { ProfileHero() }
}

@Preview(name = "Large text", showBackground = true, widthDp = 360, fontScale = 2f)
@Composable
private fun ProfileHeroLargeTextPreview() {
    EveryStoryTheme { ProfileHero() }
}
