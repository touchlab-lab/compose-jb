package org.jetbrains.compose.common.foundation.layout

import androidx.compose.runtime.Composable
import org.jetbrains.compose.common.ui.Modifier
import org.jetbrains.compose.web.TableView
import org.jetbrains.compose.web.VStack

@Composable
internal actual fun <ITEM> LazyColumnActual(
    modifier: Modifier,
    items: List<ITEM>,
    content: @Composable (ITEM) -> Unit
) {
    TableView(items, content)
}