import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
// import layout.defaults.Column
import org.jetbrains.compose.common.foundation.layout.Box
// import org.jetbrains.compose.common.foundation.layout.Column
import org.jetbrains.compose.common.foundation.layout.LazyColumn
//import org.jetbrains.compose.common.foundation.layout.Row
import org.jetbrains.compose.common.foundation.layout.ScrollDirection
import org.jetbrains.compose.common.material.Text
//import org.jetbrains.compose.common.ui.Modifier
import org.jetbrains.compose.common.ui.padding
import org.jetbrains.compose.common.ui.unit.Dp
import org.jetbrains.compose.common.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import org.jetbrains.compose.common.core.graphics.Color
import org.jetbrains.compose.common.ui.background

object SomeCode {
    @Composable
    internal fun HelloWorld() {
//        TooLong()
//         TooShort()
//        SomeConstraints()
        Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
//                modifier = Modifier.Companion.rotate(20f),
        ) {
            Text("Yo")
            repeat(10) { row ->
                 Row {
                    Text("$row: ")
                     repeat(50) { column ->
                         Text("$column ")
                     }
                 }
            }
        }


        // LazyColumn(items = (0..200).map { "Row: $it" }) {
        //     Box(Modifier.padding(30.dp)) {
        //         Text(it)
        //     }
        // }
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
//                    modifier = Modifier.padding(Dp(10F)),
                    text = "Common Row $it"
                )
            }
        }
    }
}