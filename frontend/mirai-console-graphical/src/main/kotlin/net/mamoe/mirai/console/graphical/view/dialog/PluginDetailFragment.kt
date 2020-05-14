package net.mamoe.mirai.console.graphical.view.dialog

import javafx.geometry.Insets
import net.mamoe.mirai.console.center.PluginCenter
import net.mamoe.mirai.console.graphical.util.jfxTextfield
import tornadofx.Fragment
import tornadofx.vbox

class PluginDetailFragment(info: PluginCenter.PluginInfo) : Fragment() {

    init {
        title = info.name
    }

    override val root = vbox {

        prefWidth = 450.0
        padding = Insets(25.0)
        spacing = 25.0

        jfxTextfield(info.name) {
            promptText = "插件名"
            isLabelFloat = true
        }

        jfxTextfield(info.version) {
            promptText = "版本号"
            isLabelFloat = true
        }

        jfxTextfield(info.coreVersion) {
            promptText = "Mirai核心版本"
            isLabelFloat = true
        }

        jfxTextfield(info.consoleVersion) {
            promptText = "Mirai控制台版本"
            isLabelFloat = true
        }

        jfxTextfield(info.tags.joinToString(",")) {
            promptText = "标签"
            isLabelFloat = true
        }

        jfxTextfield(info.author) {
            promptText = "作者"
            isLabelFloat = true
        }

        jfxTextfield(info.description) {
            promptText = "描述"
            isLabelFloat = true
        }

        jfxTextfield(info.usage) {
            promptText = "使用方法"
            isLabelFloat = true
        }

        jfxTextfield(info.vcs) {
            promptText = "仓库地址"
            isLabelFloat = true
        }

        jfxTextfield(info.commands.joinToString("\n\n")) {
            promptText = "命令"
            isLabelFloat = true
        }

        jfxTextfield(info.changeLog.joinToString("\n\n")) {
            promptText = "修改日志"
            isLabelFloat = true
        }
    }
}