package net.mamoe.mirai.console.graphical.util

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXListView
import com.jfoenix.controls.JFXTabPane
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.TabPane
import tornadofx.SortedFilteredList
import tornadofx.attachTo

internal fun EventTarget.jfxTabPane(op: TabPane.() -> Unit = {}) = JFXTabPane().attachTo(this, op)

internal fun EventTarget.jfxButton(text: String = "", graphic: Node? = null, op: Button.() -> Unit = {}) =
    JFXButton(text).attachTo(this, op) {
        if (graphic != null) it.graphic = graphic
    }

internal fun <T> EventTarget.jfxListView(values: ObservableList<T>? = null, op: ListView<T>.() -> Unit = {}) =
    JFXListView<T>().attachTo(this, op) {
        if (values != null) {
            if (values is SortedFilteredList<T>) values.bindTo(it)
            else it.items = values
        }
    }
