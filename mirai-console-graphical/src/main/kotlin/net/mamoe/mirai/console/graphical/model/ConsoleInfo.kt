package net.mamoe.mirai.console.graphical.model

import javafx.beans.property.SimpleStringProperty
import tornadofx.getValue
import tornadofx.setValue

class ConsoleInfo {

    val consoleVersionProperty = SimpleStringProperty()
    var consoleVersion by consoleVersionProperty

    val consoleBuildProperty = SimpleStringProperty()
    var consoleBuild by consoleBuildProperty

    val coreVersionProperty = SimpleStringProperty()
    var coreVersion by coreVersionProperty
}