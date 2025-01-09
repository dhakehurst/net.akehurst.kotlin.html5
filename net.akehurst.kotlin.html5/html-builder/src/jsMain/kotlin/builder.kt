/**
 * Copyright (C) 2024 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.akehurst.kotlin.html5

import kotlinx.browser.document
import kotlinx.dom.appendElement
import net.akehurst.kotlin.html5.widgets.TabView
import net.akehurst.kotlin.html5.widgets.TreeView
import net.akehurst.kotlin.html5.widgets.TreeViewFunctions
import org.w3c.dom.*
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.events.Event

@DslMarker
annotation class HtmlDslMarker

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

fun Element.elUpdate(modifications: HtmlElementBuilder.() -> Unit = {}) {
    val b = HtmlElementBuilder(this)
    b.modifications()
}

fun Element.elCreate() = HtmlElementBuilder(this)

fun SVGElement.svgCreate() = SvgElementBuilder(this)
fun SVGElement.svgUpdate(modifications: SvgElementBuilder.() -> Unit = {}) {
    val b = SvgElementBuilder(this)
    b.modifications()
}

@HtmlDslMarker
class HtmlElementBuilder(val element: Element) {

    val attribute = AttributeBuilder(element)
    val on = EventHandlerBuilder(element)
    val class_ = ClassBuilder(element)
    val style = StyleBuilder(element)

    var content: String?
        get() = this.element.textContent
        set(value) {
            this.element.textContent = value
        }

    fun htmlElement(tagName: String, init: HtmlElementBuilder.() -> Unit = {}): HTMLElement {
        val child = element.ownerDocument!!.createElement(tagName)
        element.appendChild(child)
        HtmlElementBuilder(child).init()
        return child as HTMLElement
    }

    private fun customElement(tagName: String, init: HtmlElementBuilder.() -> Unit = {}, customBuild: HtmlElementBuilder.() -> Unit = {}): Element {
        val child = element.ownerDocument!!.createElement(tagName)
        element.appendChild(child)
        val b = HtmlElementBuilder(child)
        b.customBuild()
        b.init()
        return child
    }

    fun a(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("a", init) as HTMLAnchorElement
    fun address(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("address", init)
    fun article(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("article", init)
    fun aside(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("aside", init)
    fun button(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("button", init) as HTMLButtonElement
    fun canvas(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("canvas", init)
    fun checkbox(init: HtmlElementBuilder.() -> Unit = {}) = customElement("input", init) {
        attribute.type = "checkbox"
    }

    fun code(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("code", init)
    fun dialog(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("dialog", init)
    fun div(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("div", init) as HTMLDivElement
    fun footer(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("footer", init)
    fun header(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("header", init)
    fun h1(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("h1", init)
    fun h2(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("h2", init)
    fun h3(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("h3", init)
    fun icon(class_: String) = htmlElement("i", { attribute.class_ = class_ })
    fun img(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("img", init)
    fun input(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("input", init)
    fun label(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("label", init)
    fun li(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("li", init)
    fun ol(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("ol", init)
    fun optgroup(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("optgroup", init)
    fun option(disabled: Boolean = false, label: String = "", selected: Boolean = false, value: String = "", init: HtmlElementBuilder.() -> Unit = {}): Element {
        return htmlElement("option", init).also {
            if (disabled) it.setAttribute("disabled", "true")
            if (label.isNotBlank()) it.setAttribute("label", label)
            if (selected) it.setAttribute("selected", "true")
            if (value.isNotBlank()) it.setAttribute("value", value)
        }
    }

    fun p(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("p", init) as HTMLParagraphElement
    fun progress(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("progress", init)
    fun main(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("main", init)
    fun nav(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("nav", init)
    fun radio(init: HtmlElementBuilder.() -> Unit = {}) = customElement("input", init) {
        attribute.type = "radio"
    }

    fun section(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("section", init)
    fun select(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("select", init)
    fun span(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("span", init)
    fun table(init: TableElementBuilder.() -> Unit = {}): HTMLTableElement {
        val table = element.appendElement("table") {} as HTMLTableElement
        TableElementBuilder(table).init()
        return table
    }

    fun tabview(init: TabViewBuilder.() -> Unit = {}): TabView {
        val tv = element.appendElement("tabview") {} as HTMLElement
        TabViewBuilder(tv).init()
        return TabView(tv)
    }

    fun <T : Any> treeview(id: String, treeFunctions: TreeViewFunctions<T>): TreeView<T> {
        val element = htmlElement("treeview", { attribute.id = id })
        val tv = TreeView<T>(element)
        tv.treeFunctions = treeFunctions
        return tv
    }

    fun textarea(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("textarea", init)
    fun ul(init: HtmlElementBuilder.() -> Unit = {}) = htmlElement("ul", init)

    fun svg(init: SvgElementBuilder.() -> Unit = {}): SVGElement {
        val child = element.ownerDocument!!.createElementNS("http://www.w3.org/2000/svg", "svg") as SVGElement
        element.appendChild(child)
        SvgElementBuilder(child).init()
        return child
    }
}

@HtmlDslMarker
class TableElementBuilder(
    val table: HTMLTableElement
) {
    fun caption(init: HtmlElementBuilder.() -> Unit = {}) {
        val cap = table.createCaption()
        HtmlElementBuilder(cap).init()
    }

    fun thead(init: TableSectionElementBuilder.() -> Unit) {
        val thead = table.createTHead()
        TableSectionElementBuilder(thead).init()
    }

    fun tbody(init: TableSectionElementBuilder.() -> Unit) {
        val thead = table.createTBody()
        TableSectionElementBuilder(thead).init()
    }

    fun tfoot(init: TableSectionElementBuilder.() -> Unit) {
        val thead = table.createTFoot()
        TableSectionElementBuilder(thead).init()
    }
}

@HtmlDslMarker
class TableSectionElementBuilder(
    val tsection: HTMLTableSectionElement
) {
    fun row(init: TableRowElementBuilder.() -> Unit): HTMLTableRowElement {
        return tsection.appendElement("tr") {
            TableRowElementBuilder(this as HTMLTableRowElement).init()
        } as HTMLTableRowElement
    }
}

@HtmlDslMarker
class TableRowElementBuilder(
    val trow: HTMLTableRowElement
) {
    fun header_cell(init: HtmlElementBuilder.() -> Unit = {}): HTMLTableCellElement {
        return trow.appendElement("th") {
            HtmlElementBuilder(this as HTMLTableCellElement).init()
        } as HTMLTableCellElement
    }

    fun data_cell(init: HtmlElementBuilder.() -> Unit = {}): HTMLTableCellElement {
        return trow.appendElement("td") {
            HtmlElementBuilder(this as HTMLTableCellElement).init()
        } as HTMLTableCellElement
    }
}

@HtmlDslMarker
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

    var checked get() = get("checked"); set(value) = set("checked", value)
    var class_ get() = get("class"); set(value) = set("class", value)
    var contenteditable get() = get("contenteditable"); set(value) = set("contenteditable", value)
    var height get() = get("height"); set(value) = set("height", value)
    var id get() = get("id"); set(value) = set("id", value)
    var for_ get() = get("for"); set(value) = set("for", value)
    var name get() = get("name"); set(value) = set("name", value)
    var style get() = get("style"); set(value) = set("style", value)
    var type get() = get("type"); set(value) = set("type", value)
    var value get() = get("value"); set(value) = set("value", value)
    var width get() = get("width"); set(value) = set("width", value)
}

@HtmlDslMarker
class EventHandlerBuilder(
    val element: Element
) {
    fun event(eventName: String, handler: (Event) -> Unit) {
        this.element.addEventListener(eventName, handler)
    }

    fun blur(handler: (Event) -> Unit) = event("blur", handler)
    fun change(handler: (Event) -> Unit) = event("change", handler)
    fun click(handler: (Event) -> Unit) = event("click", handler)
    fun dblclick(handler: (Event) -> Unit) = event("dblclick", handler)
    fun focus(handler: (Event) -> Unit) = event("focus", handler)
    fun select(handler: (Event) -> Unit) = event("select", handler)

}

@HtmlDslMarker
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

@HtmlDslMarker
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

@HtmlDslMarker
class SvgElementBuilder(
    val parent: SVGElement
) {
    val attribute = SVGAttributeBuilder(parent)
    val class_ = ClassBuilder(parent)
    val style = StyleBuilder(parent)

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
    fun line(init: SvgElementBuilder.() -> Unit = {}) = svgElement("line", init)
}

@HtmlDslMarker
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
    var marker_end get() = get("marker-end"); set(value) = set("marker-end", value)
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
    var x1 get() = get("x1"); set(value) = set("x1", value)
    var x2 get() = get("x2"); set(value) = set("x2", value)
    var y get() = get("y"); set(value) = set("y", value)
    var y1 get() = get("y1"); set(value) = set("y1", value)
    var y2 get() = get("y2"); set(value) = set("y2", value)
}

@HtmlDslMarker
class TabViewBuilder(
    val tabView: HTMLElement
) {
    fun tab(id: String, init: HtmlElementBuilder.() -> Unit) {
        tabView.appendElement("tab") {
            this.id = id
            HtmlElementBuilder(this).init()
        }
    }
}