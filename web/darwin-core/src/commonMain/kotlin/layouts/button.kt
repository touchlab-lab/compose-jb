package org.jetbrains.compose.common.material

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun ButtonActual(
    modifier: Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
)
