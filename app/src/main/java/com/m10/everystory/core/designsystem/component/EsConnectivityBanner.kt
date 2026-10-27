package com.m10.everystory.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.m10.everystory.R
import com.m10.everystory.core.designsystem.icon.EsIcons
import com.m10.everystory.core.designsystem.theme.EsMotion
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EsSpace
import com.m10.everystory.core.designsystem.theme.EsTheme
import com.m10.everystory.core.designsystem.theme.EveryStoryTheme
import com.m10.everystory.core.ui.ConnectivityStatus
import com.m10.everystory.core.ui.LocalReducedMotion

// Sits under the status bar on every screen. The status bar padding is outside the animation, so
// the screen below never slides up under the clock while the banner opens or closes.
@Composable
fun EsConnectivityBanner(
    status: ConnectivityStatus,
    updatedAt: String?,
    showSavedAction: Boolean,
    onSavedClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Keeps drawing the last banner while it closes, rather than going blank mid-animation.
    var shown by remember { mutableStateOf(ConnectivityStatus.Offline) }
    if (status != ConnectivityStatus.Online) shown = status

    val duration = if (LocalReducedMotion.current) EsMotion.REDUCED_MS else EsMotion.STANDARD_MS
    Column(modifier = modifier.windowInsetsPadding(WindowInsets.statusBars)) {
        AnimatedVisibility(
            visible = status != ConnectivityStatus.Online,
            enter = expandVertically(tween(duration, easing = EsMotion.Easing)) + fadeIn(tween(duration)),
            exit = shrinkVertically(tween(duration, easing = EsMotion.Easing)) + fadeOut(tween(duration)),
        ) {
            when (shown) {
                ConnectivityStatus.BackOnline -> BannerRow(
                    containerColor = EsTheme.colors.highlightWord,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    actionColor = MaterialTheme.colorScheme.primary,
                    icon = EsIcons.Wifi,
                    lead = stringResource(R.string.banner_back_online_lead),
                    body = stringResource(R.string.banner_back_online_body),
                    actionLabel = stringResource(R.string.refresh),
                    actionDescription = null,
                    onAction = onRefreshClick,
                )
                else -> BannerRow(
                    // Ink, not red: a problem is ink, an icon and a sentence.
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background,
                    actionColor = MaterialTheme.colorScheme.background,
                    icon = EsIcons.Offline,
                    lead = stringResource(R.string.banner_offline_lead),
                    body = updatedAt?.let { stringResource(R.string.banner_offline_body, it) }
                        ?: stringResource(R.string.banner_offline_body_no_time),
                    actionLabel = stringResource(R.string.banner_offline_action).takeIf { showSavedAction },
                    actionDescription = stringResource(R.string.banner_offline_action_description),
                    onAction = onSavedClick,
                )
            }
        }
    }
}

@Composable
private fun BannerRow(
    containerColor: Color,
    contentColor: Color,
    actionColor: Color,
    icon: ImageVector,
    lead: String,
    body: String,
    actionLabel: String?,
    actionDescription: String?,
    onAction: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(EsSpace.s12),
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor)
            .heightIn(min = EsSize.minTarget)
            .padding(start = EsSpace.s16, end = EsSpace.s8, top = EsSpace.s8, bottom = EsSpace.s8),
    ) {
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(EsSize.inlineIcon))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(lead) }
                append(' ')
                append(body)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            modifier = Modifier
                .weight(1f)
                .semantics { liveRegion = LiveRegionMode.Polite },
        )
        if (actionLabel != null) {
            TextButton(
                onClick = onAction,
                modifier = Modifier
                    .heightIn(min = EsSize.minTarget)
                    .then(
                        if (actionDescription != null) Modifier.semantics { contentDescription = actionDescription } else Modifier,
                    ),
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = actionColor,
                    textDecoration = TextDecoration.Underline,
                )
            }
        }
    }
}

@Preview(name = "Offline, day", showBackground = true, widthDp = 360)
@Composable
private fun OfflinePreview() {
    EveryStoryTheme {
        EsConnectivityBanner(ConnectivityStatus.Offline, "09:30", showSavedAction = true, onSavedClick = {}, onRefreshClick = {})
    }
}

@Preview(name = "Back online, night", showBackground = true, widthDp = 360)
@Composable
private fun BackOnlinePreview() {
    EveryStoryTheme(darkTheme = true) {
        EsConnectivityBanner(ConnectivityStatus.BackOnline, "09:30", showSavedAction = true, onSavedClick = {}, onRefreshClick = {})
    }
}

@Preview(name = "Offline, large text", showBackground = true, widthDp = 360, fontScale = 2f)
@Composable
private fun OfflineLargeTextPreview() {
    EveryStoryTheme {
        EsConnectivityBanner(ConnectivityStatus.Offline, "09:30", showSavedAction = true, onSavedClick = {}, onRefreshClick = {})
    }
}
