/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.graphical.view.dialog

import javafx.scene.image.Image
import net.mamoe.mirai.console.graphical.model.VerificationCodeModel
import tornadofx.*
import java.io.ByteArrayInputStream

class VerificationCodeFragment : Fragment() {

    companion object {
        val MAGIC_KEY = String("CANCEL".toByteArray())
    }

    val code = find<VerificationCodeModel>()

    override val root = vbox {
        // 显示验证码
        imageview(Image(ByteArrayInputStream(code.data.value)))

        form {
            fieldset {
                field("验证码") {
                    textfield(code.code)
                }
            }

            buttonbar {
                button("提交").action {
                    code.commit()
                    this@VerificationCodeFragment.close()
                }
                button("取消").action {
                    code.code.value =
                        MAGIC_KEY
                    code.commit()
                    this@VerificationCodeFragment.close()
                }
            }
        }
    }
}