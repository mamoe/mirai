/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

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
