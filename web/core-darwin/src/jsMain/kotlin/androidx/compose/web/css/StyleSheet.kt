package org.jetbrains.compose.web.css

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import org.jetbrains.compose.web.css.selectors.CSSSelector
import org.jetbrains.compose.web.css.selectors.className
import org.jetbrains.compose.web.dom.Style
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class CSSRulesHolderState : CSSRulesHolder {
    override var cssRules: CSSRuleDeclarationList by mutableStateOf(listOf())

    override fun add(cssRule: CSSRuleDeclaration) {
        cssRules += cssRule
    }
}

/**
 * Represents a collection of the css style rules.
 * StyleSheet needs to be mounted.
 * @see [Style]
 *
 * Example:
 * ```
 * object AppStylesheet : StyleSheet() {
 *     val containerClass by style {
 *         padding(24.px)
 *     }
 * }
 * ```
 *
 * Usage Example:
 * ```
 *    Style(AppStylesheet) // this mounts the stylesheet
 *    Div(classes = listOf(AppStylesheet.containerClass),...)
 * ```
 */
open class StyleSheet(
    private val rulesHolder: CSSRulesHolder = CSSRulesHolderState()
) : StyleSheetBuilder, CSSRulesHolder by rulesHolder {
    private val boundClasses = mutableMapOf<String, CSSRuleDeclarationList>()

    protected fun style(cssRule: CSSBuilder.() -> Unit) = CSSHolder(cssRule)

    companion object {
        var counter = 0
    }

    data class CSSSelfSelector(var selector: CSSSelector? = null) : CSSSelector() {
        override fun toString(): String = selector.toString()
        override fun equals(other: Any?): Boolean {
            return other is CSSSelfSelector
        }
    }

    // TODO: just proof of concept, do not use it
    fun css(cssBuild: CSSBuilder.() -> Unit): String {
        val selfSelector = CSSSelfSelector()
        val (style, newCssRules) = buildCSS(selfSelector, selfSelector, cssBuild)
        val cssRule = cssRules.find {
            it is CSSStyleRuleDeclaration &&
                it.selector is CSSSelector.CSSClass && it.style == style &&
                (boundClasses[it.selector.className] ?: emptyList()) == newCssRules
        }.unsafeCast<CSSStyleRuleDeclaration?>()
        return if (cssRule != null) {
            cssRule.selector.unsafeCast<CSSSelector.CSSClass>().className
        } else {
            val classNameSelector = className("auto-${counter++}")
            selfSelector.selector = classNameSelector
            add(classNameSelector, style)
            newCssRules.forEach { add(it) }
            boundClasses[classNameSelector.className] = newCssRules
            classNameSelector.className
        }
    }

    protected class CSSHolder(val cssBuilder: CSSBuilder.() -> Unit) {
        operator fun provideDelegate(
            sheet: StyleSheet,
            property: KProperty<*>
        ): ReadOnlyProperty<Any?, String> {
            val sheetName = "${sheet::class.simpleName}-"
            val selector = className("$sheetName${property.name}")
            val (properties, rules) = buildCSS(selector, selector, cssBuilder)
            sheet.add(selector, properties)
            rules.forEach { sheet.add(it) }

            return ReadOnlyProperty { _, _ ->
                selector.className
            }
        }
    }

    override fun buildRules(rulesBuild: GenericStyleSheetBuilder<CSSStyleRuleBuilder>.() -> Unit) =
        StyleSheet().apply(rulesBuild).cssRules
}

fun buildCSS(
    thisClass: CSSSelector,
    thisContext: CSSSelector,
    cssRule: CSSBuilder.() -> Unit
): Pair<StyleHolder, CSSRuleDeclarationList> {
    val styleSheet = StyleSheetBuilderImpl()
    val builder = CSSBuilderImpl(thisClass, thisContext, styleSheet)
    builder.cssRule()
    return builder to styleSheet.cssRules
}

@Composable
inline fun Style(
    styleSheet: CSSRulesHolder
) {
    Style(cssRules = styleSheet.cssRules)
}
