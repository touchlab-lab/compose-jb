package org.jetbrains.compose.common.material

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.material.Text as JText

@Composable
actual fun TextActual(
    text: String,
    modifier: Modifier,
    color: Color,
    size: TextUnit,
) {
    JText(
        text,
        modifier = modifier,
        color = color,
        fontSize = size,
    )
}
