package net.mamoe.mirai.console.graphical.styleSheet

import javafx.geometry.Pos
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.box
import tornadofx.c
import tornadofx.cssclass
import tornadofx.px

class PluginViewStyleSheet : BaseStyleSheet() {

    companion object {
        val jfxTreeTableView by cssclass("jfx-tree-table-view")
        val treeTableRowCell by cssclass("tree-table-row-cell")
        val columnHeader by cssclass("column-header")
        val columnHeaderBg by cssclass("column-header-background")
    }


    init {
        jfxTreeTableView {
            backgroundColor += TRANSPARENT

            columnHeader {
                borderWidth += box(0.px)

                label {
                    textFill = Color.BLACK
                }
            }

            columnHeaderBg {
                backgroundColor += c(lightColor, 0.4)
            }

            treeTableCell {
                alignment = Pos.CENTER
            }

            treeTableRowCell {

                fontWeight = FontWeight.SEMI_BOLD


                backgroundColor += TRANSPARENT

                and(":selected") {
                    backgroundColor += c(stressColor, 1.0)
                }

                and(":hover:filled") {
                    backgroundColor += c(stressColor, 0.6)
                    and(":selected") {
                        backgroundColor += c(stressColor, 1.0)
                    }
                }
            }
        }
    }
}