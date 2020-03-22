package net.mamoe.mirai.console.graphical.styleSheet

import javafx.scene.Cursor
import javafx.scene.paint.Color
import tornadofx.*

class PrimaryStyleSheet : BaseStyleSheet() {
    companion object {
        // window
        val jfxTitle by cssclass("jfx-decorator-buttons-container")
        val container by cssclass("jfx-decorator-content-container")

        // tab
        val jfxTabPane by cssclass("tab-header-background")
        val closeButton by cssclass("tab-close-button")
    }

    init {
        /*
         * window
         */
        jfxTitle {
            backgroundColor += c(primaryColor)
        }

        container {
            borderColor += box(c(primaryColor))
            borderWidth += box(0.px, 4.px, 4.px, 4.px)
        }


        /*
         * tab pane
         */
        jfxTabPane {
            backgroundColor += c(primaryColor)
        }

        // 去除JFoenix默认样式
        tab {
            and(":closable") {
                borderWidth += box(0.px)
                borderInsets += box(6.px, 0.px)
            }

            closeButton {
                and(hover) { cursor = Cursor.HAND }
            }
        }
    }
}