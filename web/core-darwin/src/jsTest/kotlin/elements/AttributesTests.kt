package org.jetbrains.compose.web.core.tests

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.attributes.forId
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import kotlin.test.Test
import kotlin.test.assertEquals

class AttributesTests {

    @Test
    fun labelForIdAttrAppliedProperly() = runTest {

        composition {
            Label(forId = "l1") { Text("label") }
        }

        assertEquals(
            expected = "<label for=\"l1\">label</label>",
            actual = root.innerHTML
        )
    }

    @Test
    fun labelForIdIsOptional() = runTest {

        composition {
            Label { Text("label") }
        }

        assertEquals(
            expected = "<label>label</label>",
            actual = root.innerHTML
        )
    }

    @Test
    fun labelForIdIsAppliedFromAttrs() = runTest {

        composition {
            Label(
                attrs = {
                    forId("lb1")
                }
            ) {
                Text("label")
            }
        }

        assertEquals(
            expected = "<label for=\"lb1\">label</label>",
            actual = root.innerHTML
        )
    }

    @Test
    fun buttonDisabledAttributeAddedOnlyWhenTrue() = runTest {
        var disabled by mutableStateOf(false)

        composition {
            Button(
                {
                    disabled(disabled)
                }
            ) {}
        }

        val btn = root.firstChild as HTMLButtonElement
        assertEquals(null, btn.getAttribute("disabled"))

        disabled = true
        waitChanges()

        assertEquals("", btn.getAttribute("disabled"))
    }

    @Test
    fun multipleClassesCallsAddMoreClasses() = runTest {
        composition {
            Div({
                classes("a", "b")
                classes("c", "d")
            }) {}
        }

        val div = root.firstChild as HTMLDivElement
        assertEquals(
            expected = "a b c d",
            actual = div.getAttribute("class")
        )
    }

    @Test
    fun multipleClassesCallsWithConditionsAddCorrectClasses() = runTest {
        val addClassD = mutableStateOf(true)
        composition {
            Div({
                classes("c")
                if (addClassD.value) {
                    classes("d")
                }
                classes("a", "b")
            }) {}
        }

        val div = root.firstChild as HTMLDivElement
        assertEquals(
            expected = "c d a b",
            actual = div.getAttribute("class")
        )

        addClassD.value = false
        waitChanges()

        assertEquals(
            expected = "c a b",
            actual = div.getAttribute("class")
        )
    }
}