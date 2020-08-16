/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.graphical.util

import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import tornadofx.addClass
import tornadofx.hbox

fun EventTarget.myButtonBar(alignment: Pos = Pos.BASELINE_LEFT, op: HBox.() -> Unit = {}) = hbox {
    addClass("my-button-bar")
    this.alignment = alignment
    op()
}