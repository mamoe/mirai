package net.mamoe.mirai.console.graphical.view

import com.jfoenix.controls.JFXAlert
import com.jfoenix.controls.JFXPopup
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Label
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.graphical.controller.MiraiGraphicalUIController
import net.mamoe.mirai.console.graphical.util.jfxButton
import net.mamoe.mirai.console.graphical.util.jfxPasswordfield
import net.mamoe.mirai.console.graphical.util.jfxTextfield
import tornadofx.*

class LoginView : View() {

    private val controller = find<MiraiGraphicalUIController>()
    private val qq = SimpleStringProperty("")
    private val psd = SimpleStringProperty("")

    override val root = pane {
        form {
            fieldset("登录") {
                field("QQ") {
                    jfxTextfield(qq)
                }
                field("密码") {
                    jfxPasswordfield(psd)
                }
            }
            jfxButton("登录").action {
                runAsync {
                    runBlocking {
                        controller.login(qq.value, psd.value)
                    }
                }.ui {
                    // show dialog
                }
            }
        }
    }
}