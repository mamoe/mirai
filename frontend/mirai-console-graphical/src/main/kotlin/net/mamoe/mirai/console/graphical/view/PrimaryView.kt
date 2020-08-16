/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.graphical.view

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXListCell
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.image.Image
import javafx.scene.input.Clipboard
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.stage.FileChooser
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.graphical.controller.MiraiGraphicalFrontEndController
import net.mamoe.mirai.console.graphical.model.BotModel
import net.mamoe.mirai.console.graphical.util.*
import tornadofx.*

class PrimaryView : View() {

    private val controller = find<MiraiGraphicalFrontEndController>()
    private lateinit var mainTabPane: TabPane

    override val root = borderpane {

        addClass("root-pane")

        left = vbox {

            addClass("left-pane")

            imageview(Image(PrimaryView::class.java.classLoader.getResourceAsStream("logo.png"))) {
                fitHeight = 40.0
                alignment = Pos.CENTER
                isPreserveRatio = true
            }

            // bot list
            jfxListView(controller.botList) {
                fitToParentHeight()

                placeholder = vbox {

                    alignment = Pos.CENTER

                    label("Bot列表为空，请登录一个Bot")

                    jfxButton("登录") {
                        buttonType = JFXButton.ButtonType.FLAT
                    }.action {
                        // select login pane
                        mainTabPane.selectionModel.selectLast()
                    }
                }

                setCellFactory {
                    object : JFXListCell<BotModel>() {
                        var tab: Tab? = null

                        init {
                            onDoubleClick {
                                tab?.select() ?: mainTabPane.logTab(
                                    text = item.uin.toString(),
                                    logs = item.logHistory
                                ).select().also { tab = it }
                            }
                        }

                        override fun updateItem(item: BotModel?, empty: Boolean) {
                            super.updateItem(item, empty)
                            if (item != null && !empty) {
                                graphic = hbox {

                                    alignment = Pos.CENTER_LEFT

                                    label(item.uin.toString())
                                    pane {
                                        hgrow = Priority.ALWAYS
                                    }
                                    jfxButton(graphic = SVG.close) {
                                        buttonType = JFXButton.ButtonType.FLAT
                                        tooltip("退出登录")
                                    }.action {
                                        alert(Alert.AlertType.CONFIRMATION, "${item.uin}将会退出登录，是否确认") {
                                            if (it == ButtonType.OK) {
                                                tab?.close()
                                                controller.logout(item.uin)
                                            }
                                        }
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
        }

        center = vbox {

            jfxTabPane {

                fitToParentHeight()

                tabClosingPolicy = TabPane.TabClosingPolicy.ALL_TABS

                logTab("Main", controller.mainLog, closeable = false)

                fixedTab("Plugins").content = find<PluginsView>().root

                fixedTab("Plugins Center").content = find<PluginsCenterView>().root

                fixedTab("Settings").content = find<SettingsView>().root

                fixedTab("Login").content = find<LoginView>().root

                mainTabPane = this
            }

            // command input
            textfield {

                promptText = "在这里输出命令"

                setOnKeyPressed {
                    if (it.code == KeyCode.ENTER) {
                        runAsync {
                            runBlocking { controller.sendCommand(text) }
                        }.ui { text = "" }
                    }
                }
            }
        }
    }

    fun Tab.select() = apply {
        if (!mainTabPane.tabs.contains(this)) mainTabPane.tabs.add(this)
        mainTabPane.selectionModel.select(this)
    }
}

private fun TabPane.fixedTab(title: String) = tab(title) { isClosable = false }

private fun TabPane.logTab(
    text: String? = null,
    logs: ObservableList<Pair<String, String>>,
    closeable: Boolean = true,
    op: Tab.() -> Unit = {}
) = tab(text) {

    this.isClosable = closeable

    vbox {
        myButtonBar(alignment = Pos.BASELINE_RIGHT) {

            jfxButton("导出日志").action {
                val path = chooseFile(
                    "选择保存路径",
                    arrayOf(FileChooser.ExtensionFilter("日志", "txt")),
                    FileChooserMode.Save,
                    owner = FX.primaryStage
                ) {
                    initialFileName = "$text.txt"
                }
                runAsyncWithOverlay {
                    path.firstOrNull()?.run {
                        if (!exists()) createNewFile()
                        writer().use {
                            logs.forEach { log -> it.appendln(log.first) }
                        }
                        true
                    } ?: false
                }.ui {// isSucceed: Boolean ->
                    // notify something
                }
            }
        }

        listview(logs) {

            fitToParentSize()
            cellFormat {

                addPseudoClass(it.second)

                graphic = label(it.first) {
                    maxWidthProperty().bind(this@listview.widthProperty())
                    isWrapText = true


                    contextmenu {
                        item("复制").action {
                            Clipboard.getSystemClipboard().putString(it.first)
                        }
                        item("删除").action {
                            logs.remove(it)
                        }
                    }
                }
            }
        }
    }
    also(op)
}