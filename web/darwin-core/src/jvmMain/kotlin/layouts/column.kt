package org.jetbrains.compose.common.foundation.layout

import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import org.jetbrains.compose.common.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column as JColumn
import org.jetbrains.compose.common.ui.implementation

@Composable
internal actual fun ColumnActual(modifier: Modifier, scrollDirection: ScrollDirection?, content: @Composable () -> Unit) {
    if (scrollDirection != null) {
        val scrollState = rememberScrollState()
        val scrollableModifier = when (scrollDirection) {
            ScrollDirection.HORIZONTAL -> modifier.implementation.horizontalScroll(scrollState)
            ScrollDirection.VERTICAL -> modifier.implementation.verticalScroll(scrollState)
        }

        JColumn(modifier = scrollableModifier) {
            content.invoke()
        }
    }
}