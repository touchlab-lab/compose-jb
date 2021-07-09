import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import org.jetbrains.compose.common.material.Text

object SomeCode {
    @OptIn(ExperimentalUnitApi::class)
    @Composable
    internal fun HelloWorld() {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Text("Yo")
            Column(modifier = Modifier.align(Alignment.Center)) {
                repeat(10) { row ->
                    Row {
                        Text("${row + 5}: ")
                        repeat(5) { column ->
                            Text("${5 + row + column} ", size = TextUnit(5f + row.toFloat() + column.toFloat(), type = TextUnitType.Unspecified))
                        }
                    }
                }
            }
            Text("Okay", modifier = Modifier.align(Alignment.BottomEnd))
//            Column(
////                    modifier = Modifier.align(Alignment.BottomEnd)
//            ) {
//
//            }
        }
//        TooLong()
//         TooShort()
//        SomeConstraints()



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