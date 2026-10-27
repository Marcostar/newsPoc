package com.m10.everystory.core.designsystem.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.m10.everystory.core.designsystem.theme.EsRadius

// A Material bottom sheet in EveryStory's style: card colour, no tonal tint, opens fully.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EsBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    content: @Composable ColumnScope.() -> Unit,
) {
    // The sheet opens in its own window, which starts from the phone's density and loses the
    // in-app text size. Hand it the app's density so its text matches everything else.
    val appDensity = LocalDensity.current
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = EsRadius.sheet,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
    ) {
        CompositionLocalProvider(LocalDensity provides appDensity) {
            content()
        }
    }
}
