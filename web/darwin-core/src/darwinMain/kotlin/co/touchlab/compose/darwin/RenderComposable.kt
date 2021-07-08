package org.jetbrains.compose.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.ControlledComposition
import androidx.compose.runtime.DefaultMonotonicFrameClock
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.snapshots.ObserverHandle
import androidx.compose.runtime.snapshots.Snapshot
import co.touchlab.compose.darwin.RootUIKitWrapper
import co.touchlab.compose.darwin.UIKitApplier
import co.touchlab.compose.darwin.UIKitWrapper
import co.touchlab.compose.darwin.internal.castOrCreate
import kotlinx.cinterop.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.jetbrains.compose.common.foundation.layout.ScrollDirection
import org.jetbrains.compose.common.ui.Modifier
import org.jetbrains.compose.web.GlobalSnapshotManager.ensureStarted
import platform.CoreFoundation.CFStringRef
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSCoder
import platform.Foundation.NSIndexPath
import platform.Foundation.NSString
import platform.UIKit.*
import platform.darwin.NSInteger
import platform.darwin.NSObject
import platform.darwin.NSObjectMeta
import platform.darwin.StringPtrVar
import platform.objc.sel_registerName
import kotlin.properties.Delegates

interface UIViewScope<out TView : UIView>

/**
 * Platform-specific mechanism for starting a monitor of global snapshot state writes
 * in order to schedule the periodic dispatch of snapshot apply notifications.
 * This process should remain platform-specific; it is tied to the threading and update model of
 * a particular platform and framework target.
 *
 * Composition bootstrapping mechanisms for a particular platform/framework should call
 * [ensureStarted] during setup to initialize periodic global snapshot notifications.
 */
@ThreadLocal
internal object GlobalSnapshotManager {
    private var started = false
    private var commitPending = false
    private var removeWriteObserver: (ObserverHandle)? = null

    private val scheduleScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun ensureStarted() {
        if (!started) {
            started = true
            removeWriteObserver = Snapshot.registerGlobalWriteObserver(globalWriteObserver)
        }
    }

    private val globalWriteObserver: (Any) -> Unit = {
        // Race, but we don't care too much if we end up with multiple calls scheduled.
        if (!commitPending) {
            commitPending = true
            schedule {
                commitPending = false
                Snapshot.sendApplyNotifications()
            }
        }
    }

    /**
     * List of deferred callbacks to run serially. Guarded by its own monitor lock.
     */
    private val scheduledCallbacks = mutableListOf<() -> Unit>()

    /**
     * Guarded by [scheduledCallbacks]'s monitor lock.
     */
    private var isSynchronizeScheduled = false

    /**
     * Synchronously executes any outstanding callbacks and brings snapshots into a
     * consistent, updated state.
     */
    private fun synchronize() {
        scheduledCallbacks.forEach { it.invoke() }
        scheduledCallbacks.clear()
        isSynchronizeScheduled = false
    }

    private fun schedule(block: () -> Unit) {
        scheduledCallbacks.add(block)
        if (!isSynchronizeScheduled) {
            isSynchronizeScheduled = true
            scheduleScope.launch { synchronize() }
        }
    }
}
/*
@Composable
fun Text(value: String) {
    ComposeNode<UIViewWrapper<UILabel>, UIKitApplier>(
        factory = { UIViewWrapper(UILabel()) },
        update = {
            set(value) { value -> view.text = value }
        },
    )
}

@Composable
fun Button(
    value: String,
    onClick: () -> Unit
) {
    ComposeNode<UIControlWrapper<UIButton>, UIKitApplier>(
        factory = { UIControlWrapper(UIButton().apply {
            setTitleColor(UIColor.blackColor, UIControlStateNormal)
        }) },
        update = {
            set(value) { value -> view.setTitle(value, UIControlStateNormal) }
            set(onClick) { oc -> updateOnClick(onClick) }
        },
    )
}*/

/*
@Composable
fun KotlinButton(
    value: String,
    onClick: () -> Unit
) {
    ComposeNode<UIViewWrapper<KButton>, UIKitApplier>(
        factory = { UIViewWrapper(KButton().apply {
            setTitleColor(UIColor.blackColor, UIControlStateNormal)
        }) },
        update = {
            set(value) { value -> view.setTitle(value, UIControlStateNormal) }
            set(onClick) { oc -> view.updateOnClick(onClick) }
        },
    )
}
*/

private fun makeRect() = CGRectMake(0.toDouble(), 0.toDouble(), 300.toDouble(), 100.toDouble())

internal class KButton():UIButton(frame = makeRect()){
    private var onClick: () -> Unit = {}

    fun updateOnClick(onClick: () -> Unit){
        this.onClick = onClick
    }

    init {
        addTarget(this, sel_registerName("clicked"), UIControlEventTouchUpInside)
    }

    @ObjCAction
    fun clicked() {
        this.onClick()
    }
}

class UIStackViewWrapper(override val view: UIStackView): UIKitWrapper<UIStackView> {
    override fun insert(index: Int, nodeWrapper: UIKitWrapper<*>){
        view.insertArrangedSubview(nodeWrapper.view, index.convert())
    }

    override fun remove(index: Int, count: Int) {
        super.remove(index, count)
    }

    override fun move(from: Int, to: Int, count: Int) {
        if (from == to) {
            return // nothing to do
        }

        val allViews = view.subviews.subList(from, from + count).map { it as UIView }

        repeat(count) { vindex ->
            allViews[vindex].removeFromSuperview()
        }

        repeat(count) { vindex ->
            view.insertArrangedSubview(allViews[vindex], (to + vindex).convert())
        }
    }
}

class VStackViewWrapper(private val childrenContainer: UIView): UIKitWrapper<UIView> {
    var spacing by Delegates.observable(0.0) { _, _, _ ->
        reapplyConstraints()
    }

    var scrollDirection: ScrollDirection? by Delegates.observable(null) { _, oldDirection, newDirection ->
        if (oldDirection != newDirection) {
            arrangeContainers()
        }
    }

    private var managedConstraints = emptyList<NSLayoutConstraint>()

    override val view: UIView = UIView()
    private var scrollView: UIScrollView? = null

    init {
        childrenContainer.translatesAutoresizingMaskIntoConstraints = false
        arrangeContainers()
    }

    override fun insert(index: Int, nodeWrapper: UIKitWrapper<*>) {
        childrenContainer.insertSubview(nodeWrapper.view, index.convert<NSInteger>())

        nodeWrapper.view.translatesAutoresizingMaskIntoConstraints = false
        nodeWrapper.view.setContentHuggingPriority(UILayoutPriorityRequired, UILayoutConstraintAxisHorizontal)
        nodeWrapper.view.setContentHuggingPriority(UILayoutPriorityRequired, UILayoutConstraintAxisVertical)

        reapplyConstraints()
    }

    override fun remove(index: Int, count: Int) {
        childrenContainer.subviews.subList(index, index+count).forEach { (it as UIView).removeFromSuperview() }

        reapplyConstraints()
    }

    override fun move(from: Int, to: Int, count: Int) {
        if (from == to) {
            return // nothing to do
        }

        val allViews = childrenContainer.subviews.subList(from, from + count).map { it as UIView }

        repeat(count) { vindex ->
            allViews[vindex].removeFromSuperview()
        }

        repeat(count) { vindex ->
            childrenContainer.insertSubview(allViews[vindex], (to + vindex).convert<NSInteger>())
        }

        reapplyConstraints()
    }

    private fun arrangeContainers() {
        childrenContainer.removeFromSuperview()
        scrollView?.removeFromSuperview()

        val scrollDirection = scrollDirection

        if (scrollDirection == null) {
            scrollView = null
            view.addSubview(childrenContainer)

            listOf(
                childrenContainer.leadingAnchor.constraintEqualToAnchor(view.layoutMarginsGuide.leadingAnchor),
                childrenContainer.topAnchor.constraintEqualToAnchor(view.layoutMarginsGuide.topAnchor),
                childrenContainer.trailingAnchor.constraintEqualToAnchor(view.layoutMarginsGuide.trailingAnchor),
                childrenContainer.bottomAnchor.constraintEqualToAnchor(view.layoutMarginsGuide.bottomAnchor).apply {
                    priority = UILayoutPriorityDefaultHigh
                },
            ).forEach { it.active = true }
        } else {
            val scrollView = scrollView ?: UIScrollView().apply {
                backgroundColor = UIColor.clearColor
                translatesAutoresizingMaskIntoConstraints = false
            }.also { scrollView = it }
            view.addSubview(scrollView)
            scrollView.addSubview(childrenContainer)
            listOf(
                scrollView.leadingAnchor.constraintEqualToAnchor(view.leadingAnchor),
                scrollView.topAnchor.constraintEqualToAnchor(view.topAnchor),
                scrollView.trailingAnchor.constraintEqualToAnchor(view.trailingAnchor),
                scrollView.bottomAnchor.constraintEqualToAnchor(view.bottomAnchor),
            ).forEach { it.active = true }

            when (scrollDirection) {
                ScrollDirection.HORIZONTAL -> {
                    childrenContainer.heightAnchor.constraintLessThanOrEqualToAnchor(scrollView.heightAnchor)
                }
                ScrollDirection.VERTICAL -> {
                    childrenContainer.widthAnchor.constraintLessThanOrEqualToAnchor(scrollView.widthAnchor)
                }
            }.active = true

            listOf(
                childrenContainer.leadingAnchor.constraintEqualToAnchor(scrollView.contentLayoutGuide.leadingAnchor),
                childrenContainer.topAnchor.constraintEqualToAnchor(scrollView.contentLayoutGuide.topAnchor),
                childrenContainer.trailingAnchor.constraintEqualToAnchor(scrollView.contentLayoutGuide.trailingAnchor),
                childrenContainer.bottomAnchor.constraintEqualToAnchor(scrollView.contentLayoutGuide.bottomAnchor),
            ).forEach { it.active = true }
        }
    }

    private fun reapplyConstraints() {
        // We want to store these so they don't change during computation.
        val immutableSpacing = spacing
        val subviews = childrenContainer.subviews.filterIsInstance<UIView>()

        childrenContainer.removeConstraints(managedConstraints)

        val constraints = mutableListOf<NSLayoutConstraint>()
        for (index in childrenContainer.subviews.indices) {
            val current = subviews[index]
            val next = subviews.getOrNull(index + 1)

            constraints.add(
                current.leadingAnchor.constraintEqualToAnchor(childrenContainer.layoutMarginsGuide.leadingAnchor)
            )

            constraints.add(
                current.trailingAnchor.constraintEqualToAnchor(childrenContainer.layoutMarginsGuide.trailingAnchor)
            )

            if (index == 0) {
                constraints.add(
                    current.topAnchor.constraintEqualToAnchor(childrenContainer.layoutMarginsGuide.topAnchor)
                )
            }

            constraints.add(
                if (next != null) {
                    next.topAnchor.constraintEqualToAnchor(current.bottomAnchor, immutableSpacing)
                } else {
                    current.bottomAnchor.constraintEqualToAnchor(childrenContainer.layoutMarginsGuide.bottomAnchor)
                }
            )
        }
        constraints.forEach { it.setActive(true) }
        managedConstraints = constraints
    }
}

class HStackViewWrapper(override val view: UIView): UIKitWrapper<UIView> {
    var spacing by Delegates.observable(0.0) { _, _, _ ->
        reapplyConstraints()
    }

    private var managedConstraints = emptyList<NSLayoutConstraint>()

    override fun insert(index: Int, nodeWrapper: UIKitWrapper<*>) {
        super.insert(index, nodeWrapper)

        nodeWrapper.view.translatesAutoresizingMaskIntoConstraints = false
        nodeWrapper.view.setContentHuggingPriority(UILayoutPriorityRequired, UILayoutConstraintAxisHorizontal)
        nodeWrapper.view.setContentHuggingPriority(UILayoutPriorityRequired, UILayoutConstraintAxisVertical)

        reapplyConstraints()
    }

    override fun remove(index: Int, count: Int) {
        super.remove(index, count)

        reapplyConstraints()
    }

    override fun move(from: Int, to: Int, count: Int) {
        super.move(from, to, count)

        reapplyConstraints()
    }

    private fun reapplyConstraints() {
        // We want to store these so they don't change during computation.
        val immutableSpacing = spacing
        val subviews = view.subviews.filterIsInstance<UIView>()

        view.removeConstraints(managedConstraints)

        val constraints = mutableListOf<NSLayoutConstraint>()
        for (index in view.subviews.indices) {
            val current = subviews[index]
            val next = subviews.getOrNull(index + 1)

            constraints.add(
                current.topAnchor.constraintEqualToAnchor(view.layoutMarginsGuide.topAnchor)
            )

            constraints.add(
                current.bottomAnchor.constraintEqualToAnchor(view.layoutMarginsGuide.bottomAnchor)
            )

            if (index == 0) {
                constraints.add(
                    current.leadingAnchor.constraintEqualToAnchor(view.layoutMarginsGuide.leadingAnchor)
                )
            }

            constraints.add(
                if (next != null) {
                    next.leadingAnchor.constraintEqualToAnchor(current.trailingAnchor, immutableSpacing)
                } else {
                    current.trailingAnchor.constraintEqualToAnchor(view.layoutMarginsGuide.trailingAnchor).apply {
                        priority = UILayoutPriorityDefaultHigh
                    }
                }
            )
        }
        constraints.forEach { it.setActive(true) }
        managedConstraints = constraints
    }
}

class ZStackViewWrapper(override val view: UIView): UIKitWrapper<UIView> {
    override fun insert(index: Int, nodeWrapper: UIKitWrapper<*>) {
        super.insert(index, nodeWrapper)

        nodeWrapper.view.apply {
            translatesAutoresizingMaskIntoConstraints = false
            listOf(
                leadingAnchor.constraintEqualToAnchor(view.layoutMarginsGuide.leadingAnchor),
                topAnchor.constraintEqualToAnchor(view.layoutMarginsGuide.topAnchor),
                trailingAnchor.constraintEqualToAnchor(view.layoutMarginsGuide.trailingAnchor),
                bottomAnchor.constraintEqualToAnchor(view.layoutMarginsGuide.bottomAnchor),
            ).forEach {
                it.active = true
            }
        }
    }
}

@Composable
fun <ITEM> TableView(items: List<ITEM>, content: @Composable (ITEM) -> Unit) {
    val compositionContext = rememberCompositionContext()
    ComposeNode<UITableViewWrapper<ITEM>, UIKitApplier>(
        factory = {
            UITableViewWrapper(
                UITableView(CGRectZero.readValue(), UITableViewStyle.UITableViewStylePlain),
                content,
                compositionContext,
            )
        },
        update = {
            set(items) { items -> this.items = items }
            set(content) { content -> this.content = content }
        }
    )
}

interface LazyColumnScope {

}

internal object LazyColumnScopeInstance: LazyColumnScope

interface LazyColumnItemScope {
}

internal object LazyColumnItemScopeInstance: LazyColumnItemScope

class UITableViewWrapper<ITEM>(
    override val view: UITableView,
    initialContent: @Composable (ITEM) -> Unit,
    private val compositionContext: CompositionContext,
): UIKitWrapper<UITableView> {

    var items: List<ITEM> by Delegates.observable(emptyList()) { _, _, _ ->
        view.reloadData()
    }

    var content: @Composable (ITEM) -> Unit = initialContent
        set(newContent) {
            field = newContent
            view.reloadData()
        }

    private val dataSource = DataSource()
    private val delegate = Delegate()

    init {
        view.dataSource = dataSource
        view.delegate = delegate

        view.registerClass(cellClass = Cell.`class`(), forCellReuseIdentifier = "Cell")
    }

    inner class DataSource: NSObject(), UITableViewDataSourceProtocol {
        override fun tableView(tableView: UITableView, numberOfRowsInSection: NSInteger): NSInteger {
            return items.count().toLong()
        }

        override fun tableView(tableView: UITableView, cellForRowAtIndexPath: NSIndexPath): UITableViewCell {
            val cell = tableView.dequeueReusableCellWithIdentifier("Cell", cellForRowAtIndexPath) as Cell
            val item = items[cellForRowAtIndexPath.row.toInt()]
            cell.setContent(compositionContext) {
                content(item)
            }
            return cell
        }
    }

    inner class Delegate: NSObject(), UITableViewDelegateProtocol {

    }

    class Cell: UITableViewCell {
        @OverrideInit constructor(coder: NSCoder) : super(coder)

        @OverrideInit constructor(style: UITableViewCellStyle, reuseIdentifier: String?): super(style, reuseIdentifier)

        companion object: UITableViewCellMeta()

        private var composition: Composition? = null

        fun setContent(parent: CompositionContext, content: @Composable () -> Unit) {
            val composition = composition?.takeIf { !it.isDisposed } ?: Composition(
                applier = UIKitApplier(RootUIKitWrapper(contentView)),
                parent = parent,
            ).also { composition = it }

            composition.setContent(content)
        }
    }

    override fun insert(index: Int, nodeWrapper: UIKitWrapper<*>) {
        error("Doesn't support children.")
    }

    override fun remove(index: Int, count: Int) {
        error("Doesn't support children.")
    }

    override fun move(from: Int, to: Int, count: Int) {
        error("Doesn't support children.")
    }
}

@Composable
fun VStack(spacing: Double = 0.0, modifier: Modifier = Modifier, scrollDirection: ScrollDirection?, content: @Composable () -> Unit) {
    ComposeNode<VStackViewWrapper, UIKitApplier>(
        factory = {
            VStackViewWrapper(TestView())
        },
        update = {
            set(spacing) { value -> this.spacing = value }
            set(scrollDirection) { direction -> this.scrollDirection = direction }
            set(modifier) { v ->
                v.castOrCreate().modHandlers.forEach { block -> block.invoke(view) }
            }
        },
        content = content,
    )
}

@Composable
fun HStack(spacing: Double = 0.0, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    ComposeNode<HStackViewWrapper, UIKitApplier>(
        factory = {
            HStackViewWrapper(UIView())
        },
        update = {
            set(spacing) { value -> this.spacing = value }
            set(modifier) { v ->
                v.castOrCreate().modHandlers.forEach { block -> block.invoke(view) }
            }
        },
        content = content,
    )
}

@Composable
fun ZStack(modifier: Modifier, content: @Composable () -> Unit) {
    ComposeNode<ZStackViewWrapper, UIKitApplier>(
        factory = {
            ZStackViewWrapper(UIView())
        },
        update = {
            set(modifier) { v ->
                v.castOrCreate().modHandlers.forEach { block -> block.invoke(view) }
            }
        },
        content = content,
    )
}

/**
 * Use this method to mount the composition at the certain [root]
 *
 * @param root - the [Element] that will be the root of the DOM tree managed by Compose
 * @param content - the Composable lambda that defines the composition content
 *
 * @return the instance of the [Composition]
 */
fun <TView : UIView> renderComposable(
    root: TView,
    content: @Composable UIViewScope<TView>.() -> Unit
): Composition {
    GlobalSnapshotManager.ensureStarted()

    val context = DefaultMonotonicFrameClock + Dispatchers.Main // JsMicrotasksDispatcher()
    val recomposer = Recomposer(context)
    val composition = Composition(
        applier = UIKitApplier(RootUIKitWrapper(root)),
        parent = recomposer
    )
    val scope = object : UIViewScope<TView> {}
    composition.setContent @Composable {
        content(scope)
    }

    CoroutineScope(context).launch(start = CoroutineStart.UNDISPATCHED) {
        recomposer.runRecomposeAndApplyChanges()
    }
    return composition
}

class TestView: UIView(CGRectZero.readValue()) {
    @ObjCAction
    fun didMoveToWindow() {
        println("didMoveToWindow")
    }

    @ObjCAction
    fun willMoveToWindow(newWindow: UIWindow?) {
        println("willMove: $newWindow")
    }
}

///**
// * Use this method to mount the composition at the element with id - [rootElementId].
// *
// * @param rootElementId - the id of the [Element] that will be the root of the DOM tree managed
// * by Compose
// * @param content - the Composable lambda that defines the composition content
// *
// * @return the instance of the [Composition]
// */
//@Suppress("UNCHECKED_CAST")
//fun renderComposable(
//    rootElementId: String,
//    content: @Composable DOMScope<Element>.() -> Unit
//): Composition = renderComposable(
//    root = document.getElementById(rootElementId)!!,
//    content = content
//)
//
///**
// * Use this method to mount the composition at the [HTMLBodyElement] of the current document
// *
// * @param content - the Composable lambda that defines the composition content
// *
// * @return the instance of the [Composition]
// */
//fun renderComposableInBody(
//    content: @Composable DOMScope<HTMLBodyElement>.() -> Unit
//): Composition = renderComposable(
//    root = document.getElementsByTagName("body")[0] as HTMLBodyElement,
//    content = content
//)
