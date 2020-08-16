/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.graphical.view

import javafx.geometry.Pos
import net.mamoe.mirai.console.graphical.controller.MiraiGraphicalFrontEndController
import net.mamoe.mirai.console.graphical.model.GlobalSettingModel
import net.mamoe.mirai.console.graphical.util.jfxButton
import net.mamoe.mirai.console.graphical.util.jfxTextfield
import net.mamoe.mirai.console.graphical.util.myButtonBar
import tornadofx.*
import java.awt.Desktop
import java.io.File

class SettingsView : View() {

    private val controller = find<MiraiGraphicalFrontEndController>()
    private val settingModel = find<GlobalSettingModel>()

    override val root = vbox {

        myButtonBar(alignment = Pos.BASELINE_RIGHT) {
            jfxButton("撤掉").action {
                settingModel.rollback()
            }
            jfxButton("保存").action {
                settingModel.commit()
            }
        }

        form {
            fieldset("插件目录") {
                field {
                    jfxTextfield((System.getProperty("user.dir") + "/plugins/").replace("//", "/")) { isEditable = false }
                    jfxButton("打开目录").action {
                        (System.getProperty("user.dir") + "/plugins/").replace("//", "/").also { path ->
                            Desktop.getDesktop().takeIf { it.isSupported(Desktop.Action.OPEN) }?.open(File(path))
                        }
                    }
                }
            }

            fieldset("背景目录") {
                field {
                    jfxTextfield((System.getProperty("user.dir") + "/background/").replace("//", "/")) { isEditable = false }
                    jfxButton("打开目录").action {
                        (System.getProperty("user.dir") + "/background/").replace("//", "/").also { path ->
                            Desktop.getDesktop().takeIf { it.isSupported(Desktop.Action.OPEN) }?.open(File(path))
                        }
                    }
                }
            }

            fieldset("最大日志容量") {
                field {
                    jfxTextfield().bind(settingModel.maxLogNum)
                }
            }
        }
    }
}