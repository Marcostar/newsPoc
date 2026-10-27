package com.m10.everystory.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.m10.everystory.R
import com.m10.everystory.core.designsystem.icon.EsIcons
import com.m10.everystory.core.designsystem.theme.EsRadius
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EsSpace
import com.m10.everystory.core.designsystem.theme.EsTheme
import com.m10.everystory.core.designsystem.theme.EsType
import com.m10.everystory.core.designsystem.theme.EveryStoryTheme
import com.m10.everystory.core.ui.composeEmail
import com.m10.everystory.core.ui.openInCustomTab
import com.m10.everystory.feature.profile.component.Confetti
import com.m10.everystory.feature.profile.component.FunFactsCard
import com.m10.everystory.feature.profile.component.PhotoCard
import com.m10.everystory.feature.profile.component.ProfileHero

// Each sticker leans a little differently, like they were slapped on by hand.
private val STICKER_TILTS = listOf(-3f, 2f, -1.5f, 2.5f, -2f, 1f)

// Where each link goes. A link left empty in strings.xml simply isn't shown.
data class ProfileLinks(
    val portfolio: String,
    val linkedIn: String,
    val gitHub: String,
    val email: String,
)

private data class LinkTile(
    val icon: ImageVector,
    val label: String,
    val description: String,
    val onClick: () -> Unit,
)

@Composable
fun ProfileRoute(onBack: () -> Unit) {
    val context = LocalContext.current
    val toolbarColor = MaterialTheme.colorScheme.primary.toArgb()
    ProfileScreen(
        links = ProfileLinks(
            portfolio = stringResource(R.string.profile_url_portfolio),
            linkedIn = stringResource(R.string.profile_url_linkedin),
            gitHub = stringResource(R.string.profile_url_github),
            email = stringResource(R.string.profile_email_address),
        ),
        onBack = onBack,
        onOpenLink = { url -> context.openInCustomTab(url, toolbarColor) },
        onEmail = { address -> context.composeEmail(address) },
    )
}

// The one playful page in a calm app: it's an easter egg, so it's allowed some fun.
@Composable
fun ProfileScreen(
    links: ProfileLinks,
    onBack: () -> Unit,
    onOpenLink: (String) -> Unit,
    onEmail: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            BackToYou(onBack)
            Column(
                verticalArrangement = Arrangement.spacedBy(EsSpace.s24),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = EsSpace.screen, end = EsSpace.screen, top = EsSpace.s8, bottom = EsSpace.s32),
            ) {
                ProfileHero()
                Text(
                    text = stringResource(R.string.profile_intro),
                    style = EsType.standfirst,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                PhotoCard()
                FunFactsCard()
                InterestStickers()
                BelieveQuote()
                Links(links, onOpenLink, onEmail)
                Text(
                    text = stringResource(R.string.profile_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = EsTheme.colors.muted,
                )
            }
        }
        Confetti(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun BackToYou(onBack: () -> Unit) {
    val description = stringResource(R.string.back_to, stringResource(R.string.nav_you))
    TextButton(
        onClick = onBack,
        modifier = Modifier
            .padding(start = EsSpace.s8, top = EsSpace.s4)
            .heightIn(min = EsSize.minTarget)
            .semantics { contentDescription = description },
    ) {
        Icon(EsIcons.Back, contentDescription = null)
        Text(
            text = stringResource(R.string.nav_you),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = EsSpace.s8),
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun InterestStickers() {
    val interests = stringArrayResource(R.array.profile_interests)
    // Background and text colour pairs from the palette, taken in turn.
    val stickerColors = listOf(
        MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer,
        MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary,
        EsTheme.colors.notice to MaterialTheme.colorScheme.onSurface,
    )
    Column(verticalArrangement = Arrangement.spacedBy(EsSpace.s12)) {
        SectionTitle(stringResource(R.string.profile_interests_title))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(EsSpace.s8),
            verticalArrangement = Arrangement.spacedBy(EsSpace.s12),
        ) {
            interests.forEachIndexed { index, interest ->
                val (background, content) = stickerColors[index % stickerColors.size]
                Text(
                    text = interest,
                    style = MaterialTheme.typography.labelLarge,
                    color = content,
                    modifier = Modifier
                        .graphicsLayer { rotationZ = STICKER_TILTS[index % STICKER_TILTS.size] }
                        .background(background, EsRadius.pill)
                        .padding(horizontal = EsSpace.s12, vertical = EsSpace.s8),
                )
            }
        }
    }
}

@Composable
private fun BelieveQuote() {
    Column(verticalArrangement = Arrangement.spacedBy(EsSpace.s8)) {
        SectionTitle(stringResource(R.string.profile_believe_title))
        Row(horizontalArrangement = Arrangement.spacedBy(EsSpace.s12)) {
            // The opening quote mark is decoration; TalkBack just reads the sentence.
            Text(
                text = stringResource(R.string.profile_quote_mark),
                style = MaterialTheme.typography.displaySmall,
                color = EsTheme.colors.coralInk,
                modifier = Modifier.clearAndSetSemantics { },
            )
            Column(verticalArrangement = Arrangement.spacedBy(EsSpace.s4), modifier = Modifier.padding(top = EsSpace.s8)) {
                Text(
                    text = stringResource(R.string.profile_believe),
                    style = EsType.quote.copy(fontSize = MaterialTheme.typography.titleLarge.fontSize),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.profile_believe_source).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = EsTheme.colors.coralInk,
                )
            }
        }
    }
}

@Composable
private fun Links(links: ProfileLinks, onOpenLink: (String) -> Unit, onEmail: (String) -> Unit) {
    val tiles = buildList {
        if (links.portfolio.isNotBlank()) add(webTile(EsIcons.Work, R.string.profile_portfolio, links.portfolio, onOpenLink))
        if (links.linkedIn.isNotBlank()) add(webTile(EsIcons.You, R.string.profile_linkedin, links.linkedIn, onOpenLink))
        if (links.gitHub.isNotBlank()) add(webTile(EsIcons.Code, R.string.profile_github, links.gitHub, onOpenLink))
        if (links.email.isNotBlank()) {
            add(
                LinkTile(
                    icon = EsIcons.Mail,
                    label = stringResource(R.string.profile_email),
                    description = stringResource(R.string.profile_email_description),
                    onClick = { onEmail(links.email) },
                ),
            )
        }
    }
    if (tiles.isEmpty()) return

    val tileColors = listOf(
        MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary,
        MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer,
        EsTheme.colors.notice to MaterialTheme.colorScheme.onSurface,
    )
    Column(verticalArrangement = Arrangement.spacedBy(EsSpace.s12)) {
        SectionTitle(stringResource(R.string.profile_links_title))
        FlowRow(
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(EsSpace.s12),
            verticalArrangement = Arrangement.spacedBy(EsSpace.s12),
        ) {
            tiles.forEachIndexed { index, tile ->
                val (background, content) = tileColors[index % tileColors.size]
                LinkTileView(tile, background, content, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun webTile(icon: ImageVector, labelRes: Int, url: String, onOpenLink: (String) -> Unit): LinkTile {
    val label = stringResource(labelRes)
    return LinkTile(
        icon = icon,
        label = label,
        description = stringResource(R.string.profile_link_description, label),
        onClick = { onOpenLink(url) },
    )
}

@Composable
private fun LinkTileView(tile: LinkTile, background: Color, content: Color, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(EsSpace.s12),
        modifier = modifier
            .heightIn(min = EsSize.sourceButton)
            .clip(EsRadius.card)
            .background(background)
            .clickable(role = Role.Button, onClick = tile.onClick)
            .semantics(mergeDescendants = true) { contentDescription = tile.description }
            .padding(EsSpace.s16),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(tile.icon, contentDescription = null, tint = content)
            Spacer(modifier = Modifier.weight(1f))
            Icon(EsIcons.OpenInNew, contentDescription = null, tint = content)
        }
        Text(text = tile.label, style = MaterialTheme.typography.labelLarge, color = content)
    }
}

private val previewLinks = ProfileLinks(
    portfolio = "https://example.com",
    linkedIn = "https://linkedin.com/in/example",
    gitHub = "https://github.com/example",
    email = "hello@example.com",
)

@Preview(name = "Day", showBackground = true, widthDp = 360, heightDp = 1600)
@Composable
private fun ProfileScreenPreview() {
    EveryStoryTheme { ProfileScreen(previewLinks, onBack = {}, onOpenLink = {}, onEmail = {}) }
}

@Preview(
    name = "Night",
    showBackground = true,
    widthDp = 360,
    heightDp = 1600,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ProfileScreenNightPreview() {
    EveryStoryTheme(darkTheme = true) { ProfileScreen(previewLinks, onBack = {}, onOpenLink = {}, onEmail = {}) }
}

@Preview(name = "Large text", showBackground = true, widthDp = 360, heightDp = 2400, fontScale = 2f)
@Composable
private fun ProfileScreenLargeTextPreview() {
    EveryStoryTheme { ProfileScreen(previewLinks, onBack = {}, onOpenLink = {}, onEmail = {}) }
}
