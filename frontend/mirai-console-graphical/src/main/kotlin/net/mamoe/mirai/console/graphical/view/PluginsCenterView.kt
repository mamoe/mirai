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
import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.TreeTableCell
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.graphical.controller.MiraiGraphicalFrontEndController
import net.mamoe.mirai.console.graphical.event.ReloadEvent
import net.mamoe.mirai.console.graphical.model.PluginModel
import net.mamoe.mirai.console.graphical.stylesheet.PluginViewStyleSheet
import net.mamoe.mirai.console.graphical.util.jfxButton
import net.mamoe.mirai.console.graphical.util.jfxTreeTableView
import net.mamoe.mirai.console.graphical.view.dialog.PluginDetailFragment
import tornadofx.*

class PluginsCenterView : View() {

    private val controller = find<MiraiGraphicalFrontEndController>()
    private val center get() = MiraiConsole.frontEnd.pluginCenter
    private val plugins: ObservableList<PluginModel> = observableListOf()

    init {
        // 监听插件重载，情况插件列表，重新载入。
        // 同时把页面刷新，按键的listener也初始化
        subscribe<ReloadEvent> { plugins.clear() }
    }

    override val root = jfxTreeTableView(plugins) {

        addStylesheet(PluginViewStyleSheet::class)

        placeholder = button("从崔云获取插件列表") {
            action {
                isDisable = true
                runAsync {
                    fetch()
                }.ui {
                    plugins.addAll(it)
                    isDisable = false
                }
            }
        }

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
                prefWidthProperty().bind(this@jfxTreeTableView.widthProperty().multiply(0.48))

                setCellValueFactory {
                    return@setCellValueFactory it.value.value.descriptionProperty
                }
            },
            JFXTreeTableColumn<PluginModel, PluginModel>("操作").apply {
                prefWidthProperty().bind(this@jfxTreeTableView.widthProperty().multiply(0.2))

                setCellValueFactory { return@setCellValueFactory it.value.valueProperty() }

                setCellFactory {
                    return@setCellFactory object : TreeTableCell<PluginModel, PluginModel>() {
                        override fun updateItem(item: PluginModel?, empty: Boolean) {
                            if (item != null && !empty) {
                                graphic = hbox {

                                    spacing = 15.0
                                    alignment = Pos.CENTER

                                    jfxButton("详情") {
                                        action { detail(item) }
                                    }


                                    jfxButton(if (item.expired) "更新" else  "下载") {
                                        action { download(item, this) }
                                    }

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

    private fun fetch(): List<PluginModel> = mutableListOf<PluginModel>().apply {
        runBlocking {
            var page = 1
            while (true) {
                val map = center.fetchPlugin(page++)
                if (map.isEmpty()) return@runBlocking
                map.forEach {
                    with(
                        PluginModel(
                            it.value.name,
                            it.value.version,
                            it.value.author,
                            it.value.description,
                            it.value
                        )
                    ) {
                        add(this)
                        controller.checkUpdate(this)
                        controller.checkAmbiguous(this)
                    }
                }
            }
        }
    }

    private fun detail(pluginModel: PluginModel) {
        runAsync {
            runBlocking { center.findPlugin(pluginModel.name) }
        }.ui {
            it?.apply {
                PluginDetailFragment(this).openModal()
            }
        }
    }

    private fun download(pluginModel: PluginModel, button: Button) {
        button.isDisable = true
        button.text = "连接中..."
        runAsync {
            runBlocking {
                center.downloadPlugin(pluginModel.name) {
                    // download process
                    Platform.runLater {
                        button.text = "$it%"
                    }
                }
            }
        }.ui {
            with(button) {
                isDisable = false
                text = "重载插件"
                setOnAction {
                    controller.reloadPlugins()
                }
            }
        }
    }

}