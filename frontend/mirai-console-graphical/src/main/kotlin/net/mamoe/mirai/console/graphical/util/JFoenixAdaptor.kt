/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.graphical.util

import com.jfoenix.controls.*
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.ListView
import tornadofx.SortedFilteredList
import tornadofx.attachTo
import tornadofx.bind

internal fun EventTarget.jfxTabPane(op: JFXTabPane.() -> Unit = {}) = JFXTabPane().attachTo(this, op)

internal fun EventTarget.jfxButton(text: String = "", graphic: Node? = null, op: JFXButton.() -> Unit = {}) =
    JFXButton(text).attachTo(this, op) {
        if (graphic != null) it.graphic = graphic
        it.buttonType = JFXButton.ButtonType.RAISED
    }

fun EventTarget.jfxTextfield(value: String? = null, op: JFXTextField.() -> Unit = {}) =
    JFXTextField().attachTo(this, op) {
        if (value != null) it.text = value
    }

fun EventTarget.jfxTextfield(property: ObservableValue<String>, op: JFXTextField.() -> Unit = {}) =
    jfxTextfield().apply {
        bind(property)
        op(this)
    }

fun EventTarget.jfxPasswordfield(value: String? = null, op: JFXPasswordField.() -> Unit = {}) =
    JFXPasswordField().attachTo(this, op) {
        if (value != null) it.text = value
    }

fun EventTarget.jfxPasswordfield(property: ObservableValue<String>, op: JFXPasswordField.() -> Unit = {}) =
    jfxPasswordfield().apply {
        bind(property)
        op(this)
    }

internal fun <T> EventTarget.jfxListView(values: ObservableList<T>? = null, op: ListView<T>.() -> Unit = {}) =
    JFXListView<T>().attachTo(this, op) {
        if (values != null) {
            if (values is SortedFilteredList<T>) values.bindTo(it)
            else it.items = values
        }
    }

fun <T : RecursiveTreeObject<T>?> EventTarget.jfxTreeTableView(
    items: ObservableList<T>? = null,
    op: JFXTreeTableView<T>.() -> Unit = {}
) = JFXTreeTableView<T>(RecursiveTreeItem(items, RecursiveTreeObject<T>::getChildren)).attachTo(this, op)