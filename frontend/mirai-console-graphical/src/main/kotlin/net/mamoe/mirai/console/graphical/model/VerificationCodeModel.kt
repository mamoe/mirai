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