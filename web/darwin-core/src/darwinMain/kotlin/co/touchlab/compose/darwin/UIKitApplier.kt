package co.touchlab.compose.darwin

import androidx.compose.runtime.AbstractApplier
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.convert
import platform.UIKit.UIControl
import platform.UIKit.UIControlEventTouchUpInside
import platform.UIKit.UIView
import platform.UIKit.bottomAnchor
import platform.UIKit.insertSubview
import platform.UIKit.leadingAnchor
import platform.UIKit.removeFromSuperview
import platform.UIKit.setFrame
import platform.UIKit.subviews
import platform.UIKit.topAnchor
import platform.UIKit.trailingAnchor
import platform.darwin.NSInteger
import platform.objc.sel_registerName

class UIKitApplier(root: UIKitWrapper<*>) : AbstractApplier<UIKitWrapper<*>>(root) {
  override fun onClear() {
    // or current.node.clear()?; in all examples it calls 'clear' on the root
    root.view.subviews.forEach { v ->
      (v as UIView).removeFromSuperview()
    }
  }

  override fun insertBottomUp(index: Int, instance: UIKitWrapper<*>) {
    current.insert(index, instance)
  }

  override fun insertTopDown(index: Int, instance: UIKitWrapper<*>) {
    // ignored. Building tree bottom-up
  }

  override fun move(from: Int, to: Int, count: Int) {
    current.move(from, to, count)
  }

  override fun remove(index: Int, count: Int) {
    current.remove(index, count)
  }
}

interface UIKitWrapper<TView: UIView> {
  val view: TView

  fun insert(index: Int, nodeWrapper: UIKitWrapper<*>) {
    view.insertSubview(nodeWrapper.view, index.convert<NSInteger>())
  }

  fun remove(index: Int, count: Int) {
    view.subviews.subList(index, index+count).forEach { (it as UIView).removeFromSuperview() }
  }

  fun move(from: Int, to: Int, count: Int) {
    if (from == to) {
      return // nothing to do
    }

    val allViews = view.subviews.subList(from, from + count).map { it as UIView }

    repeat(count) { vindex ->
      allViews[vindex].removeFromSuperview()
    }

    repeat(count) { vindex ->
      view.insertSubview(allViews[vindex], (to + vindex).convert<NSInteger>())
    }
  }
}

class UIViewWrapper<TView : UIView>(override val view: TView) : UIKitWrapper<TView>{
    private var onClick: (() -> Unit)? = null

    private val clickedPointer = sel_registerName("clicked")

    fun updateOnClick(onClick: () -> Unit) {
        if(this.onClick == null) {
            (view as UIControl).addTarget(this, clickedPointer, UIControlEventTouchUpInside)
        }
        this.onClick = onClick
    }

    @ObjCAction
    fun clicked() {
        onClick?.invoke()
    }
}

/*open class UIClickableViewWrapper<TView : UIView>(view: TView) : UIViewWrapper<TView>(view) {
    private var onClick: (() -> Unit)? = null

    fun updateOnClick(onClick: () -> Unit) {
        if(this.onClick == null) {
            (view as UIControl).addTarget(this, sel_registerName("clicked"), UIControlEventTouchUpInside)
        }
        this.onClick = onClick
    }

    @ObjCAction
    fun clicked() {
        this.onClick?.invoke()
    }
}*/

class RootUIKitWrapper(override val view: UIView): UIKitWrapper<UIView>