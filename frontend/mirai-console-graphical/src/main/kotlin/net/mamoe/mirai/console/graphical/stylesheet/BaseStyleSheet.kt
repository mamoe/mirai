/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.graphical.stylesheet

import javafx.scene.layout.BackgroundRepeat
import javafx.scene.paint.Color
import net.mamoe.mirai.console.MiraiConsole
import tornadofx.Stylesheet
import tornadofx.cssclass
import tornadofx.csselement
import java.io.File
import kotlin.random.Random

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

        val vBox by csselement("VBox")
    }

    init {

        rootPane {

            child(imageView) {}


            jfxTabPane {
                val bg = File(MiraiConsole.path, "background")
                if (!bg.exists()) bg.mkdir()
                if (bg.isDirectory) {
                    bg.listFiles()!!.filter { file -> file.extension in listOf("jpg", "jpeg", "png", "gif") }
                        .randomElement()?.also {
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

fun <T> Collection<T>.randomElement(): T? {
    if (isEmpty())
        return null
    return elementAt(Random.nextInt(size))
}