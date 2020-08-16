/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.graphical.stylesheet

import javafx.scene.Cursor
import javafx.scene.text.FontWeight
import tornadofx.box
import tornadofx.c
import tornadofx.cssclass
import tornadofx.px

class PrimaryStyleSheet : BaseStyleSheet() {
    companion object {
        // window
        val jfxTitle by cssclass("jfx-decorator-buttons-container")
        val container by cssclass("jfx-decorator-content-container")

        // jfx tab
        val jfxTabHeader by cssclass("tab-header-background")
        val closeButton by cssclass("tab-close-button")

        // jfx list view
        val leftPane by cssclass("left-pane")
        val jfxListView by cssclass("jfx-list-view")
        val jfxListCell by cssclass("jfx-list-cell")
    }

    init {
        /*
         * window
         */
        jfxTitle {
            backgroundColor += c(primaryColor)
        }

        container {
            borderColor += box(c(primaryColor))
            borderWidth += box(0.px, 4.px, 4.px, 4.px)
        }

        /*
         * bot list
         */
        rootPane {
            leftPane {

                backgroundColor += c(primaryColor)
//                这个padding有bug，十分神奇
                padding = box(0.px, 4.px, 0.px, 0.px)
                spacing = 4.px

                jfxListView {

                    // placeholder
                    vBox {
                        spacing = 15.px

                        label {
                            textFill = c(fontColor)
                            fontWeight = FontWeight.BOLD
                        }

                        button {
                            textFill = c(fontColor)
                            fontWeight = FontWeight.BOLD
                            backgroundColor += c(secondaryColor, 0.8)
                            padding = box(2.px, 10.px)
                            cursor = Cursor.HAND
                        }
                    }

                    jfxListCell {
                        backgroundColor += c(100, 100, 100, 0.4)
                        backgroundRadius += box(5.px)

                        label {
                            textFill = c(fontColor)
                            fontWeight = FontWeight.BOLD
                        }

                        button {
                            opacity = 0.0
                            backgroundRadius += box(10.px)
                            backgroundColor += c(fontColor, 0.1)
                            cursor = Cursor.HAND

                            and(hover) {
                                opacity = 1.0
                            }
                        }
                    }
                }
            }
        }


        /*
         * tab pane
         */
        jfxTabHeader {
            backgroundColor += c(primaryColor)
        }

        jfxTabPane {

            // 日志列表样式
            vBox {
                padding = box(15.px)
                spacing = 15.px
            }

            myButtonBar {
                spacing = 15.px

                button {
                    backgroundColor += c(secondaryColor, 0.8)
                    padding = box(2.px, 10.px)
                    cursor = Cursor.HAND
                    textFill = c(fontColor)
                    fontSize = 12.px
                }
            }

            listView {
                // 字体在label里，大坑
                label {
                    fontSize = 13.px
                }

                listCell {

                    and(":WARNING") {
                        backgroundColor += c("FFFF00", 0.3) // Yellow
                    }

                    and(":ERROR") {
                        backgroundColor += c("FF0000", 0.3) // Red
                    }

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

                // 调整滚动条
                scrollBar {

                    backgroundColor += TRANSPARENT

                    // 隐藏水平滚动条
                    and(horizontal) {
                        prefHeight = 0.px
                        s(incrementArrow, decrementArrow) { backgroundColor += TRANSPARENT }
                    }

                    and(vertical) {
                        thumb {
                            backgroundColor += c(stressColor, 0.6)
                        }

                        track {
                            backgroundColor += TRANSPARENT
                        }
                    }
                }
            }
        }

        // 去除JFoenix默认样式
        tab {
            and(":closable") {
                borderWidth += box(0.px)
                borderInsets += box(6.px, 0.px)
            }

            closeButton {
                and(hover) { cursor = Cursor.HAND }
            }
        }
    }
}