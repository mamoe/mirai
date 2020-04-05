package net.mamoe.mirai.console.graphical.view

import com.jfoenix.controls.JFXTreeTableColumn
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.TreeTableCell
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.center.PluginCenter
import net.mamoe.mirai.console.graphical.controller.MiraiGraphicalUIController
import net.mamoe.mirai.console.graphical.model.PluginModel
import net.mamoe.mirai.console.graphical.stylesheet.PluginViewStyleSheet
import net.mamoe.mirai.console.graphical.util.jfxButton
import net.mamoe.mirai.console.graphical.util.jfxTreeTableView
import tornadofx.*

class PluginsCenterView : View() {

    private val controller = find<MiraiGraphicalUIController>()
    private val center = PluginCenter.Default
    private val plugins: ObservableList<PluginModel> by lazy(::fetch)

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


                                    jfxButton("下载") {
                                        action { download(item) }
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

    private fun fetch(): ObservableList<PluginModel> =
        runAsync {
            val ret = observableListOf<PluginModel>()
            runBlocking {
                var page = 1
                while (true) {
                    val map = center.fetchPlugin(page++)
                    if (map.isEmpty()) return@runBlocking
                    map.forEach {
                        with(PluginModel(
                            it.value.name,
                            it.value.version,
                            it.value.author,
                            it.value.description,
                            it.value
                        )) {
                            ret.add(this)
                            controller.checkUpdate(this)
                            controller.checkAmbiguous(this)
                        }
                    }
                }
            }
            return@runAsync ret
        }.get()

    private fun detail(pluginModel: PluginModel) {
        //to show pluginModel.insight
    }

    private fun download(pluginModel: PluginModel) {
        // controller.checkAmbiguous(pluginModel)
        // do nothing
    }

}