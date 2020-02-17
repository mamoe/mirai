package net.mamoe.mirai.console.graphical.view

import com.jfoenix.controls.JFXListCell
import javafx.scene.control.TabPane
import javafx.scene.image.Image
import net.mamoe.mirai.console.graphical.controller.MiraiGraphicalUIController
import net.mamoe.mirai.console.graphical.model.BotModel
import net.mamoe.mirai.console.graphical.util.jfxButton
import net.mamoe.mirai.console.graphical.util.jfxListView
import net.mamoe.mirai.console.graphical.util.jfxTabPane
import tornadofx.*
import java.io.FileInputStream

class PrimaryView : View() {

    private val controller = find<MiraiGraphicalUIController>()

    override val root = borderpane {

        prefWidth = 1000.0
        prefHeight = 650.0

        left = vbox {

            imageview(Image(FileInputStream("logo.png")))

            // bot list
            jfxListView(controller.botList) {
                fitToParentSize()
//                prefHeight = 100.0
                setCellFactory {
                    object : JFXListCell<BotModel>() {
                        init {
                            onDoubleClick {
                                (center as TabPane).tab(item.uin.toString()) {
                                    listview(item.logHistory)
                                    select()
                                    onDoubleClick { close() }
                                }
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

            gridpane {
                row {
                    jfxButton("登录")
                    jfxButton("插件")
                    jfxButton("设置")
                }
            }
        }

        center = jfxTabPane {
            tab("Main") {
                listview(controller.mainLog) {

                    fitToParentSize()
                    cellFormat {
                        graphic = label(it) {
                            maxWidthProperty().bind(this@listview.widthProperty())
                            isWrapText = true
                        }
                    }
                }
            }
        }
    }
}