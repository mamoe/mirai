/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.graphical.stylesheet

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