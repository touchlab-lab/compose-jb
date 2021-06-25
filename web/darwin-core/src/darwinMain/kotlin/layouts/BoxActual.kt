package org.jetbrains.compose.common.foundation.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import co.touchlab.compose.darwin.UIKitApplier
import co.touchlab.compose.darwin.UIViewWrapper
import co.touchlab.compose.darwin.internal.castOrCreate
import org.jetbrains.compose.common.ui.Modifier
import org.jetbrains.compose.web.ZStack
import platform.UIKit.UILabel
import platform.UIKit.UIView
import platform.UIKit.addSubview
import platform.UIKit.sizeToFit

@Composable
internal actual fun BoxActual(
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    ZStack(modifier, content)
}