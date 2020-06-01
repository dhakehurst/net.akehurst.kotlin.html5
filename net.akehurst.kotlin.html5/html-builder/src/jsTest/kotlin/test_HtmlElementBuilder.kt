package net.akehurst.kotlin.html5

import kotlin.browser.document
import kotlin.test.Test


class test_HtmlElementBuilder {

   // @Test
    fun t() {

        document.body {
            main {
                div {
                    attribute.id = "svgContainer"
                    svg {

                    }
                }
            }
        }

    }
}