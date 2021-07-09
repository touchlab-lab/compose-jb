package org.jetbrains.compose.common.material

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit

@Composable
expect fun TextActual(
    text: String,
    modifier: Modifier,
    color: Color,
    size: TextUnit,
)
