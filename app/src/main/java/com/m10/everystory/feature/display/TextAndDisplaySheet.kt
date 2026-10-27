package com.m10.everystory.feature.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextDecoration
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
import com.m10.everystory.core.ui.labelRes
import com.m10.everystory.core.ui.themeOptions
import com.m10.everystory.domain.model.TextSize
import com.m10.everystory.domain.model.ThemeSetting
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextAndDisplaySheet(
    onDismiss: () -> Unit,
    viewModel: DisplaySettingsViewModel = viewModel(factory = DisplaySettingsViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    // Let the sheet slide away before it leaves the composition.
    val close: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    // The sheet opens in its own window, which starts from the phone's density and loses the
    // theme's text scale. Hand it the app's density so the preview grows as the size changes.
    val appDensity = LocalDensity.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = EsRadius.sheet,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
    ) {
        CompositionLocalProvider(LocalDensity provides appDensity) {
            TextAndDisplayContent(
                uiState = uiState,
                onTextSizeChange = viewModel::onTextSizeChange,
                onThemeSelected = viewModel::onThemeSelected,
                onReset = viewModel::onReset,
                onClose = close,
            )
        }
    }
}

@Composable
fun TextAndDisplayContent(
    uiState: DisplaySettingsUiState,
    onTextSizeChange: (Int) -> Unit,
    onThemeSelected: (ThemeSetting) -> Unit,
    onReset: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(EsSpace.s16),
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(start = EsSpace.screen, end = EsSpace.screen, bottom = EsSpace.s20),
    ) {
        SheetHeader(onReset = onReset, onClose = onClose)

        // A sample paragraph so the size can be judged on real reading text, live.
        Text(
            text = stringResource(R.string.display_preview),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background, EsRadius.image)
                .padding(horizontal = EsSpace.s16, vertical = EsSpace.s12),
        )

        TextSizeControl(step = uiState.textSizeStep, onStepChange = onTextSizeChange)

        Column(verticalArrangement = Arrangement.spacedBy(EsSpace.s8)) {
            SettingLabel(stringResource(R.string.display_theme))
            EsSegmentedControl(
                options = themeOptions,
                selected = uiState.themeSetting,
                label = { setting -> stringResource(setting.labelRes()) },
                onSelect = onThemeSelected,
            )
        }

        Button(
            onClick = onClose,
            shape = EsRadius.button,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = EsSize.primaryButton),
        ) {
            Text(text = stringResource(R.string.display_done), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun SheetHeader(onReset: () -> Unit, onClose: () -> Unit) {
    val closeDescription = stringResource(R.string.display_close_description)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.display_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        TextButton(onClick = onReset, modifier = Modifier.heightIn(min = EsSize.minTarget)) {
            Text(
                text = stringResource(R.string.display_reset),
                style = MaterialTheme.typography.labelLarge,
                textDecoration = TextDecoration.Underline,
            )
        }
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

// Small A, a seven-step slider, big A. The buttons do the same as the slider for anyone who
// finds dragging hard; at either end the button greys out and its description says why.
@Composable
private fun TextSizeControl(step: Int, onStepChange: (Int) -> Unit) {
    val lastStep = TextSize.scales.lastIndex
    val percent = (TextSize.scaleOf(step) * 100).roundToInt()
    val sizeLabel = stringResource(R.string.display_size_value, step + 1, TextSize.scales.size, percent)
    Column(verticalArrangement = Arrangement.spacedBy(EsSpace.s4)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SettingLabel(stringResource(R.string.display_text_size), modifier = Modifier.weight(1f))
            Text(text = sizeLabel, style = MaterialTheme.typography.bodyMedium, color = EsTheme.colors.muted)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            SizeStepButton(
                description = stringResource(
                    if (step > 0) R.string.display_smaller else R.string.display_smallest_reached,
                ),
                enabled = step > 0,
                onClick = { onStepChange(step - 1) },
            ) {
                Text(text = stringResource(R.string.aa_letter), style = MaterialTheme.typography.titleSmall)
            }
            Slider(
                value = step.toFloat(),
                onValueChange = { onStepChange(it.roundToInt()) },
                valueRange = 0f..lastStep.toFloat(),
                steps = lastStep - 1, // the stops between the two ends
                modifier = Modifier
                    .weight(1f)
                    .semantics { stateDescription = sizeLabel },
            )
            SizeStepButton(
                description = stringResource(
                    if (step < lastStep) R.string.display_larger else R.string.display_largest_reached,
                ),
                enabled = step < lastStep,
                onClick = { onStepChange(step + 1) },
            ) {
                Text(text = stringResource(R.string.aa_letter), style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
private fun SizeStepButton(
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .heightIn(min = EsSize.minTarget)
            .widthIn(min = EsSize.minTarget)
            .semantics { contentDescription = description },
    ) {
        content()
    }
}

@Composable
private fun SettingLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun TextAndDisplayPreview() {
    EveryStoryTheme {
        TextAndDisplayContent(
            uiState = DisplaySettingsUiState(),
            onTextSizeChange = {},
            onThemeSelected = {},
            onReset = {},
            onClose = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TextAndDisplayNightPreview() {
    EveryStoryTheme(darkTheme = true) {
        TextAndDisplayContent(
            uiState = DisplaySettingsUiState(textSizeStep = 4, themeSetting = ThemeSetting.Dark),
            onTextSizeChange = {},
            onThemeSelected = {},
            onReset = {},
            onClose = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, fontScale = 2f)
@Composable
private fun TextAndDisplayLargeTextPreview() {
    EveryStoryTheme {
        TextAndDisplayContent(
            uiState = DisplaySettingsUiState(textSizeStep = TextSize.scales.lastIndex),
            onTextSizeChange = {},
            onThemeSelected = {},
            onReset = {},
            onClose = {},
        )
    }
}
