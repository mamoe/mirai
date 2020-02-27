package net.mamoe.mirai.console.graphical.model

import javafx.beans.property.SimpleStringProperty
import tornadofx.ItemViewModel
import tornadofx.getValue
import tornadofx.setValue

class VerificationCode {
    val codeProperty = SimpleStringProperty("")
    var code: String by codeProperty
}

class VerificationCodeModel(code: VerificationCode) : ItemViewModel<VerificationCode>(code) {
    constructor() : this(VerificationCode())

    val code = bind(VerificationCode::codeProperty)
}