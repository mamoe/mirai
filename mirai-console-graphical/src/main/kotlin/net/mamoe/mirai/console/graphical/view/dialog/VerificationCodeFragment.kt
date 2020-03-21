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