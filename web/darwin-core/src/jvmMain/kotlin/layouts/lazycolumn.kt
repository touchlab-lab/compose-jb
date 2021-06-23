package org.jetbrains.compose.common.foundation.layout

import androidx.compose.foundation.lazy.items
import org.jetbrains.compose.common.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column as JColumn
import androidx.compose.foundation.lazy.LazyColumn as JLazyColumn
import org.jetbrains.compose.common.ui.implementation

@Composable
internal actual fun <ITEM> LazyColumnActual(modifier: Modifier, items: List<ITEM>, content: @Composable (ITEM) -> Unit) {
    JLazyColumn(modifier = modifier.implementation) {
        items(items) {
            content(it)
        }
    }
}