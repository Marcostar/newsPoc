package com.m10.everystory.feature.developer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.m10.everystory.BuildConfig
import com.m10.everystory.R
import com.m10.everystory.core.common.AppError
import com.m10.everystory.core.designsystem.component.EsSnackbarHost
import com.m10.everystory.core.designsystem.icon.EsIcons
import com.m10.everystory.core.designsystem.theme.EsRadius
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EsSpace
import com.m10.everystory.core.designsystem.theme.EsTheme
import com.m10.everystory.core.designsystem.theme.EveryStoryTheme

@Composable
fun DeveloperSettingsRoute(
    backLabel: String,
    onBack: () -> Unit,
    onClose: () -> Unit,
    viewModel: DeveloperSettingsViewModel = viewModel(factory = DeveloperSettingsViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DeveloperSettingsScreen(
        uiState = uiState,
        baseUrl = BuildConfig.NEWS_BASE_URL,
        backLabel = backLabel,
        onBack = onBack,
        onClose = onClose,
        onInputChange = viewModel::onInputChange,
        onTestAndSave = viewModel::onTestAndSave,
        onRemoveKey = viewModel::onRemoveKey,
        onMessageShown = viewModel::onMessageShown,
    )
}

@Composable
fun DeveloperSettingsScreen(
    uiState: DeveloperSettingsUiState,
    baseUrl: String,
    backLabel: String,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onInputChange: (String) -> Unit,
    onTestAndSave: () -> Unit,
    onRemoveKey: () -> Unit,
    onMessageShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val savedText = stringResource(R.string.developer_snackbar_saved)
    val removedText = stringResource(R.string.developer_snackbar_removed)
    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(if (message == DeveloperMessage.KeySaved) savedText else removedText)
        onMessageShown()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DeveloperTopBar(backLabel = backLabel, onBack = onBack, onClose = onClose)
            Column(
                verticalArrangement = Arrangement.spacedBy(EsSpace.s16),
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(start = EsSpace.screen, end = EsSpace.screen, top = EsSpace.s16, bottom = EsSize.snackbarClearance),
            ) {
                Text(
                    text = stringResource(R.string.developer_eyebrow).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = EsTheme.colors.coralInk,
                )
                Text(
                    text = stringResource(R.string.developer_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = stringResource(R.string.developer_intro),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                KeyInUse(ownKeyEnding = uiState.ownKeyEnding)
                KeyField(
                    input = uiState.input,
                    error = uiState.inputError,
                    enabled = !uiState.isChecking,
                    onInputChange = onInputChange,
                    onDone = onTestAndSave,
                )
                TestAndSaveButton(isChecking = uiState.isChecking, onClick = onTestAndSave)
                if (uiState.ownKeyEnding != null) {
                    TextButton(
                        onClick = onRemoveKey,
                        modifier = Modifier.heightIn(min = EsSize.minTarget),
                    ) {
                        Text(text = stringResource(R.string.developer_remove_key), style = MaterialTheme.typography.labelLarge)
                    }
                }
                RequestsCard(baseUrl = baseUrl)
                Text(
                    text = stringResource(R.string.developer_footer),
                    style = MaterialTheme.typography.bodyMedium,
                    color = EsTheme.colors.muted,
                )
            }
        }
        EsSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(EsSpace.s12),
        )
    }
}

// Same pair as You: a labelled way back (to wherever it was opened from), and Close to Today.
@Composable
private fun DeveloperTopBar(backLabel: String, onBack: () -> Unit, onClose: () -> Unit) {
    val backDescription = stringResource(R.string.back_to, backLabel)
    val closeDescription = stringResource(R.string.close_to_today)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = EsSpace.s8, vertical = EsSpace.s4),
    ) {
        TextButton(
            onClick = onBack,
            modifier = Modifier
                .heightIn(min = EsSize.minTarget)
                .semantics { contentDescription = backDescription },
        ) {
            Icon(EsIcons.Back, contentDescription = null)
            Text(
                text = backLabel,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = EsSpace.s8),
            )
        }
        Spacer(Modifier.weight(1f))
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
private fun KeyInUse(ownKeyEnding: String?) {
    val value = ownKeyEnding?.let { stringResource(R.string.developer_own_key, it) }
        ?: stringResource(R.string.developer_builtin_key)
    // Read out as words; "dot dot dot dot" helps nobody.
    val description = ownKeyEnding?.let { stringResource(R.string.developer_using_own_description, it) }
        ?: "${stringResource(R.string.developer_using)} $value"
    Surface(
        shape = EsRadius.card,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = description
                liveRegion = LiveRegionMode.Polite
            },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EsSpace.s12),
            modifier = Modifier.padding(EsSpace.s16),
        ) {
            Icon(EsIcons.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.developer_using))
                    append(' ')
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(value) }
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun KeyField(
    input: String,
    error: KeyInputError?,
    enabled: Boolean,
    onInputChange: (String) -> Unit,
    onDone: () -> Unit,
) {
    // Hidden by default, like a password: people demo this on a shared screen.
    var showKey by rememberSaveable { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    val toggleState = stringResource(if (showKey) R.string.developer_key_shown else R.string.developer_key_hidden)

    Column(verticalArrangement = Arrangement.spacedBy(EsSpace.s8)) {
        Text(
            text = stringResource(R.string.developer_key_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        OutlinedTextField(
            value = input,
            onValueChange = onInputChange,
            enabled = enabled,
            singleLine = true,
            isError = error != null,
            placeholder = { Text(stringResource(R.string.developer_key_placeholder)) },
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
            visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = {
                keyboard?.hide()
                onDone()
            }),
            trailingIcon = {
                TextButton(
                    onClick = { showKey = !showKey },
                    modifier = Modifier
                        .heightIn(min = EsSize.minTarget)
                        .semantics { stateDescription = toggleState },
                ) {
                    Text(
                        text = stringResource(if (showKey) R.string.developer_key_hide else R.string.developer_key_show),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            },
            supportingText = { KeyHelp(error) },
            shape = EsRadius.button,
            // No red: the error state is ink, an icon and a sentence.
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun KeyHelp(error: KeyInputError?) {
    if (error == null) {
        Text(
            text = stringResource(R.string.developer_key_help),
            style = MaterialTheme.typography.bodyMedium,
            color = EsTheme.colors.muted,
        )
        return
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(EsSpace.s4),
        modifier = Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
    ) {
        Icon(
            EsIcons.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(EsSize.inlineIcon),
        )
        Text(
            text = stringResource(error.messageRes()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun KeyInputError.messageRes(): Int = when (this) {
    KeyInputError.Empty -> R.string.developer_key_empty
    KeyInputError.WrongFormat -> R.string.developer_key_wrong_format
    KeyInputError.Rejected -> R.string.developer_key_rejected
    is KeyInputError.CouldNotCheck -> when (error) {
        AppError.Offline -> R.string.developer_key_offline
        AppError.RateLimited -> R.string.developer_key_rate_limited
        AppError.Timeout, AppError.Server, AppError.Unauthorized, is AppError.Unknown -> R.string.developer_key_no_response
    }
}

// The one primary action. While checking, the words change as well as the colour.
@Composable
private fun TestAndSaveButton(isChecking: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isChecking,
        shape = EsRadius.button,
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = EsTheme.colors.disabledFill,
            disabledContentColor = EsTheme.colors.disabledText,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = EsSize.primaryButton),
    ) {
        if (isChecking) {
            CircularProgressIndicator(
                strokeWidth = EsSize.focusRing,
                color = EsTheme.colors.disabledText,
                modifier = Modifier.size(EsSize.progressSmall),
            )
            Spacer(Modifier.width(EsSpace.s8))
        }
        Text(
            text = stringResource(if (isChecking) R.string.developer_checking else R.string.developer_test_and_save),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun RequestsCard(baseUrl: String) {
    Surface(
        shape = EsRadius.card,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(EsSpace.s8),
            modifier = Modifier.padding(EsSpace.s16),
        ) {
            Text(
                text = stringResource(R.string.developer_requests_label).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = EsTheme.colors.muted,
            )
            val code = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
            Text(stringResource(R.string.developer_requests_headlines, baseUrl), style = code, color = MaterialTheme.colorScheme.onSurface)
            Text(stringResource(R.string.developer_requests_search, baseUrl), style = code, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = stringResource(R.string.developer_requests_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(name = "Built-in key, day", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun DeveloperSettingsPreview() {
    EveryStoryTheme {
        DeveloperSettingsScreen(
            uiState = DeveloperSettingsUiState(input = "not-a-key", inputError = KeyInputError.WrongFormat),
            baseUrl = "https://newsapi.org/v2/",
            backLabel = "Today",
            onBack = {},
            onClose = {},
            onInputChange = {},
            onTestAndSave = {},
            onRemoveKey = {},
            onMessageShown = {},
        )
    }
}

@Preview(name = "Own key, night", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun DeveloperSettingsNightPreview() {
    EveryStoryTheme(darkTheme = true) {
        DeveloperSettingsScreen(
            uiState = DeveloperSettingsUiState(ownKeyEnding = "1a2b", isChecking = true),
            baseUrl = "https://newsapi.org/v2/",
            backLabel = "Today",
            onBack = {},
            onClose = {},
            onInputChange = {},
            onTestAndSave = {},
            onRemoveKey = {},
            onMessageShown = {},
        )
    }
}

@Preview(name = "Large text", showBackground = true, widthDp = 360, heightDp = 1400, fontScale = 2f)
@Composable
private fun DeveloperSettingsLargeTextPreview() {
    EveryStoryTheme {
        DeveloperSettingsScreen(
            uiState = DeveloperSettingsUiState(),
            baseUrl = "https://newsapi.org/v2/",
            backLabel = "Today",
            onBack = {},
            onClose = {},
            onInputChange = {},
            onTestAndSave = {},
            onRemoveKey = {},
            onMessageShown = {},
        )
    }
}
