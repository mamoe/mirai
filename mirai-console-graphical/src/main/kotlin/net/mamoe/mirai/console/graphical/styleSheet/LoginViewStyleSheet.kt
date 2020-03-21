package net.mamoe.mirai.console.graphical.styleSheet

import javafx.scene.Cursor
import javafx.scene.effect.BlurType
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

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