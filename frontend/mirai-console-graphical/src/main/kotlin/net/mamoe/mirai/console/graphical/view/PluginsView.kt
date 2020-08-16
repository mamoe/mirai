/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.graphical.view

import com.jfoenix.controls.JFXTreeTableColumn
import javafx.scene.control.TreeTableCell
import net.mamoe.mirai.console.graphical.controller.MiraiGraphicalFrontEndController
import net.mamoe.mirai.console.graphical.model.PluginModel
import net.mamoe.mirai.console.graphical.stylesheet.PluginViewStyleSheet
import net.mamoe.mirai.console.graphical.util.jfxButton
import net.mamoe.mirai.console.graphical.util.jfxTreeTableView
import tornadofx.View
import tornadofx.addStylesheet
import tornadofx.visibleWhen

class PluginsView : View() {

    private val controller = find<MiraiGraphicalFrontEndController>()
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