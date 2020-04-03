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