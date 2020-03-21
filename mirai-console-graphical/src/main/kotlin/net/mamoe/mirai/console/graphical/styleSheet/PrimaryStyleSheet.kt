package net.mamoe.mirai.console.graphical.styleSheet

import tornadofx.box
import tornadofx.c
import tornadofx.cssclass
import tornadofx.px

class PrimaryStyleSheet : BaseStyleSheet() {
    companion object {
        // window
        val jfxTitle by cssclass("jfx-decorator-buttons-container")
        val container by cssclass("jfx-decorator-content-container")

        // tab
        val tabPane by cssclass("tab-header-background")
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
        tabPane {
            backgroundColor += c(primaryColor)
        }
    }
}