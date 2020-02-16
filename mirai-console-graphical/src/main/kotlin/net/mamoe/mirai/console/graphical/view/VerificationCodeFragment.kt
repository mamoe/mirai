package net.mamoe.mirai.console.graphical.view

import javafx.scene.Parent
import tornadofx.*

class VerificationCodeFragment : Fragment() {

    override val root = vbox {
        //TODO: 显示验证码

        form {
            fieldset {
                field("验证码") {
                    textfield()
                }
            }
        }
    }
}