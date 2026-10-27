package com.m10.everystory.feature.profile.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EsTheme
import com.m10.everystory.core.ui.LocalReducedMotion
import kotlin.math.sin
import kotlin.random.Random

private const val PIECE_COUNT = 48
private const val DURATION_MS = 1_800
private const val FADE_FROM = 0.75f // the last quarter fades out
private const val SEED = 2026 // same burst every time, so previews and screenshots match

private class Piece(random: Random) {
    val x = random.nextFloat()
    val startY = -random.nextFloat() * 0.3f
    val fall = 0.7f + random.nextFloat() * 0.6f
    val sway = 4f + random.nextFloat() * 6f
    val spin = (random.nextFloat() - 0.5f) * 4f
    val colorIndex = random.nextInt(4)
    val stretch = 1f + random.nextFloat()
}

/**
 * A one-off burst of the app's own colours falling over the page. Drawn in a single Canvas and
 * animated in the draw phase, so it never recomposes the page. Skipped when the reader has asked
 * for less motion. Doesn't take touches.
 */
@Composable
fun Confetti(modifier: Modifier = Modifier) {
    if (LocalReducedMotion.current) return
    val colors = listOf(
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.primary,
        EsTheme.colors.sageMark,
        MaterialTheme.colorScheme.secondaryContainer,
    )
    val pieces = remember { Random(SEED).let { random -> List(PIECE_COUNT) { Piece(random) } } }
    val pieceSize = with(LocalDensity.current) { EsSize.confettiPiece.toPx() }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(DURATION_MS, easing = LinearEasing)) }

    Canvas(modifier = modifier) {
        val t = progress.value
        if (t >= 1f) return@Canvas
        val alpha = if (t < FADE_FROM) 1f else (1f - t) / (1f - FADE_FROM)
        pieces.forEach { piece ->
            // Falls with a little gravity and drifts side to side as it goes.
            val x = piece.x * size.width + sin(t * piece.sway) * pieceSize * 2
            val y = (piece.startY + piece.fall * t * t) * size.height
            val color: Color = colors[piece.colorIndex].copy(alpha = alpha)
            rotate(degrees = piece.spin * t * 360f, pivot = Offset(x, y)) {
                drawRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(pieceSize, pieceSize * piece.stretch),
                )
            }
        }
    }
}
