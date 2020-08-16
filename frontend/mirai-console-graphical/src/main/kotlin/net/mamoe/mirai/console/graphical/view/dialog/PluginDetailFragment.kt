/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

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