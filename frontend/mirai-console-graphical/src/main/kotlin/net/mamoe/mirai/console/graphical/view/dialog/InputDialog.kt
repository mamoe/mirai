package net.mamoe.mirai.console.graphical.view.dialog

import javafx.scene.control.TextField
import javafx.stage.Modality
import javafx.stage.StageStyle
import tornadofx.*

class InputDialog(title: String) : Fragment() {

    private lateinit var input: TextField

    init {
        titleProperty.value = title
    }

    override val root = form {

        fieldset {
            field(title) {
                input = textfield("")
            }

            buttonbar {

                button("提交").action { close() }
            }
        }
    }

    fun open(): String {
        // 阻塞窗口直到关闭
        openModal(
            stageStyle = StageStyle.DECORATED,
            modality = Modality.APPLICATION_MODAL,
            block = true
        )
        return input.text
    }
}