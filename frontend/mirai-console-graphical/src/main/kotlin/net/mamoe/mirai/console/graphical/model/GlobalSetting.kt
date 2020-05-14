package net.mamoe.mirai.console.graphical.model

import javafx.beans.property.SimpleLongProperty
import tornadofx.ItemViewModel
import tornadofx.getValue
import tornadofx.setValue

class GlobalSetting {
    val maxLogNumProperty = SimpleLongProperty(4096)
    var maxLongNum: Long by maxLogNumProperty
}

class GlobalSettingModel(setting: GlobalSetting) : ItemViewModel<GlobalSetting>(setting) {
    constructor() : this(GlobalSetting())

    val maxLogNum = bind(GlobalSetting::maxLogNumProperty)
}
