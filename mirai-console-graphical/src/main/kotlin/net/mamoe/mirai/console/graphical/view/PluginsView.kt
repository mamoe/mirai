package net.mamoe.mirai.console.graphical.view

import com.jfoenix.controls.JFXTreeTableColumn
import javafx.scene.control.TreeTableCell
import net.mamoe.mirai.console.graphical.controller.MiraiGraphicalUIController
import net.mamoe.mirai.console.graphical.event.ReloadEvent
import net.mamoe.mirai.console.graphical.model.PluginModel
import net.mamoe.mirai.console.graphical.stylesheet.PluginViewStyleSheet
import net.mamoe.mirai.console.graphical.util.jfxButton
import net.mamoe.mirai.console.graphical.util.jfxTreeTableView
import tornadofx.View
import tornadofx.addStylesheet
import tornadofx.visibleWhen

class PluginsView : View() {

    private val controller = find<MiraiGraphicalUIController>()
    val plugins = controller.pluginList

    override val root = jfxTreeTableView(plugins) {

        addStylesheet(PluginViewStyleSheet::class)

        isShowRoot = false
        columns.addAll(
            JFXTreeTableColumn<PluginModel, String>("插件名").apply {
                prefWidthProperty().bind(this@jfxTreeTableView.widthProperty().multiply(0.1))

                setCellValueFactory {
                    return@setCellValueFactory it.value.value.nameProperty
                }
            },
            JFXTreeTableColumn<PluginModel, String>("版本").apply {
                prefWidthProperty().bind(this@jfxTreeTableView.widthProperty().multiply(0.1))

                setCellValueFactory {
                    return@setCellValueFactory it.value.value.versionProperty
                }
            },
            JFXTreeTableColumn<PluginModel, String>("作者").apply {
                prefWidthProperty().bind(this@jfxTreeTableView.widthProperty().multiply(0.1))

                setCellValueFactory {
                    return@setCellValueFactory it.value.value.authorProperty
                }
            },
            JFXTreeTableColumn<PluginModel, String>("介绍").apply {
                prefWidthProperty().bind(this@jfxTreeTableView.widthProperty().multiply(0.6))

                setCellValueFactory {
                    return@setCellValueFactory it.value.value.descriptionProperty
                }
            },
            JFXTreeTableColumn<PluginModel, PluginModel>("操作").apply {
                prefWidthProperty().bind(this@jfxTreeTableView.widthProperty().multiply(0.08))

                setCellValueFactory { return@setCellValueFactory it.value.valueProperty() }

                setCellFactory {
                    return@setCellFactory object : TreeTableCell<PluginModel, PluginModel>() {
                        override fun updateItem(item: PluginModel?, empty: Boolean) {
                            if (item != null && !empty) {
                                graphic = jfxButton("更新") {
                                    visibleWhen(item.expiredProperty)

                                    // to do update
                                }
                                text = ""
                            } else {
                                graphic = null
                                text = ""
                            }
                        }
                    }
                }
            }
        )
    }
}