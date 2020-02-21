package net.mamoe.mirai.console.graphical.view

import com.jfoenix.controls.JFXTreeTableColumn
import net.mamoe.mirai.console.graphical.controller.MiraiGraphicalUIController
import net.mamoe.mirai.console.graphical.model.PluginModel
import net.mamoe.mirai.console.graphical.util.jfxTreeTableView
import tornadofx.View

class PluginsView : View() {

    private val controller = find<MiraiGraphicalUIController>()
    val plugins = controller.pluginList

    override val root = jfxTreeTableView(plugins) {
        columns.addAll(
            JFXTreeTableColumn<PluginModel, String>("插件名").apply {  },
            JFXTreeTableColumn<PluginModel, String>("版本").apply {  },
            JFXTreeTableColumn<PluginModel, String>("作者").apply {  },
            JFXTreeTableColumn<PluginModel, String>("介绍").apply {  }
        )
    }
}