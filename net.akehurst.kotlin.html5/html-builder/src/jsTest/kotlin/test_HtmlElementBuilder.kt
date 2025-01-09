package net.akehurst.kotlin.html5

import kotlinx.browser.document
import kotlin.test.Test


class test_HtmlElementBuilder {

    @Test
    fun html() {

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

    @Test
    fun table() {
        document.body {
            table {
                caption("")
                thead {
                    row {
                        header_cell { content=""; attribute.style="" }
                        header_cell { content=""; attribute.style="" }
                        header_cell { content=""; attribute.style="" }
                    }
                }
                tbody {
                    row {
                        data_cell { p {  } }
                        data_cell { span {  } }
                        data_cell { span {  } }
                    }
                }
                tfoot {
                    row {
                        header_cell { content = ""; attribute.style = "" }
                        header_cell { content = ""; attribute.style = "" }
                        header_cell { content = ""; attribute.style = "" }
                    }
                }
            }
        }

    }
}