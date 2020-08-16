/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.graphical.model

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import net.mamoe.mirai.console.center.PluginCenter
import net.mamoe.mirai.console.plugins.PluginDescription
import tornadofx.getValue
import tornadofx.setValue

class PluginModel(
    val name: String,
    val version: String,
    val author: String,
    val description: String,
    var insight: PluginCenter.PluginInsight? = null
) : RecursiveTreeObject<PluginModel>() {
    constructor(plugin: PluginDescription) : this(plugin.name, plugin.version, plugin.author, plugin.info)

    val nameProperty = SimpleStringProperty(this, "nameProperty", name)
    val versionProperty = SimpleStringProperty(this, "versionProperty", version)
    val authorProperty = SimpleStringProperty(this, "authorProperty", author)
    val descriptionProperty = SimpleStringProperty(this, "descriptionProperty", description)

    val enabledProperty = SimpleBooleanProperty(this, "enabledProperty")
    var enabled by enabledProperty

    val expiredProperty = SimpleBooleanProperty(this, "expiredProperty", false)
    var expired by expiredProperty
}