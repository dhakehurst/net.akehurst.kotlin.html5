package net.akehurst.kotlin.html5

import org.w3c.dom.Element
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.Document

fun Document.head(init: HtmlElementBuilder.() -> Unit = {}): Element {
    val child = this.createElement("head")
    this.appendChild(child)
    HtmlElementBuilder(child).init()
    return child
}

fun Document.body(init: HtmlElementBuilder.() -> Unit = {}): Element {
    val child = this.createElement("body")
    this.appendChild(child)
    HtmlElementBuilder(child).init()
    return child
}

fun Element.create() = HtmlElementBuilder(this)
fun SVGElement.create() = SvgElementBuilder(this)

class HtmlElementBuilder(val parent: Element) {

    val attribute = AttributeBuilder(parent)
    val class_ = ClassBuilder(parent)
    val style = StyleBuilder(parent)

    fun htmlElement(tagName: String, init: HtmlElementBuilder.() -> Unit = {}): Element {
        val child = parent.ownerDocument!!.createElement(tagName)
        parent.appendChild(child)
        HtmlElementBuilder(child).init()
        return child
    }

    fun main(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("main", init)
    fun div(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("div", init)

    fun svg(init: SvgElementBuilder.() -> Unit = {}): SVGElement {
        val child = parent.ownerDocument!!.createElementNS("http://www.w3.org/2000/svg", "svg") as SVGElement
        parent.appendChild(child)
        SvgElementBuilder(child).init()
        return child
    }
}

class AttributeBuilder(
        val element: Element
) {
    fun get(attributeName: String): String? {
        return this.element.getAttribute(attributeName)
    }

    fun set(attributeName: String, value: String?) {
        if (value == null) {
            this.element.removeAttribute(attributeName)
        } else {
            this.element.setAttribute(attributeName, value)
        }
    }

    var id
        get() = get("id")
        set(value) = set("id", value)

    var style
        get() = get("style")
        set(value) = set("style", value)

    var class_
        get() = get("class")
        set(value) = set("class", value)
}

class ClassBuilder(
        val element: Element
) {
    fun get(): List<String>? {
        return this.element.getAttribute("class")?.split(" ")
    }

    fun add(value: String) {
        val list = get()
        when {
            list == null -> this.element.setAttribute("class", value)
            list.contains(value) -> { /* do nothing */
            }
            else -> {
                val newValue = (list + value).joinToString(" ")
                this.element.setAttribute("class", newValue)
            }
        }
    }

    fun remove(value: String) {
        val list = get()
        when {
            list == null -> { /* do nothing */
            }
            list.contains(value).not() -> { /* do nothing */
            }
            else -> {
                val newValue = (list - value).joinToString(" ")
                this.element.setAttribute("class", newValue)
            }
        }
    }

}

class StyleBuilder(
        val element: Element
) {

    fun get(): Map<String, String>? {
        val styleString = this.element.getAttribute("style")
        val list = styleString?.split(";")
        val map = list?.associate {
            val parts = it.trim().split("=")
            val f = parts[0].trim()
            val s = parts[0].trim()
            Pair(f, s)
        }
        return map
    }

    fun set(value:Map<String, String>?) {
        if (value == null) {
            this.element.removeAttribute("style")
        } else {
            val stringValue = value.entries.joinToString(";") {
                "${it.key}=${it.value}"
            }
            this.element.setAttribute("style", stringValue)
        }
    }

    fun get(cssPropertyName: String): String? {
        return get()?.get(cssPropertyName)
    }

    fun set(cssPropertyName: String, value:String?) {
        val newMap = get()?.toMutableMap() ?: mutableMapOf()
        if (null==value) {
            newMap.remove(cssPropertyName)
        } else {
            newMap[cssPropertyName] = value
        }
        this.set(newMap)
    }

    var display
        get() = get("display")
        set(value) = set("display", value)

    var color
        get() = get("color")
        set(value) = set("color", value)

    var background_color
        get() = get("background-color")
        set(value) = set("background-color", value)
}

class SvgElementBuilder(
        val parent: SVGElement
) {
}