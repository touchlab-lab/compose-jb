import androidx.compose.runtime.Composable
import org.jetbrains.compose.common.foundation.layout.Column
import org.jetbrains.compose.common.foundation.layout.LazyColumn
import org.jetbrains.compose.common.foundation.layout.Row
import org.jetbrains.compose.common.material.Text
import org.jetbrains.compose.common.ui.Modifier
import org.jetbrains.compose.common.ui.padding
import org.jetbrains.compose.common.ui.unit.Dp

object SomeCode {
    @Composable
    internal fun HelloWorld() {
//        TooLong()
//         TooShort()
//        SomeConstraints()
//         Column {
//             repeat(200) { row ->
//                 Row {
//                     Text("$row: ")
//                     repeat(50) { column ->
//                         Text("$column ")
//                     }
//                 }
//             }
//         }


        LazyColumn(items = (0..200).map { "Row: $it" }) {
            Text(it)
        }
    }

    @Composable
    internal fun TooLong() {
        Column {
            repeat(60) {
                Text("Common Row $it")
            }
        }
    }

    @Composable
    internal fun TooShort() {
        Column {
            repeat(10) {
                Text("Common Row $it")
            }
        }
    }

    @Composable
    internal fun SomeConstraints() {
        Column {
            repeat(5) {
                Text(
                    modifier = Modifier.padding(Dp(10F)),
                    text = "Common Row $it"
                )
            }
        }
    }
}