package com.m10.everystory.feature.today.component

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.m10.everystory.R
import com.m10.everystory.core.designsystem.icon.EsIcons
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EsSpace
import com.m10.everystory.core.designsystem.theme.EveryStoryTheme
import com.m10.everystory.core.ui.englishNameRes
import com.m10.everystory.domain.model.NewsLanguage

// Translate icon and the language code ("EN"), as in the prototype. The code alone means little
// when spoken, so TalkBack hears the full language name.
@Composable
fun LanguageButton(
    language: NewsLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.language_button_description, stringResource(language.englishNameRes()))
    TextButton(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = EsSize.minTarget)
            .widthIn(min = EsSize.minTarget)
            .semantics { contentDescription = description },
    ) {
        Icon(EsIcons.Language, contentDescription = null)
        Text(
            text = language.code.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = EsSpace.s4),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LanguageButtonPreview() {
    EveryStoryTheme { LanguageButton(language = NewsLanguage.German, onClick = {}) }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LanguageButtonNightPreview() {
    EveryStoryTheme(darkTheme = true) { LanguageButton(language = NewsLanguage.Arabic, onClick = {}) }
}
