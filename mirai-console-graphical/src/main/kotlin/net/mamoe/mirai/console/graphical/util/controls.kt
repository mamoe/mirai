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