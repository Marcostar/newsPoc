package com.m10.everystory.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import com.m10.everystory.R
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EsSpace
import com.m10.everystory.core.designsystem.theme.EsTheme

@Composable
fun EsLogoLockup(
    modifier: Modifier = Modifier,
    markSize: Dp = EsSize.logoMarkInline,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(EsSpace.s8),
    ) {
        Image(
            painter = painterResource(if (EsTheme.isDark) R.drawable.ic_logo_mark_night else R.drawable.ic_logo_mark),
            contentDescription = null,
            modifier = Modifier.size(markSize),
        )
        EsWordmark(style = textStyle)
    }
}

// The italic "Every" belongs to the wordmark only; running text writes EveryStory plainly.
@Composable
fun EsWordmark(
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
) {
    val every = stringResource(R.string.wordmark_every)
    val story = stringResource(R.string.wordmark_story)
    val quote = stringResource(R.string.wordmark_quote)
    val quoteColor = MaterialTheme.colorScheme.tertiary
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Normal)) { append(every) }
            append(story)
            withStyle(SpanStyle(color = quoteColor)) { append(quote) }
        },
        style = style,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}
