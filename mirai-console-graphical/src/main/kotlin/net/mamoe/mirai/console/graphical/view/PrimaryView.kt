package net.mamoe.mirai.console.graphical.view

import com.jfoenix.controls.JFXListCell
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
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

            imageview(Image(PrimaryView::class.java.classLoader.getResourceAsStream("logo.png")))

            // bot list
            jfxListView(controller.botList) {
                fitToParentSize()

                setCellFactory {
                    object : JFXListCell<BotModel>() {
                        var tab: Tab? = null

                        init {
                            onDoubleClick {
                                if (tab == null) {
                                    (center as TabPane).tab(item.uin.toString()) {
                                        listview(item.logHistory)
                                        onDoubleClick { close() }
                                        tab = this
                                    }
                                } else {
                                    (center as TabPane).tabs.add(tab)
                                }
                                tab?.select()
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

            hbox {
                padding = Insets(10.0)
                spacing = 10.0
                alignment = Pos.CENTER

                jfxButton("L").action {
                    find<LoginFragment>().openModal()
                }
                jfxButton("P")
                jfxButton("S")


                style { backgroundColor += c("00BCD4") }
                children.style(true) {
                    backgroundColor += c("00BCD4")
                    fontSize = 15.px
                    fontWeight = FontWeight.BOLD
                    textFill = Color.WHITE
                    borderRadius += box(25.px)
                    backgroundRadius += box(25.px)
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
