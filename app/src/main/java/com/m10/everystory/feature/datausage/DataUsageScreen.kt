package com.m10.everystory.feature.datausage

import android.content.Context
import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.m10.everystory.R
import com.m10.everystory.core.designsystem.component.EsSegmentedControl
import com.m10.everystory.core.designsystem.icon.EsIcons
import com.m10.everystory.core.designsystem.theme.EsRadius
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EsSpace
import com.m10.everystory.core.designsystem.theme.EsTheme
import com.m10.everystory.core.designsystem.theme.EveryStoryTheme
import com.m10.everystory.domain.model.DailyBytes
import com.m10.everystory.domain.model.DataUsage
import com.m10.everystory.domain.model.NetworkKind
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

private val networkOptions = listOf(NetworkKind.Mobile, NetworkKind.WiFi)

@Composable
fun DataUsageRoute(
    onBack: () -> Unit,
    viewModel: DataUsageViewModel = viewModel(factory = DataUsageViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DataUsageScreen(uiState = uiState, onBack = onBack, onNetworkSelected = viewModel::onNetworkSelected)
}

@Composable
fun DataUsageScreen(
    uiState: DataUsageUiState,
    onBack: () -> Unit,
    onNetworkSelected: (NetworkKind) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        BackToYou(onBack = onBack)
        Column(
            verticalArrangement = Arrangement.spacedBy(EsSpace.s16),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = EsSpace.screen, end = EsSpace.screen, bottom = EsSpace.s24),
        ) {
            Text(
                text = stringResource(R.string.data_title),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics { heading() },
            )
            EsSegmentedControl(
                options = networkOptions,
                selected = uiState.network,
                label = { network -> stringResource(network.labelRes()) },
                onSelect = onNetworkSelected,
            )
            // Nothing to show until the numbers arrive; they come from disk, so it's quick.
            uiState.usage?.let { usage ->
                MonthTotal(usage)
                SectionLabel(stringResource(R.string.data_last_seven_days))
                WeekChart(usage.lastSevenDays)
                SectionLabel(stringResource(R.string.data_breakdown))
                Breakdown(usage)
                Text(
                    text = stringResource(R.string.data_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = EsTheme.colors.muted,
                )
            }
        }
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
private fun MonthTotal(usage: DataUsage) {
    Surface(
        shape = EsRadius.card,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(EsSpace.s4),
            modifier = Modifier
                .padding(EsSpace.s16)
                .semantics(mergeDescendants = true) {},
        ) {
            Text(
                text = stringResource(R.string.data_this_month).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = EsTheme.colors.muted,
            )
            Text(
                text = LocalContext.current.bytesText(usage.totalBytes),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(usage.network.sinceRes(), monthStartText()),
                style = MaterialTheme.typography.bodyMedium,
                color = EsTheme.colors.muted,
            )
            if (usage.appBytes == null) {
                Text(
                    text = stringResource(R.string.data_system_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// Seven bars, today on the right. Read to TalkBack as one sentence listing each day.
@Composable
private fun WeekChart(days: List<DailyBytes>) {
    val locale = LocalConfiguration.current.locales[0]
    val context = LocalContext.current
    val largest = days.maxOfOrNull { it.bytes }?.coerceAtLeast(1L) ?: 1L
    val spoken = days.joinToString { day ->
        "${day.date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)} ${context.bytesText(day.bytes)}"
    }
    val chartDescription = stringResource(R.string.data_chart_description, spoken)
    Row(
        horizontalArrangement = Arrangement.spacedBy(EsSpace.s8),
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = chartDescription },
    ) {
        days.forEach { day ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(EsSpace.s4),
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    contentAlignment = Alignment.BottomCenter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(EsSize.usageChart),
                ) {
                    // A hairline even for zero, so an empty day still reads as a day.
                    val fraction = (day.bytes.toFloat() / largest).coerceAtLeast(MIN_BAR_FRACTION)
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(fraction)
                            .background(MaterialTheme.colorScheme.primary, EsRadius.badge),
                    )
                }
                Text(
                    text = day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
                    style = MaterialTheme.typography.bodyMedium,
                    color = EsTheme.colors.muted,
                    maxLines = 1,
                )
            }
        }
    }
}

private const val MIN_BAR_FRACTION = 0.02f

@Composable
private fun Breakdown(usage: DataUsage) {
    val total = usage.totalBytes.coerceAtLeast(1L)
    Column(verticalArrangement = Arrangement.spacedBy(EsSpace.s16)) {
        BreakdownRow(EsIcons.Today, stringResource(R.string.data_stories), usage.storiesBytes, total)
        BreakdownRow(EsIcons.Images, stringResource(R.string.data_images), usage.imagesBytes, total)
        BreakdownRow(EsIcons.Ads, stringResource(R.string.data_ads_and_other), usage.adsAndOtherBytes, total)
    }
}

// Icon, words and number say the same thing as the bar, so the bar is never the only signal.
@Composable
private fun BreakdownRow(icon: ImageVector, label: String, bytes: Long, total: Long) {
    Column(
        verticalArrangement = Arrangement.spacedBy(EsSpace.s8),
        modifier = Modifier.semantics(mergeDescendants = true) {},
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = EsSpace.s12),
            )
            Text(
                text = LocalContext.current.bytesText(bytes),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(EsSize.usageBar)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, EsRadius.pill),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(bytes.toFloat() / total)
                    .height(EsSize.usageBar)
                    .background(MaterialTheme.colorScheme.primary, EsRadius.pill),
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

// Android's own formatter, so sizes read exactly as they do in Settings, in the reader's locale.
private fun Context.bytesText(bytes: Long): String = Formatter.formatShortFileSize(this, bytes)

@Composable
private fun monthStartText(): String {
    val locale = LocalConfiguration.current.locales[0]
    return LocalDate.now().withDayOfMonth(1).format(DateTimeFormatter.ofPattern("d MMM", locale))
}

private fun NetworkKind.labelRes(): Int = when (this) {
    NetworkKind.Mobile -> R.string.data_mobile
    NetworkKind.WiFi -> R.string.data_wifi
}

private fun NetworkKind.sinceRes(): Int = when (this) {
    NetworkKind.Mobile -> R.string.data_since_mobile
    NetworkKind.WiFi -> R.string.data_since_wifi
}

private val previewUsage = DataUsage(
    network = NetworkKind.WiFi,
    appBytes = 38_400_000,
    storiesBytes = 4_200_000,
    imagesBytes = 27_900_000,
    lastSevenDays = List(7) { index ->
        DailyBytes(LocalDate.of(2026, 10, 21 + index), listOf(2, 5, 0, 8, 3, 12, 6)[index] * 1_000_000L)
    },
)

@Preview(showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun DataUsagePreview() {
    EveryStoryTheme {
        DataUsageScreen(
            uiState = DataUsageUiState(network = NetworkKind.WiFi, usage = previewUsage),
            onBack = {},
            onNetworkSelected = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 900, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DataUsageNightPreview() {
    EveryStoryTheme(darkTheme = true) {
        DataUsageScreen(
            uiState = DataUsageUiState(network = NetworkKind.Mobile, usage = previewUsage.copy(appBytes = null)),
            onBack = {},
            onNetworkSelected = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 1200, fontScale = 2f)
@Composable
private fun DataUsageLargeTextPreview() {
    EveryStoryTheme {
        DataUsageScreen(
            uiState = DataUsageUiState(network = NetworkKind.WiFi, usage = previewUsage),
            onBack = {},
            onNetworkSelected = {},
        )
    }
}
