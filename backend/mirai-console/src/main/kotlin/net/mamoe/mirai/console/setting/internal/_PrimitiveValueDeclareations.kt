/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.setting.internal

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.builtins.serializer
import net.mamoe.mirai.console.setting.IntValue
import net.mamoe.mirai.console.setting.SerializerAwareValue


//// region PrimitiveValues CODEGEN ////

// TODO: 2020/6/21 CODEGEN

internal abstract class IntValueImpl : IntValue, SerializerAwareValue<Int>, KSerializer<Unit> {
    constructor()
    constructor(default: Int) {
        _value = default
    }

    private var _value: Int? = null

    final override var value: Int
        get() = _value ?: error("IntValue.value should be initialized before get.")
        set(v) {
            if (v != this._value) {
                this._value = v
                onChanged()
            }
        }

    protected abstract fun onChanged()

    final override val serializer: KSerializer<Unit> get() = this
    final override val descriptor: SerialDescriptor get() = BuiltInSerializerConstants.IntSerializerDescriptor
    final override fun serialize(encoder: Encoder, value: Unit) = Int.serializer().serialize(encoder, this.value)
    final override fun deserialize(decoder: Decoder) {
        value = Int.serializer().deserialize(decoder)
    }
}

//// endregion PrimitiveValues CODEGEN ////
