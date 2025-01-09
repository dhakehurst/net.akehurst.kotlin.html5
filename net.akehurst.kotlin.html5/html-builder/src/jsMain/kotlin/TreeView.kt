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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.asList
import org.w3c.dom.events.Event
import kotlin.coroutines.coroutineContext

class TreeViewFunctions<T>(
    val label: (root: Any?, node: T) -> String,
    val hasChildren: (node: T) -> Boolean,
    val children: suspend (node: T) -> Array<T>,
    val onClick: suspend (node:T) -> Unit = {}
)

class TreeView<T : Any>(
    val element: Element
) {

    companion object {
        fun initialise(document: Document): Map<String, TreeView<*>> {
            val map = mutableMapOf<String, TreeView<*>>()
            document.querySelectorAll("treeview").asList().forEach { el ->
                val treeview = el as Element
                val id = treeview.getAttribute("id") as String
                val tv = TreeView<Any>(treeview)
                map[id] = tv
            }
            return map
        }
    }

    val document: Document get() = element.ownerDocument!!

    private var _loadingElement: Element = this.document.createElement("div")
    private var _loading = false
    var loading
        get() = this._loading
        set(value) {
            this._loading = value
            this.showLoading(value)
        }

    var treeFunctions: TreeViewFunctions<T>? = null
    private var _roots = emptyList<T>()

    init {
        this._loadingElement.setAttribute("class", "treeview-loading")
    }

    suspend fun setRoots(roots: List<T>) {
        this.clear()
        _roots = roots
        when {
            roots.isEmpty() -> Unit //addNode(this.element, null, "<Empty>")
            else -> roots.forEach {
                this.addNode(this.element, it, it)
            }
        }
    }

    fun clear() {
        _roots = emptyList<T>()
        while (null != this.element.firstChild) {
            this.element.removeChild(this.element.firstChild!!)
        }
    }

    suspend fun refresh() {
        //TODO: refresh rather than recreate !
        while (null != this.element.firstChild) {
            this.element.removeChild(this.element.firstChild!!)
        }
        _roots.forEach {
            this.addNode(this.element, it, it)
        }
    }

    suspend fun addNode(parentElement: Element, root: T?, node: T) {
        if (this.treeFunctions!!.hasChildren(node)) {
            val branchEl = document.createElement("treeview-branch")
            parentElement.append(branchEl)
            this.setLabel(branchEl, this.treeFunctions!!.label(root, node))
            val childrenEl = document.createElement("treeview-children")
            branchEl.append(childrenEl)
            branchEl.addEventListener("click", {
                it.stopPropagation()
                if (null == branchEl.getAttribute("open")) {
                    branchEl.setAttribute("open", "true")
                } else {
                    branchEl.removeAttribute("open")
                }
                if (null == childrenEl.firstChild) {
                    GlobalScope.launch {
                        val children = treeFunctions!!.children(node)
                        children.forEach {
                            addNode(childrenEl, root, it)
                        }
                    }
                }
            })
        } else {
            val leafEl = document.createElement("treeview-leaf")
            parentElement.append(leafEl)
            this.setLabel(leafEl, this.treeFunctions!!.label(root, node))
            leafEl.addEventListener("click", {
                GlobalScope.launch {
                    treeFunctions!!.onClick.invoke(node)
                }
                it.stopPropagation()
            })
        }
    }

    fun setLabel(nodeElement: Element, label: String) {
        val span = document.createElement("span");
        span.append(label);
        nodeElement.appendChild(span)
    }

    private fun showLoading(visible: Boolean) {
        while (null != this.element.firstChild) {
            this.element.removeChild(this.element.firstChild!!)
        }
        if (visible) {
            this.element.appendChild(this._loadingElement)
        }
    }
}

