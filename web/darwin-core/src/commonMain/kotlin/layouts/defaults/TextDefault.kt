package org.jetbrains.compose.common.material

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit

@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier.Companion,
    color: Color = Color.Black,
    size: TextUnit = TextUnit.Unspecified
) {
    TextActual(text, modifier, color, size)
}
