package net.akehurst.kotlin.html5

import kotlinx.browser.document


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