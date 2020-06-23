/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.setting.internal

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import net.mamoe.mirai.console.setting.SerializerAwareValue
import net.mamoe.mirai.console.setting.Setting


internal object BuiltInSerializerConstants {
//// region BuiltInSerializerConstants primitives CODEGEN ////

    @JvmStatic
    val ByteSerializerDescriptor = Byte.serializer().descriptor

    @JvmStatic
    val ShortSerializerDescriptor = Short.serializer().descriptor

    @JvmStatic
    val IntSerializerDescriptor = Int.serializer().descriptor

    @JvmStatic
    val LongSerializerDescriptor = Long.serializer().descriptor

    @JvmStatic
    val FloatSerializerDescriptor = Float.serializer().descriptor

    @JvmStatic
    val DoubleSerializerDescriptor = Double.serializer().descriptor

    @JvmStatic
    val CharSerializerDescriptor = Char.serializer().descriptor

    @JvmStatic
    val BooleanSerializerDescriptor = Boolean.serializer().descriptor

    @JvmStatic
    val StringSerializerDescriptor = String.serializer().descriptor


//// endregion BuiltInSerializerConstants primitives CODEGEN ////
}

//// region Setting.value primitives impl CODEGEN ////

// TODO: 2020/6/21 CODEGEN

internal fun Setting.valueImpl(default: Int): SerializerAwareValue<Int> {
    return object : IntValueImpl(default), SerializerAwareValue<Int>, KSerializer<Unit> {
        override fun onChanged() = this@valueImpl.onValueChanged(this)
    }
}

//// endregion Setting.value primitives impl CODEGEN ////
