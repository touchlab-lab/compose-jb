package org.jetbrains.compose.common.foundation.layout

import org.jetbrains.compose.common.ui.Modifier
import androidx.compose.runtime.Composable

@Composable
internal expect fun ColumnActual(modifier: Modifier, scrollDirection: ScrollDirection?, content: @Composable () -> Unit)

enum class ScrollDirection {
    HORIZONTAL,
    VERTICAL,
}