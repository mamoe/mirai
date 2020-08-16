/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.graphical.model

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.ItemViewModel
import tornadofx.getValue
import tornadofx.setValue

class VerificationCode(data: ByteArray = ByteArray(0)) {
    val codeProperty = SimpleStringProperty(null)
    var code: String? by codeProperty

    val dataProperty: SimpleObjectProperty<ByteArray> = SimpleObjectProperty()
    val data: ByteArray by dataProperty

    init {
        dataProperty.set(data)
    }
}

class VerificationCodeModel(code: VerificationCode) : ItemViewModel<VerificationCode>(code) {
    constructor() : this(VerificationCode())

    val code = bind(VerificationCode::codeProperty)
    val data = bind(VerificationCode::dataProperty)
}