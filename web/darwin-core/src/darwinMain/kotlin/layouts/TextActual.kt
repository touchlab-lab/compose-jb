package org.jetbrains.compose.common.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import co.touchlab.compose.darwin.UIKitApplier
import co.touchlab.compose.darwin.UIViewWrapper
import co.touchlab.compose.darwin.internal.castOrCreate
import org.jetbrains.compose.common.core.graphics.Color
import org.jetbrains.compose.common.core.graphics.toUIColor
import org.jetbrains.compose.common.ui.Modifier
import org.jetbrains.compose.common.ui.unit.TextUnit
import platform.UIKit.UILabel
import platform.UIKit.UIView
import androidx.compose.ui.platform.UIKitView

@Composable
actual fun TextActual(
    text: String,
    modifier: Modifier,
    color: Color,
    size: TextUnit
) {
    UIKitView(
        factory = { UILabel() },
//        modifier = modifier,
        update = { label ->
            label.text = text
            label.textColor = color.toUIColor()
            label.font = label.font.fontWithSize(size.value.toDouble())
//            modifier.castOrCreate().modHandlers.forEach { block -> block.invoke(label) }
        }
    )
}
