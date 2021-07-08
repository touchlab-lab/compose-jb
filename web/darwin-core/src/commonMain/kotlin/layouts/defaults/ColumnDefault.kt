package layout.defaults // org.jetbrains.compose.common.foundation.layout

import org.jetbrains.compose.common.ui.Modifier
import androidx.compose.runtime.Composable
import org.jetbrains.compose.common.foundation.layout.ColumnActual
import org.jetbrains.compose.common.foundation.layout.ScrollDirection

@Composable
fun Column(
    modifier: Modifier = Modifier.Companion,
    scrollDirection: ScrollDirection? = null,
    content: @Composable () -> Unit
) { ColumnActual(modifier, scrollDirection, content) }
