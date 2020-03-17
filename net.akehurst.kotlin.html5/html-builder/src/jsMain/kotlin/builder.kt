package net.akehurst.kotlin.html5

import org.w3c.dom.Element
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.Document
import kotlin.dom.appendText

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

fun Element.update(modifications: HtmlElementBuilder.() -> Unit = {}) {
    val b = HtmlElementBuilder(this)
    b.modifications()
}
fun Element.create() = HtmlElementBuilder(this)
fun SVGElement.create() = SvgElementBuilder(this)
fun SVGElement.svgUpdate(modifications: SvgElementBuilder.() -> Unit = {}) {
    val b = SvgElementBuilder(this)
    b.modifications()
}
class HtmlElementBuilder(val parent: Element) {

    val attribute = AttributeBuilder(parent)
    val class_ = ClassBuilder(parent)
    val style = StyleBuilder(parent)

    var content :String?
    get() = this.parent.textContent
    set(value) {
        this.parent.textContent = value
    }

    fun htmlElement(tagName: String, init: HtmlElementBuilder.() -> Unit = {}): Element {
        val child = parent.ownerDocument!!.createElement(tagName)
        parent.appendChild(child)
        HtmlElementBuilder(child).init()
        return child
    }

    fun main(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("main", init)
    fun div(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("div", init)
    fun span(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("span", init)

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

    var class_ get() = get("class"); set(value) = set("class", value)
    var contenteditable get() = get("contenteditable"); set(value) = set("contenteditable", value)
    var height get() = get("height"); set(value) = set("height", value)
    var id get() = get("id"); set(value) = set("id", value)
    var style get() = get("style"); set(value) = set("style", value)
    var width get() = get("width"); set(value) = set("width", value)
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
            val parts = it.trim().split(":")
            val f = parts[0].trim()
            val s = parts[1].trim()
            Pair(f, s)
        }
        return map
    }

    fun set(value: Map<String, String>?) {
        if (value == null) {
            this.element.removeAttribute("style")
        } else {
            val stringValue = value.entries.joinToString(";") {
                "${it.key}:${it.value}"
            }
            this.element.setAttribute("style", stringValue)
        }
    }

    fun get(cssPropertyName: String): String? {
        return get()?.get(cssPropertyName)
    }

    fun set(cssPropertyName: String, value: String?) {
        val newMap = get()?.toMutableMap() ?: mutableMapOf()
        if (null == value) {
            newMap.remove(cssPropertyName)
        } else {
            newMap[cssPropertyName] = value
        }
        this.set(newMap)
    }

    var display get() = get("display"); set(value) = set("display", value)
    var color get() = get("color"); set(value) = set("color", value)
    var background_color get() = get("background-color"); set(value) = set("background-color", value)
    var position get() = get("position"); set(value) = set("position", value)
    var width get() = get("width"); set(value) = set("width", value)
    var height get() = get("height"); set(value) = set("height", value)
    var top get() = get("top"); set(value) = set("top", value)
    var left get() = get("left"); set(value) = set("left", value)
}

class SvgElementBuilder(
        val parent: SVGElement
) {
    val attribute = SVGAttributeBuilder(parent)

    fun svgElement(tagName: String, init: SvgElementBuilder.() -> Unit = {}): SVGElement {
        val child = parent.ownerDocument!!.createElementNS("http://www.w3.org/2000/svg", tagName) as SVGElement
        parent.appendChild(child)
        SvgElementBuilder(child).init()
        return child
    }

    fun defs(init: SvgElementBuilder.() -> Unit = {}) = svgElement("defs", init)
    fun marker(init: SvgElementBuilder.() -> Unit = {}) = svgElement("marker", init)
    fun path(init: SvgElementBuilder.() -> Unit = {}) = svgElement("path", init)
    fun g(init: SvgElementBuilder.() -> Unit = {}) = svgElement("g", init)
    fun circle(init: SvgElementBuilder.() -> Unit = {}) = svgElement("circle", init)
    fun rect(init: SvgElementBuilder.() -> Unit = {}) = svgElement("rect", init)
}

class SVGAttributeBuilder(
        val element: SVGElement
) {

    fun get(attributeName: String): String? {
        return this.element.getAttributeNS(null, attributeName)
    }

    fun set(attributeName: String, value: String?) {
        if (value == null) {
            this.element.removeAttributeNS(null, attributeName)
        } else {
            this.element.setAttributeNS(null, attributeName, value)
        }
    }

    var cx get() = get("cx"); set(value) = set("cx", value)
    var cy get() = get("cy"); set(value) = set("cy", value)
    var d get() = get("d"); set(value) = set("d", value)
    var fill get() = get("fill"); set(value) = set("fill", value)
    var id get() = get("id"); set(value) = set("id", value)
    var height get() = get("height"); set(value) = set("height", value)
    var markerHeight get() = get("markerHeight"); set(value) = set("markerHeight", value)
    var markerUnits get() = get("markerUnits"); set(value) = set("markerUnits", value)
    var markerWidth get() = get("markerWidth"); set(value) = set("markerWidth", value)
    var orient get() = get("orient"); set(value) = set("orient", value)
    var r get() = get("r"); set(value) = set("r", value)
    var refX get() = get("refX"); set(value) = set("refX", value)
    var refY get() = get("refY"); set(value) = set("refY", value)
    var rx get() = get("rx"); set(value) = set("rx", value)
    var ry get() = get("ry"); set(value) = set("ry", value)
    var stroke get() = get("stroke"); set(value) = set("stroke", value)
    var stroke_width get() = get("stroke-width"); set(value) = set("stroke-width", value)
    var viewBox get() = get("viewBox"); set(value) = set("viewBox", value)
    var width get() = get("width"); set(value) = set("width", value)
    var x get() = get("x"); set(value) = set("x", value)
    var y get() = get("y"); set(value) = set("y", value)
}