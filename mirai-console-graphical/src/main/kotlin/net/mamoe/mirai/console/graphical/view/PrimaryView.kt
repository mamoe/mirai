package net.mamoe.mirai.console.graphical.view

import javafx.scene.control.TabPane
import javafx.stage.Modality
import net.mamoe.mirai.console.graphical.controller.MiraiGraphicalUIController
import tornadofx.*

class PrimaryView : View() {

    private val controller = find<MiraiGraphicalUIController>()

    override val root = borderpane {

        top = menubar {
            menu("机器人") {
                item("登录").action {
                    find<LoginFragment>().openModal(
                        modality = Modality.APPLICATION_MODAL,
                        resizable = false
                    )
                }
            }
        }

        left = listview(controller.botList) {
            fitToParentHeight()

            cellFormat {

                graphic = vbox {
                    label(it.uin.toString())
//                    label(stringBinding(it.botProperty) { if (value != null) value.nick else "登陆中" })
                }

                onDoubleClick {
                    (center as TabPane).tab(it.uin.toString()) {
                        listview(it.logHistory)

                        isClosable = true
                        select()
                    }
                }
            }
        }

        center = tabpane {
            tab("Main") {
                listview(controller.mainLog)

                isClosable = false
            }
        }
    }

}