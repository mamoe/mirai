package net.mamoe.mirai.console.graphical.view

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXListCell
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.stage.FileChooser
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.graphical.controller.MiraiGraphicalUIController
import net.mamoe.mirai.console.graphical.model.BotModel
import net.mamoe.mirai.console.graphical.util.jfxButton
import net.mamoe.mirai.console.graphical.util.jfxListView
import net.mamoe.mirai.console.graphical.util.jfxTabPane
import net.mamoe.mirai.console.graphical.util.myButtonBar
import tornadofx.*

class PrimaryView : View() {

    private val controller = find<MiraiGraphicalUIController>()
    private lateinit var mainTabPane: TabPane

    override val root = borderpane {

        addClass("root-pane")

        left = vbox {

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
                        mainTabPane.selectionModel.select(3)
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
        }

        center = vbox {

            jfxTabPane {

                fitToParentHeight()

                tabClosingPolicy = TabPane.TabClosingPolicy.ALL_TABS

                logTab("Main", controller.mainLog, closeable = false)

                fixedTab("Plugins").content = find<PluginsView>().root

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
    logs: ObservableList<String>,
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