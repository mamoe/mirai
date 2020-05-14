package net.mamoe.mirai.console.graphical.view

import javafx.geometry.Pos
import net.mamoe.mirai.console.graphical.controller.MiraiGraphicalUIController
import net.mamoe.mirai.console.graphical.model.GlobalSettingModel
import net.mamoe.mirai.console.graphical.util.jfxButton
import net.mamoe.mirai.console.graphical.util.jfxTextfield
import net.mamoe.mirai.console.graphical.util.myButtonBar
import tornadofx.*
import java.awt.Desktop
import java.io.File

class SettingsView : View() {

    private val controller = find<MiraiGraphicalUIController>()
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