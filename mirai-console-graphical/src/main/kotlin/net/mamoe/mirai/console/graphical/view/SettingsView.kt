package net.mamoe.mirai.console.graphical.view

import net.mamoe.mirai.console.graphical.controller.MiraiGraphicalUIController
import net.mamoe.mirai.console.graphical.util.jfxButton
import net.mamoe.mirai.console.graphical.util.jfxTextfield
import tornadofx.*

class SettingsView : View() {

    private val controller = find<MiraiGraphicalUIController>()

    override val root = form {

        fieldset {
            field {
                jfxButton("撤掉") {  }
                jfxButton("保存") {  }
            }
        }

        fieldset("插件目录") {
            field {
                jfxTextfield("...") { isEditable = false }
                jfxButton("打开目录")
            }
        }

        fieldset("最大日志容量") {
            field {
                jfxTextfield("...") {

                }
            }
        }
    }
}