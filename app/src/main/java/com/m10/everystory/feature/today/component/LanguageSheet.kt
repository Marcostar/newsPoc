package com.m10.everystory.feature.today.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.m10.everystory.R
import com.m10.everystory.core.designsystem.component.EsBottomSheet
import com.m10.everystory.core.designsystem.icon.EsIcons
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EsSpace
import com.m10.everystory.core.designsystem.theme.EsTheme
import com.m10.everystory.core.designsystem.theme.EveryStoryTheme
import com.m10.everystory.core.ui.englishNameRes
import com.m10.everystory.core.ui.localeList
import com.m10.everystory.core.ui.nativeNameRes
import com.m10.everystory.domain.model.NewsLanguage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSheet(
    selected: NewsLanguage,
    onSelect: (NewsLanguage) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    // Let the sheet slide away before it leaves the composition.
    val close: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }
    EsBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        LanguageSheetContent(
            selected = selected,
            onSelect = { language ->
                onSelect(language)
                close()
            },
            onClose = close,
        )
    }
}

@Composable
fun LanguageSheetContent(
    selected: NewsLanguage,
    onSelect: (NewsLanguage) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val closeDescription = stringResource(R.string.language_close_description)
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(start = EsSpace.screen, end = EsSpace.screen, bottom = EsSpace.s20),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.language_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
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
        Text(
            text = stringResource(R.string.language_note),
            style = MaterialTheme.typography.bodyMedium,
            color = EsTheme.colors.muted,
            modifier = Modifier.padding(bottom = EsSpace.s8),
        )
        Column(modifier = Modifier.selectableGroup()) {
            NewsLanguage.entries.forEach { language ->
                LanguageRow(
                    language = language,
                    isSelected = language == selected,
                    onClick = { onSelect(language) },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

// Native name first, English name under it. The chosen one is bold with a tick, never colour alone.
@Composable
private fun LanguageRow(language: NewsLanguage, isSelected: Boolean, onClick: () -> Unit) {
    val englishName = stringResource(language.englishNameRes())
    val detail = if (language == NewsLanguage.Arabic) {
        stringResource(R.string.language_detail_rtl, englishName)
    } else {
        englishName
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(EsSpace.s12),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = EsSize.primaryButton)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = EsSpace.s8),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(EsSpace.s4), modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(language.nativeNameRes()),
                style = MaterialTheme.typography.bodyLarge.copy(localeList = language.localeList()),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(text = detail, style = MaterialTheme.typography.bodyMedium, color = EsTheme.colors.muted)
        }
        if (isSelected) {
            Icon(EsIcons.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun LanguageSheetPreview() {
    EveryStoryTheme {
        LanguageSheetContent(selected = NewsLanguage.English, onSelect = {}, onClose = {})
    }
}

@Preview(showBackground = true, widthDp = 360, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LanguageSheetNightPreview() {
    EveryStoryTheme(darkTheme = true) {
        LanguageSheetContent(selected = NewsLanguage.Arabic, onSelect = {}, onClose = {})
    }
}

@Preview(showBackground = true, widthDp = 360, fontScale = 2f)
@Composable
private fun LanguageSheetLargeTextPreview() {
    EveryStoryTheme {
        LanguageSheetContent(selected = NewsLanguage.German, onSelect = {}, onClose = {})
    }
}
