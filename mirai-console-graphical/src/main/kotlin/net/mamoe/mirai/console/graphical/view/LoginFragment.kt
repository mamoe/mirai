package net.mamoe.mirai.console.graphical.view

import javafx.beans.property.SimpleStringProperty
import net.mamoe.mirai.console.graphical.controller.MiraiGraphicalUIController
import tornadofx.*

class LoginFragment : Fragment() {

    private val controller = find<MiraiGraphicalUIController>(FX.defaultScope)
    private val qq = SimpleStringProperty()
    private val psd = SimpleStringProperty()

    override val root = form {
        fieldset("登录") {
            field("QQ") {
                textfield(qq)
            }
            field("密码") {
                passwordfield(psd)
            }
            button("登录").action {
                controller.login(qq.value, psd.value)
                close()
            }
        }
    }
}