package net.mamoe.mirai.console.graphical.view

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.image.Image
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.graphical.controller.MiraiGraphicalUIController
import net.mamoe.mirai.console.graphical.styleSheet.LoginViewStyleSheet
import net.mamoe.mirai.console.graphical.util.jfxButton
import net.mamoe.mirai.console.graphical.util.jfxPasswordfield
import net.mamoe.mirai.console.graphical.util.jfxTextfield
import tornadofx.*

class LoginView : View("CNM") {

    private val controller = find<MiraiGraphicalUIController>()
    private val qq = SimpleStringProperty("")
    private val psd = SimpleStringProperty("")

    override val root = borderpane {

        addStylesheet(LoginViewStyleSheet::class)

        center = vbox {

            imageview(Image(LoginView::class.java.classLoader.getResourceAsStream("character.png"))) {
                alignment = Pos.CENTER
            }

            jfxTextfield(qq) {
                promptText = "QQ"
                isLabelFloat = true
            }

            jfxPasswordfield(psd) {
                promptText = "Password"
                isLabelFloat = true
            }

            jfxButton("Login").action {
                runAsync {
                    runBlocking { controller.login(qq.value, psd.value) }
                }.ui {
                    qq.value = ""
                    psd.value = ""
                }
            }
        }
    }
}