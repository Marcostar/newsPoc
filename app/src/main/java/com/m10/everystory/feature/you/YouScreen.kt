package com.m10.everystory.feature.you

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.m10.everystory.BuildConfig
import com.m10.everystory.R
import com.m10.everystory.core.designsystem.component.EsSegmentedControl
import com.m10.everystory.core.designsystem.icon.EsIcons
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EsSpace
import com.m10.everystory.core.designsystem.theme.EsTheme
import com.m10.everystory.core.designsystem.theme.EveryStoryTheme
import com.m10.everystory.core.ui.labelRes
import com.m10.everystory.core.ui.openInCustomTab
import com.m10.everystory.core.ui.TourStep
import com.m10.everystory.core.ui.themeOptions
import com.m10.everystory.domain.model.ThemeSetting

private const val NEWS_API_URL = "https://newsapi.org"

@Composable
fun YouRoute(
    onClose: () -> Unit,
    onStartTour: () -> Unit,
    onOpenDisplaySettings: () -> Unit,
    viewModel: YouViewModel = viewModel(factory = YouViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val toolbarColor = MaterialTheme.colorScheme.primary.toArgb()
    YouScreen(
        uiState = uiState,
        versionName = BuildConfig.VERSION_NAME,
        versionCode = BuildConfig.VERSION_CODE,
        onClose = onClose,
        onThemeSelected = viewModel::onThemeSelected,
        onStartTour = onStartTour,
        onOpenDisplaySettings = onOpenDisplaySettings,
        onNewsApiClick = { context.openInCustomTab(NEWS_API_URL, toolbarColor) },
    )
}

@Composable
fun YouScreen(
    uiState: YouUiState,
    versionName: String,
    versionCode: Int,
    onClose: () -> Unit,
    onThemeSelected: (ThemeSetting) -> Unit,
    onStartTour: () -> Unit,
    onOpenDisplaySettings: () -> Unit,
    onNewsApiClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        YouHeader(onClose = onClose)
        Column(
            verticalArrangement = Arrangement.spacedBy(EsSpace.s12),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = EsSpace.screen, end = EsSpace.screen, bottom = EsSpace.s24),
        ) {
            SectionLabel(stringResource(R.string.you_appearance))
            EsSegmentedControl(
                options = themeOptions,
                selected = uiState.themeSetting,
                label = { setting -> stringResource(setting.labelRes()) },
                onSelect = onThemeSelected,
            )
            AboutRow(
                icon = EsIcons.TextSize,
                title = stringResource(R.string.you_display),
                subtitle = stringResource(R.string.you_display_detail),
                onClick = onOpenDisplaySettings,
            )

            SectionLabel(stringResource(R.string.you_help))
            val tipCount = TourStep.entries.size
            AboutRow(
                icon = EsIcons.Tour,
                title = stringResource(R.string.you_tour),
                subtitle = pluralStringResource(R.plurals.you_tour_detail, tipCount, tipCount),
                onClick = onStartTour,
                clickDescription = pluralStringResource(R.plurals.you_tour_description, tipCount, tipCount),
            )

            SectionLabel(stringResource(R.string.you_about))
            AboutRow(
                icon = EsIcons.Info,
                title = stringResource(R.string.app_name),
                subtitle = stringResource(R.string.you_version, versionName, versionCode),
            )
            AboutRow(
                icon = EsIcons.OpenInNew,
                title = stringResource(R.string.you_news_source),
                subtitle = stringResource(R.string.you_news_source_detail),
                onClick = onNewsApiClick,
                clickDescription = stringResource(R.string.you_news_source_description),
            )
        }
    }
}

@Composable
private fun YouHeader(onClose: () -> Unit) {
    val closeDescription = stringResource(R.string.close_to_today)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = EsSpace.screen, end = EsSpace.s8, top = EsSpace.s8, bottom = EsSpace.s4),
    ) {
        Text(
            text = stringResource(R.string.you_title),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        TextButton(
            onClick = onClose,
            modifier = Modifier
                .heightIn(min = EsSize.minTarget)
                .semantics { contentDescription = closeDescription },
        ) {
            Icon(EsIcons.Close, contentDescription = null)
            Text(
                text = stringResource(R.string.close),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = EsSpace.s4),
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = EsTheme.colors.muted,
        modifier = Modifier
            .padding(top = EsSpace.s8)
            .semantics { heading() },
    )
}

@Composable
private fun AboutRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    clickDescription: String? = null,
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(EsSpace.s12),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = EsSize.primaryButton)
            .then(clickModifier)
            .padding(vertical = EsSpace.s8)
            .semantics(mergeDescendants = true) {
                if (clickDescription != null) contentDescription = clickDescription
            },
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(verticalArrangement = Arrangement.spacedBy(EsSpace.s4), modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = EsTheme.colors.muted)
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun YouScreenPreview() {
    EveryStoryTheme {
        YouScreen(
            uiState = YouUiState(themeSetting = ThemeSetting.System),
            versionName = "1.0",
            versionCode = 1,
            onClose = {},
            onThemeSelected = {},
            onStartTour = {},
            onOpenDisplaySettings = {},
            onNewsApiClick = {},
        )
    }
}
