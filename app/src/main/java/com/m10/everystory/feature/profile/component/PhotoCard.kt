package com.m10.everystory.feature.profile.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.m10.everystory.R
import com.m10.everystory.core.designsystem.theme.EsRadius
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EsSpace
import com.m10.everystory.core.designsystem.theme.EsType
import com.m10.everystory.core.designsystem.theme.EveryStoryTheme

private const val PHOTO_RATIO = 4f / 5f
private const val TILT_DEGREES = -2f // pinned up slightly crooked, like a print on a fridge

// A polaroid-style print: the photo, a wider white-ish strip underneath, and a handwritten-feel caption.
@Composable
fun PhotoCard(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(EsSpace.s12),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = EsSpace.s12)
            .graphicsLayer { rotationZ = TILT_DEGREES }
            .background(MaterialTheme.colorScheme.surfaceContainer, EsRadius.badge)
            .border(BorderStroke(EsSize.hairline, MaterialTheme.colorScheme.outlineVariant), EsRadius.badge)
            .padding(start = EsSpace.s12, end = EsSpace.s12, top = EsSpace.s12, bottom = EsSpace.s16),
    ) {
        Image(
            painter = painterResource(R.drawable.profile_photo),
            contentDescription = stringResource(R.string.profile_photo_description),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(PHOTO_RATIO)
                .clip(EsRadius.badge),
        )
        Text(
            text = stringResource(R.string.profile_photo_caption),
            style = EsType.quote,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(name = "Day", showBackground = true, widthDp = 360)
@Composable
private fun PhotoCardPreview() {
    EveryStoryTheme { PhotoCard(Modifier.padding(EsSpace.s16)) }
}

@Preview(name = "Night", showBackground = true, widthDp = 360, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PhotoCardNightPreview() {
    EveryStoryTheme(darkTheme = true) { PhotoCard(Modifier.padding(EsSpace.s16)) }
}

@Preview(name = "Large text", showBackground = true, widthDp = 360, fontScale = 2f)
@Composable
private fun PhotoCardLargeTextPreview() {
    EveryStoryTheme { PhotoCard(Modifier.padding(EsSpace.s16)) }
}
