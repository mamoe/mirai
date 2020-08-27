/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.data

import kotlinx.serialization.builtins.serializer
import net.mamoe.mirai.console.data.PluginData
import net.mamoe.mirai.console.data.ReferenceValue
import net.mamoe.mirai.console.data.SerializerAwareValue
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
internal fun <T : Any> PluginData.valueImplPrimitive(kClass: KClass<T>): SerializerAwareValue<T>? {
    return when (kClass) {
        //// region PluginData_valueImplPrimitive CODEGEN ////

        Byte::class -> byteValueImpl()
        Short::class -> shortValueImpl()
        Int::class -> intValueImpl()
        Long::class -> longValueImpl()
        Float::class -> floatValueImpl()
        Double::class -> doubleValueImpl()
        Char::class -> charValueImpl()
        Boolean::class -> booleanValueImpl()
        String::class -> stringValueImpl()

        //// endregion PluginData_valueImplPrimitive CODEGEN ////
        else -> error("Internal error: unexpected type passed: ${kClass.qualifiedName}")
    } as SerializerAwareValue<T>?
}


//// region PluginData_value_PrimitivesImpl CODEGEN ////

internal fun PluginData.valueImpl(default: Byte): SerializerAwareValue<Byte> {
    return object : ByteValueImpl(default) {
        override fun onChanged() = this@valueImpl.onValueChanged(this)
    }
}

internal fun PluginData.byteValueImpl(): SerializerAwareValue<Byte> {
    return object : ByteValueImpl() {
        override fun onChanged() = this@byteValueImpl.onValueChanged(this)
    }
}

internal fun PluginData.valueImpl(default: Short): SerializerAwareValue<Short> {
    return object : ShortValueImpl(default) {
        override fun onChanged() = this@valueImpl.onValueChanged(this)
    }
}

internal fun PluginData.shortValueImpl(): SerializerAwareValue<Short> {
    return object : ShortValueImpl() {
        override fun onChanged() = this@shortValueImpl.onValueChanged(this)
    }
}

internal fun PluginData.valueImpl(default: Int): SerializerAwareValue<Int> {
    return object : IntValueImpl(default) {
        override fun onChanged() = this@valueImpl.onValueChanged(this)
    }
}

internal fun PluginData.intValueImpl(): SerializerAwareValue<Int> {
    return object : IntValueImpl() {
        override fun onChanged() = this@intValueImpl.onValueChanged(this)
    }
}

internal fun PluginData.valueImpl(default: Long): SerializerAwareValue<Long> {
    return object : LongValueImpl(default) {
        override fun onChanged() = this@valueImpl.onValueChanged(this)
    }
}

internal fun PluginData.longValueImpl(): SerializerAwareValue<Long> {
    return object : LongValueImpl() {
        override fun onChanged() = this@longValueImpl.onValueChanged(this)
    }
}

internal fun PluginData.valueImpl(default: Float): SerializerAwareValue<Float> {
    return object : FloatValueImpl(default) {
        override fun onChanged() = this@valueImpl.onValueChanged(this)
    }
}

internal fun PluginData.floatValueImpl(): SerializerAwareValue<Float> {
    return object : FloatValueImpl() {
        override fun onChanged() = this@floatValueImpl.onValueChanged(this)
    }
}

internal fun PluginData.valueImpl(default: Double): SerializerAwareValue<Double> {
    return object : DoubleValueImpl(default) {
        override fun onChanged() = this@valueImpl.onValueChanged(this)
    }
}

internal fun PluginData.doubleValueImpl(): SerializerAwareValue<Double> {
    return object : DoubleValueImpl() {
        override fun onChanged() = this@doubleValueImpl.onValueChanged(this)
    }
}

internal fun PluginData.valueImpl(default: Char): SerializerAwareValue<Char> {
    return object : CharValueImpl(default) {
        override fun onChanged() = this@valueImpl.onValueChanged(this)
    }
}

internal fun PluginData.charValueImpl(): SerializerAwareValue<Char> {
    return object : CharValueImpl() {
        override fun onChanged() = this@charValueImpl.onValueChanged(this)
    }
}

internal fun PluginData.valueImpl(default: Boolean): SerializerAwareValue<Boolean> {
    return object : BooleanValueImpl(default) {
        override fun onChanged() = this@valueImpl.onValueChanged(this)
    }
}

internal fun PluginData.booleanValueImpl(): SerializerAwareValue<Boolean> {
    return object : BooleanValueImpl() {
        override fun onChanged() = this@booleanValueImpl.onValueChanged(this)
    }
}

internal fun PluginData.valueImpl(default: String): SerializerAwareValue<String> {
    return object : StringValueImpl(default) {
        override fun onChanged() = this@valueImpl.onValueChanged(this)
    }
}

internal fun PluginData.stringValueImpl(): SerializerAwareValue<String> {
    return object : StringValueImpl() {
        override fun onChanged() = this@stringValueImpl.onValueChanged(this)
    }
}

//// endregion PluginData_value_PrimitivesImpl CODEGEN ////

internal class LazyReferenceValueImpl<T> : ReferenceValue<T>, AbstractValueImpl<T>() {
    private var initialied: Boolean = false
    private var valueField: T? = null

    @Suppress("UNCHECKED_CAST")
    override var value: T
        get() {
            check(initialied) { "Internal error: LazyReferenceValueImpl.valueField isn't initialized" }
            return valueField as T
        }
        set(value) {
            initialied = true
            valueField = value
        }
}