package com.m10.everystory.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.m10.everystory.core.designsystem.theme.EsTheme

// Slightly above centre keeps heads in frame in most news photos.
private val NewsPhotoFocus = BiasAlignment(horizontalBias = 0f, verticalBias = -0.3f)

// Renders nothing if the image fails, so cards fall back to their no-image layout
// instead of showing a broken frame.
@Composable
fun ArticleImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    var failed by remember(url) { mutableStateOf(false) }
    if (failed) return

    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        placeholder = ColorPainter(EsTheme.colors.placeholderA),
        contentScale = ContentScale.Crop,
        alignment = NewsPhotoFocus,
        onError = { failed = true },
        modifier = modifier,
    )
}
