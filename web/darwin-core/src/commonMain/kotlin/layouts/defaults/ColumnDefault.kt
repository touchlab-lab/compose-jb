package org.jetbrains.compose.common.foundation.layout

import org.jetbrains.compose.common.ui.Modifier
import androidx.compose.runtime.Composable

@Composable
fun Column(
    modifier: Modifier = Modifier.Companion,
    scrollDirection: ScrollDirection? = null,
    content: @Composable () -> Unit
) { ColumnActual(modifier, scrollDirection, content) }
