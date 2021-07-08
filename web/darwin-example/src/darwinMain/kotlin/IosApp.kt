import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.BaseUIKitComposable
import androidx.compose.ui.platform.UIViewConvertible
import org.jetbrains.compose.web.renderComposable
import platform.UIKit.UIView

val IosApp: UIViewConvertible = IosAppImpl

private object IosAppImpl: BaseUIKitComposable() {
    @Composable
    override fun Content() {
        SomeCode.HelloWorld()
    }
}