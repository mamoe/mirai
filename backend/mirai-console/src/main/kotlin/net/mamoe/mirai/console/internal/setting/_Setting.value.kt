/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.setting

import kotlinx.serialization.builtins.serializer
import net.mamoe.mirai.console.setting.SerializerAwareValue
import net.mamoe.mirai.console.setting.Setting
import kotlin.reflect.KClass


internal object BuiltInSerializerConstants {
    //// region BuiltInSerializerConstantsPrimitives CODEGEN ////

    @JvmStatic
    internal val ByteSerializerDescriptor = Byte.serializer().descriptor

    @JvmStatic
    internal val ShortSerializerDescriptor = Short.serializer().descriptor

    @JvmStatic
    internal val IntSerializerDescriptor = Int.serializer().descriptor

    @JvmStatic
    internal val LongSerializerDescriptor = Long.serializer().descriptor

    @JvmStatic
    internal val FloatSerializerDescriptor = Float.serializer().descriptor

    @JvmStatic
    internal val DoubleSerializerDescriptor = Double.serializer().descriptor

    @JvmStatic
    internal val CharSerializerDescriptor = Char.serializer().descriptor

    @JvmStatic
    internal val BooleanSerializerDescriptor = Boolean.serializer().descriptor

    @JvmStatic
    internal val StringSerializerDescriptor = String.serializer().descriptor

    //// endregion BuiltInSerializerConstantsPrimitives CODEGEN ////
}

@Suppress("UNCHECKED_CAST")
internal fun <T : Any> Setting.valueImplPrimitive(kClass: KClass<T>): SerializerAwareValue<T>? {
    return when (kClass) {
        //// region Setting_valueImplPrimitive CODEGEN ////

        Byte::class -> byteValueImpl()
        Short::class -> shortValueImpl()
        Int::class -> intValueImpl()
        Long::class -> longValueImpl()
        Float::class -> floatValueImpl()
        Double::class -> doubleValueImpl()
        Char::class -> charValueImpl()
        Boolean::class -> booleanValueImpl()
        String::class -> stringValueImpl()

        //// endregion Setting_valueImplPrimitive CODEGEN ////
        else -> error("Internal error: unexpected type passed: ${kClass.qualifiedName}")
    } as SerializerAwareValue<T>?
}


//// region Setting_value_PrimitivesImpl CODEGEN ////

internal fun Setting.valueImpl(default: Byte): SerializerAwareValue<Byte> {
    return object : ByteValueImpl(default) {
        override fun onChanged() = this@valueImpl.onValueChanged(this)
    }
}
internal fun Setting.byteValueImpl(): SerializerAwareValue<Byte> {
    return object : ByteValueImpl() {
        override fun onChanged() = this@byteValueImpl.onValueChanged(this)
    }
}
internal fun Setting.valueImpl(default: Short): SerializerAwareValue<Short> {
    return object : ShortValueImpl(default) {
        override fun onChanged() = this@valueImpl.onValueChanged(this)
    }
}
internal fun Setting.shortValueImpl(): SerializerAwareValue<Short> {
    return object : ShortValueImpl() {
        override fun onChanged() = this@shortValueImpl.onValueChanged(this)
    }
}
internal fun Setting.valueImpl(default: Int): SerializerAwareValue<Int> {
    return object : IntValueImpl(default) {
        override fun onChanged() = this@valueImpl.onValueChanged(this)
    }
}
internal fun Setting.intValueImpl(): SerializerAwareValue<Int> {
    return object : IntValueImpl() {
        override fun onChanged() = this@intValueImpl.onValueChanged(this)
    }
}
internal fun Setting.valueImpl(default: Long): SerializerAwareValue<Long> {
    return object : LongValueImpl(default) {
        override fun onChanged() = this@valueImpl.onValueChanged(this)
    }
}
internal fun Setting.longValueImpl(): SerializerAwareValue<Long> {
    return object : LongValueImpl() {
        override fun onChanged() = this@longValueImpl.onValueChanged(this)
    }
}
internal fun Setting.valueImpl(default: Float): SerializerAwareValue<Float> {
    return object : FloatValueImpl(default) {
        override fun onChanged() = this@valueImpl.onValueChanged(this)
    }
}
internal fun Setting.floatValueImpl(): SerializerAwareValue<Float> {
    return object : FloatValueImpl() {
        override fun onChanged() = this@floatValueImpl.onValueChanged(this)
    }
}
internal fun Setting.valueImpl(default: Double): SerializerAwareValue<Double> {
    return object : DoubleValueImpl(default) {
        override fun onChanged() = this@valueImpl.onValueChanged(this)
    }
}
internal fun Setting.doubleValueImpl(): SerializerAwareValue<Double> {
    return object : DoubleValueImpl() {
        override fun onChanged() = this@doubleValueImpl.onValueChanged(this)
    }
}
internal fun Setting.valueImpl(default: Char): SerializerAwareValue<Char> {
    return object : CharValueImpl(default) {
        override fun onChanged() = this@valueImpl.onValueChanged(this)
    }
}
internal fun Setting.charValueImpl(): SerializerAwareValue<Char> {
    return object : CharValueImpl() {
        override fun onChanged() = this@charValueImpl.onValueChanged(this)
    }
}
internal fun Setting.valueImpl(default: Boolean): SerializerAwareValue<Boolean> {
    return object : BooleanValueImpl(default) {
        override fun onChanged() = this@valueImpl.onValueChanged(this)
    }
}
internal fun Setting.booleanValueImpl(): SerializerAwareValue<Boolean> {
    return object : BooleanValueImpl() {
        override fun onChanged() = this@booleanValueImpl.onValueChanged(this)
    }
}
internal fun Setting.valueImpl(default: String): SerializerAwareValue<String> {
    return object : StringValueImpl(default) {
        override fun onChanged() = this@valueImpl.onValueChanged(this)
    }
}
internal fun Setting.stringValueImpl(): SerializerAwareValue<String> {
    return object : StringValueImpl() {
        override fun onChanged() = this@stringValueImpl.onValueChanged(this)
    }
}

//// endregion Setting_value_PrimitivesImpl CODEGEN ////
