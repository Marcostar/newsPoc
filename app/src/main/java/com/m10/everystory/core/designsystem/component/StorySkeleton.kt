package com.m10.everystory.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.m10.everystory.core.designsystem.theme.EsRadius
import com.m10.everystory.core.designsystem.theme.EsSize
import com.m10.everystory.core.designsystem.theme.EsSpace
import com.m10.everystory.core.designsystem.theme.EsTheme

private const val SKELETON_ROWS = 4
private const val SHORT_LINE = 0.4f
private const val LONG_LINE = 0.9f
private const val LEAD_IMAGE_RATIO = 16f / 9f

// Static on purpose: the design asks for no shimmer, which also covers reduced motion.
@Composable
fun StorySkeleton(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(EsSpace.s24),
        modifier = modifier.clearAndSetSemantics { },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(EsSpace.s8)) {
            Block(Modifier.fillMaxWidth().aspectRatio(LEAD_IMAGE_RATIO))
            Line(SHORT_LINE)
            Line(LONG_LINE)
        }
        repeat(SKELETON_ROWS) {
            Row(horizontalArrangement = Arrangement.spacedBy(EsSpace.s16)) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(EsSpace.s8),
                    modifier = Modifier.weight(1f).padding(top = EsSpace.s4),
                ) {
                    Line(SHORT_LINE)
                    Line(LONG_LINE)
                }
                Block(Modifier.size(EsSize.thumbnail))
            }
        }
    }
}

@Composable
private fun Line(fraction: Float) {
    Block(Modifier.fillMaxWidth(fraction).height(EsSize.skeletonLine))
}

@Composable
private fun Block(modifier: Modifier) {
    Box(modifier.background(EsTheme.colors.placeholderA, EsRadius.image))
}
