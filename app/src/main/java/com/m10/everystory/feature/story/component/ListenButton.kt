package com.m10.everystory.feature.story.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import com.m10.everystory.R
import com.m10.everystory.core.designsystem.icon.EsIcons
import com.m10.everystory.core.designsystem.theme.EsRadius
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EsSpace
import com.m10.everystory.core.designsystem.theme.EveryStoryTheme

// Starts read aloud, or stops it. The label changes as well as the state, so it never relies
// on TalkBack's "selected" alone.
@Composable
fun ListenButton(
    isListening: Boolean,
    minutes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = stringResource(if (isListening) R.string.listen_state_on else R.string.listen_state_off)
    OutlinedButton(
        onClick = onClick,
        shape = EsRadius.pill,
        border = BorderStroke(EsSize.outlineButton, MaterialTheme.colorScheme.primary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
        contentPadding = PaddingValues(horizontal = EsSpace.s16),
        modifier = modifier
            .heightIn(min = EsSize.minTarget)
            .semantics { stateDescription = state },
    ) {
        Icon(EsIcons.Listen, contentDescription = null, modifier = Modifier.size(EsSize.inlineIcon))
        Text(
            text = if (isListening) stringResource(R.string.listen_playing) else stringResource(R.string.listen_action, minutes),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = EsSpace.s8),
        )
    }
}

@Preview(name = "Day", showBackground = true)
@Composable
private fun ListenButtonPreview() {
    EveryStoryTheme { ListenButton(isListening = false, minutes = 1, onClick = {}) }
}

@Preview(name = "Listening, night", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ListenButtonNightPreview() {
    EveryStoryTheme(darkTheme = true) { ListenButton(isListening = true, minutes = 1, onClick = {}) }
}

@Preview(name = "Large text", showBackground = true, widthDp = 360, fontScale = 2f)
@Composable
private fun ListenButtonLargeTextPreview() {
    EveryStoryTheme { ListenButton(isListening = false, minutes = 1, onClick = {}) }
}
