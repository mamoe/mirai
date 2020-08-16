/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

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