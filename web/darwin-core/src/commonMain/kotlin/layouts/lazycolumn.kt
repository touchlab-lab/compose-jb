package org.jetbrains.compose.common.foundation.layout

import androidx.compose.runtime.Composable
import org.jetbrains.compose.common.ui.Modifier


@Composable
internal expect fun <ITEM> LazyColumnActual(modifier: Modifier, items: List<ITEM>, content: @Composable (ITEM) -> Unit)
