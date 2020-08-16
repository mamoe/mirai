/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.graphical.stylesheet

import javafx.scene.Cursor
import javafx.scene.effect.BlurType
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.box
import tornadofx.c
import tornadofx.csselement
import tornadofx.px

class LoginViewStyleSheet : BaseStyleSheet() {

    companion object {
        val vBox by csselement("VBox")
    }

    init {

        /*
         * center box
         */
        vBox {
            maxWidth = 500.px
            maxHeight = 500.px

            backgroundColor += c(primaryColor, 0.3)
            backgroundRadius += box(15.px)

            padding = box(50.px, 100.px)
            spacing = 25.px

            borderRadius += box(15.px)
            effect = DropShadow(BlurType.THREE_PASS_BOX, Color.GRAY, 10.0, 0.0, 15.0, 15.0)
        }

        textField {
            prefHeight = 30.px
            textFill = Color.BLACK
            fontWeight = FontWeight.BOLD
        }

        /*
         * login button
         */
        button {
            backgroundColor += c(stressColor, 0.8)
            padding = box(10.px, 0.px)
            prefWidth = 500.px
            textFill = Color.WHITE
            fontWeight = FontWeight.BOLD
            cursor = Cursor.HAND
        }
    }
}