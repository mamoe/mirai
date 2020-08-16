/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.graphical.view

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.image.Image
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.graphical.controller.MiraiGraphicalFrontEndController
import net.mamoe.mirai.console.graphical.stylesheet.LoginViewStyleSheet
import net.mamoe.mirai.console.graphical.util.jfxButton
import net.mamoe.mirai.console.graphical.util.jfxPasswordfield
import net.mamoe.mirai.console.graphical.util.jfxTextfield
import tornadofx.*

class LoginView : View("CNM") {

    private val controller = find<MiraiGraphicalFrontEndController>()
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