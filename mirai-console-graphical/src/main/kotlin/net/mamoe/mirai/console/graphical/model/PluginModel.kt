package net.mamoe.mirai.console.graphical.model

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.getValue
import tornadofx.setValue

class PluginModel : RecursiveTreeObject<PluginModel>() {

    val nameProperty = SimpleStringProperty(this, "nameProperty")
    val name by nameProperty

    val descriptionProperty = SimpleStringProperty(this, "descriptionProperty")
    val description by descriptionProperty

    val enabledProperty = SimpleBooleanProperty(this, "enabledProperty")
    var enabled by enabledProperty
}