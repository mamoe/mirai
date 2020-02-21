package net.mamoe.mirai.console.graphical.view

import net.mamoe.mirai.console.graphical.controller.MiraiGraphicalUIController
import net.mamoe.mirai.console.graphical.util.jfxTextfield
import tornadofx.View
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form

class SettingsView : View() {

    private val controller = find<MiraiGraphicalUIController>()

    override val root = form {
        controller.consoleConfig.forEach {
            fieldset {
                field(it.key) {
                    jfxTextfield(it.value.toString()) { isEditable = false }
                }
            }
        }
    }
}