package net.mamoe.mirai.console.graphical.styleSheet

import tornadofx.*

class PrimaryStyleSheet : Stylesheet() {
    companion object {
        val jfxTitle by cssclass("jfx-decorator-buttons-container")
        val container by cssclass("jfx-decorator-content-container")
    }

    init {
        jfxTitle {
            backgroundColor += c("00BCD4")
        }

        container {
            borderColor += box(c("00BCD4"))
            borderWidth += box(0.px, 4.px, 4.px, 4.px)
        }
    }
}