/**
 * Copyright (C) 2020 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

package net.akehurst.kotlin.html5.widgets

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.events.Event

class TabView(
        val element: Element
) {

    companion object {
        fun initialise(document: Document) {
            document.querySelectorAll("tabview").asList().forEach { tabview ->
                TabView(tabview as Element)
            }
        }
    }

    val document: Document get() = element.ownerDocument!!

    init {
        val nav = document.createElement("tab-nav")
        element.insertBefore(nav, element.firstElementChild)
        var firstHeader:Element? = null
        var firstTab:Element? = null
        element.querySelectorAll(":scope > tab").asList().filterIsInstance<Element>().forEach { tab ->
            val header = document.createElement("tab-header")
            header.id = tab.getAttribute("id")!!
            nav.appendChild(header)
            header.textContent = tab.getAttribute("id")
            header.addEventListener("click", { _ ->
                this.tabSelect(header, tab)
            })
            if (null==firstHeader) {
                firstHeader = header
                firstTab = tab
            }
        }
        if (null!=firstHeader) {
            tabSelect(firstHeader!!, firstTab!!)
        }
    }

    fun activateTab(id:String) {
        val header = this.element.querySelector("tab-header#$id") as HTMLElement?
        val tab = this.element.querySelector("tab#$id") as HTMLElement?
        if (null!=header && null != tab) {
            tabSelect(header, tab)
        }
    }

    private fun tabSelect(header: Element, tab: Element) {
        this.element.querySelectorAll(":scope > tab-nav > tab-header").asList().filterIsInstance<Element>().forEach { e ->
            e.classList.remove("tab-active")
        }
        this.element.querySelectorAll(":scope > tab").asList().filterIsInstance<Element>().forEach { e ->
            e.setAttribute("style", "display : none")
        }
        header.classList.add("tab-active")
        tab.setAttribute("style", "display : grid")
    }
}

