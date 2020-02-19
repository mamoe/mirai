package net.mamoe.mirai.console.graphical.view

import com.jfoenix.controls.*
import javafx.collections.ObservableList
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.image.Image
import net.mamoe.mirai.console.graphical.controller.MiraiGraphicalUIController
import net.mamoe.mirai.console.graphical.model.BotModel
import net.mamoe.mirai.console.graphical.util.jfxListView
import net.mamoe.mirai.console.graphical.util.jfxTabPane
import tornadofx.*

class PrimaryView : View() {

    private val controller = find<MiraiGraphicalUIController>()

    override val root = borderpane {

        prefWidth = 1000.0
        prefHeight = 650.0

        left = vbox {

            imageview(Image(PrimaryView::class.java.classLoader.getResourceAsStream("logo.png")))

            // bot list
            jfxListView(controller.botList) {
                fitToParentSize()

                setCellFactory {
                    object : JFXListCell<BotModel>() {
                        init {
                            onDoubleClick {
                                (center as TabPane).logTab(
                                    text = item.uin.toString(),
                                    logs = item.logHistory
                                ).select()
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

        center = jfxTabPane {

            tab("Login") {
                this += find<LoginView>().root
            }

            tab("Plugin")

            tab("Settings")

            logTab("Main", controller.mainLog)
        }
    }
}

private fun TabPane.logTab(
    text: String? = null,
    logs: ObservableList<String>,
    op: Tab.() -> Unit = {}
)= tab(text) {
    listview(logs) {

        fitToParentSize()
        cellFormat {
            graphic = label(it) {
                maxWidthProperty().bind(this@listview.widthProperty())
                isWrapText = true
            }
        }
    }
    also(op)
}