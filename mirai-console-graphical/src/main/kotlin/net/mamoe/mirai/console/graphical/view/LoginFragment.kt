package net.mamoe.mirai.console.graphical.view

import com.jfoenix.controls.JFXTextField
import javafx.beans.property.SimpleStringProperty
import net.mamoe.mirai.console.graphical.controller.MiraiGraphicalUIController
import net.mamoe.mirai.console.graphical.util.jfxButton
import net.mamoe.mirai.console.graphical.util.jfxPasswordfield
import net.mamoe.mirai.console.graphical.util.jfxTextfield
import tornadofx.*

class LoginFragment : Fragment() {

    private val controller = find<MiraiGraphicalUIController>(FX.defaultScope)
    private val qq = SimpleStringProperty("0")
    private val psd = SimpleStringProperty("")

    override val root = form {
        fieldset("登录") {
            field("QQ") {
                jfxTextfield(qq)
            }
            field("密码") {
                jfxPasswordfield(psd)
            }
        }
        jfxButton("登录").action {
            controller.login(qq.value, psd.value)
            close()
        }
    }
}