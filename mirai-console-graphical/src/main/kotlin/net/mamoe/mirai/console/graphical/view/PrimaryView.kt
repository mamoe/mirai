package net.mamoe.mirai.console.graphical.view

import com.jfoenix.controls.JFXListCell
import javafx.collections.ObservableList
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.stage.FileChooser
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.graphical.controller.MiraiGraphicalUIController
import net.mamoe.mirai.console.graphical.model.BotModel
import net.mamoe.mirai.console.graphical.util.jfxListView
import net.mamoe.mirai.console.graphical.util.jfxTabPane
import tornadofx.*

class PrimaryView : View() {

    private val controller = find<MiraiGraphicalUIController>()
    private lateinit var mainTabPane: TabPane

    override val root = borderpane {

        left = vbox {

            imageview(Image(PrimaryView::class.java.classLoader.getResourceAsStream("logo.png"))) {
                isPreserveRatio = true
            }

            // bot list
            jfxListView(controller.botList) {
                fitToParentSize()

                setCellFactory {
                    object : JFXListCell<BotModel>() {
                        var tab: Tab? = null

                        init {
                            onDoubleClick {
                                tab?.select() ?: (center as TabPane).logTab(
                                    text = item.uin.toString(),
                                    logs = item.logHistory
                                ).select().also { tab = it }
                            }
                        }

                        override fun updateItem(item: BotModel?, empty: Boolean) {
                            super.updateItem(item, empty)
                            if (item != null && !empty) {
                                graphic = null
                                text = item.uin.toString()
                            } else {
                                graphic = null
                                text = ""
                            }
                        }
                    }
                }
            }

            // command input
            textfield {
                setOnKeyPressed {
                    if (it.code == KeyCode.ENTER) {
                        runAsync {
                            runBlocking { controller.sendCommand(text) }
                        }.ui { text = "" }
                    }
                }
            }
        }

        center = jfxTabPane {

            tabClosingPolicy = TabPane.TabClosingPolicy.ALL_TABS

            logTab("Main", controller.mainLog, closeable = false)

            fixedTab("Plugins").content = find<PluginsView>().root

            fixedTab("Settings").content = find<SettingsView>().root

            fixedTab("Login").content = find<LoginView>().root

            mainTabPane = this
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
    logs: ObservableList<String>,
    closeable: Boolean = true,
    op: Tab.() -> Unit = {}
) = tab(text) {

    this.isClosable = closeable

    vbox {
        buttonbar {

            button("导出日志").action {
                val path = chooseFile(
                    "选择保存路径",
                    arrayOf(FileChooser.ExtensionFilter("日志", "txt")),
                    FileChooserMode.Save
                ) {
                    initialFileName = "$text.txt"
                }
                runAsyncWithOverlay {
                    path.firstOrNull()?.run {
                        if (!exists()) createNewFile()
                        writer().use {
                            logs.forEach { log -> it.appendln(log) }
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
                graphic = label(it) {
                    maxWidthProperty().bind(this@listview.widthProperty())
                    isWrapText = true
                }
            }
        }
    }
    also(op)
}