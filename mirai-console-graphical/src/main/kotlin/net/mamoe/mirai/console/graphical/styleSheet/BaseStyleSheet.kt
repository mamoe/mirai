package net.mamoe.mirai.console.graphical.styleSheet

import javafx.scene.layout.BackgroundPosition
import javafx.scene.layout.BackgroundRepeat
import javafx.scene.layout.BackgroundSize
import javafx.scene.paint.Color
import net.mamoe.mirai.console.MiraiConsole
import tornadofx.*
import java.io.File

@OptIn(ExperimentalStdlibApi::class)
open class BaseStyleSheet : Stylesheet() {

    companion object {
        const val primaryColor = "0EA987"
        const val stressColor = "35867C"
        const val secondaryColor = "32CABA"
        const val lightColor ="9FD1CC"
        const val fontColor = "FFFFFF"
        val TRANSPARENT: Color = Color.TRANSPARENT

        val rootPane by cssclass("root-pane")
        val jfxTabPane by cssclass("jfx-tab-pane")
        val myButtonBar by cssclass("my-button-bar")

        val vbox by csselement("VBox")
    }

    init {

        rootPane {

            child(imageView) {}


            jfxTabPane {
                val bg = File(MiraiConsole.path, "background")
                if (bg.exists() && bg.isDirectory) {
                    bg.listFiles()!!.randomOrNull()?.also {
                        backgroundImage += it.toURI()
                        backgroundRepeat += BackgroundRepeat.REPEAT to BackgroundRepeat.REPEAT
                    }
                }
            }
        }

        listView {
            backgroundColor += TRANSPARENT

            listCell {
                backgroundColor += TRANSPARENT
            }
        }
    }
}