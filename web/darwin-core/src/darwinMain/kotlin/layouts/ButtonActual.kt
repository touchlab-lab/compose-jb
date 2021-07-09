package org.jetbrains.compose.common.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.UIKitView
import androidx.compose.ui.platform.setContent
import co.touchlab.compose.darwin.UIControlWrapper
import co.touchlab.compose.darwin.UIKitApplier
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.readValue
import org.jetbrains.compose.common.core.graphics.toUIColor
import platform.CoreGraphics.CGRectZero
import platform.UIKit.*
import platform.objc.sel_registerName

@Composable
actual fun ButtonActual(
    modifier: Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val parent = rememberCompositionContext()
    UIKitView(
        factory = { ComposeContentButton(parent, content) },
        modifier = modifier,
        update = { button ->
            button.onClick = onClick
        }
    )
}

private class ComposeContentButton(
    parent: CompositionContext?,
    private val content: @Composable () -> Unit
): UIControl(CGRectZero.readValue()) {
    private val clickedPointer = sel_registerName("clicked")

//    var content: @Composable () -> Unit = { }
    var onClick: () -> Unit = { }
    val composition = setContent(parent) {
        content()
    }

    init {
        addTarget(this, clickedPointer, UIControlEventTouchUpInside)
    }

    @ObjCAction
    fun clicked() {
        println("I've been clicked")
        onClick()
    }
}