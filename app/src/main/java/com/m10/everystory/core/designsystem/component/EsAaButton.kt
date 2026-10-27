package com.m10.everystory.core.designsystem.component

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
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
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EveryStoryTheme

// "Aa" in the headline serif, the reading-app convention for text settings. Icon-like, so it
// gets a full description for TalkBack.
@Composable
fun EsAaButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.aa_description)
    TextButton(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = EsSize.minTarget)
            .widthIn(min = EsSize.minTarget)
            .semantics { contentDescription = description },
    ) {
        Text(text = stringResource(R.string.aa_label), style = MaterialTheme.typography.titleLarge)
    }
}

@Preview(showBackground = true)
@Composable
private fun EsAaButtonPreview() {
    EveryStoryTheme { EsAaButton(onClick = {}) }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EsAaButtonNightPreview() {
    EveryStoryTheme(darkTheme = true) { EsAaButton(onClick = {}) }
}
