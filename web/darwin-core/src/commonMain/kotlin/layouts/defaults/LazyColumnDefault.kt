package org.jetbrains.compose.common.foundation.layout

import androidx.compose.runtime.Composable
import org.jetbrains.compose.common.ui.Modifier

@Composable
fun <ITEM> LazyColumn(
    modifier: Modifier = Modifier.Companion,
    items: List<ITEM>,
    content: @Composable (ITEM) -> Unit
) { LazyColumnActual(modifier, items, content) }
