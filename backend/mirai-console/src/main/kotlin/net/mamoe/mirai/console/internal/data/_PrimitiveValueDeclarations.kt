/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.mamoe.mirai.console.data.*

/**
 * The super class to all ValueImpl
 */
internal abstract class AbstractValueImpl<T> : Value<T> {
    open fun setValueBySerializer(value: T) {
        this.value = value
    }
}

internal fun <T> Value<T>.setValueBySerializer(value: T) =
    (this.castOrInternalError<AbstractValueImpl<T>>()).setValueBySerializer(value)

//// region PrimitiveValuesImpl CODEGEN ////

internal abstract class ByteValueImpl : ByteValue, SerializerAwareValue<Byte>, KSerializer<Unit>,
    AbstractValueImpl<Byte> {
    constructor()
    constructor(default: Byte) {
        _value = default
    }

    private var _value: Byte? = null

    final override var value: Byte
        get() = _value ?: error("ByteValue.value should be initialized before get.")
        set(v) {
            if (v != this._value) {
                if (this._value == null) {
                    this._value = v
                } else {
                    this._value = v
                    onChanged()
                }
            }
        }

    protected abstract fun onChanged()

    final override val serializer: KSerializer<Unit> get() = this
    final override val descriptor: SerialDescriptor get() = BuiltInSerializerConstants.ByteSerializerDescriptor
    final override fun serialize(encoder: Encoder, value: Unit) = Byte.serializer().serialize(encoder, this.value)
    final override fun deserialize(decoder: Decoder) = setValueBySerializer(Byte.serializer().deserialize(decoder))
    override fun toString(): String = _value?.toString() ?: "ByteValue.value not yet initialized."
    override fun equals(other: Any?): Boolean =
        other is ByteValueImpl && other::class.java == this::class.java && other._value == this._value

    override fun hashCode(): Int {
        val value = _value
        return if (value == null) 1
        else value.hashCode() * 31
    }
}

internal abstract class ShortValueImpl : ShortValue, SerializerAwareValue<Short>, KSerializer<Unit>,
    AbstractValueImpl<Short> {
    constructor()
    constructor(default: Short) {
        _value = default
    }

    private var _value: Short? = null

    final override var value: Short
        get() = _value ?: error("ShortValue.value should be initialized before get.")
        set(v) {
            if (v != this._value) {
                if (this._value == null) {
                    this._value = v
                } else {
                    this._value = v
                    onChanged()
                }
            }
        }

    protected abstract fun onChanged()

    final override val serializer: KSerializer<Unit> get() = this
    final override val descriptor: SerialDescriptor get() = BuiltInSerializerConstants.ShortSerializerDescriptor
    final override fun serialize(encoder: Encoder, value: Unit) = Short.serializer().serialize(encoder, this.value)
    final override fun deserialize(decoder: Decoder) = setValueBySerializer(Short.serializer().deserialize(decoder))
    override fun toString(): String = _value?.toString() ?: "ShortValue.value not yet initialized."
    override fun equals(other: Any?): Boolean =
        other is ShortValueImpl && other::class.java == this::class.java && other._value == this._value

    override fun hashCode(): Int {
        val value = _value
        return if (value == null) 1
        else value.hashCode() * 31
    }
}
internal abstract class IntValueImpl : IntValue, SerializerAwareValue<Int>, KSerializer<Unit>, AbstractValueImpl<Int> {
    constructor()
    constructor(default: Int) {
        _value = default
    }

    private var _value: Int? = null

    final override var value: Int
        get() = _value ?: error("IntValue.value should be initialized before get.")
        set(v) {
            if (v != this._value) {
                if (this._value == null) {
                    this._value = v
                } else {
                    this._value = v
                    onChanged()
                }
            }
        }

    protected abstract fun onChanged()

    final override val serializer: KSerializer<Unit> get() = this
    final override val descriptor: SerialDescriptor get() = BuiltInSerializerConstants.IntSerializerDescriptor
    final override fun serialize(encoder: Encoder, value: Unit) = Int.serializer().serialize(encoder, this.value)
    final override fun deserialize(decoder: Decoder) = setValueBySerializer(Int.serializer().deserialize(decoder))
    override fun toString(): String = _value?.toString() ?: "IntValue.value not yet initialized."
    override fun equals(other: Any?): Boolean =
        other is IntValueImpl && other::class.java == this::class.java && other._value == this._value

    override fun hashCode(): Int {
        val value = _value
        return if (value == null) 1
        else value.hashCode() * 31
    }
}

internal abstract class LongValueImpl : LongValue, SerializerAwareValue<Long>, KSerializer<Unit>,
    AbstractValueImpl<Long> {
    constructor()
    constructor(default: Long) {
        _value = default
    }

    private var _value: Long? = null

    final override var value: Long
        get() = _value ?: error("LongValue.value should be initialized before get.")
        set(v) {
            if (v != this._value) {
                if (this._value == null) {
                    this._value = v
                } else {
                    this._value = v
                    onChanged()
                }
            }
        }

    protected abstract fun onChanged()

    final override val serializer: KSerializer<Unit> get() = this
    final override val descriptor: SerialDescriptor get() = BuiltInSerializerConstants.LongSerializerDescriptor
    final override fun serialize(encoder: Encoder, value: Unit) = Long.serializer().serialize(encoder, this.value)
    final override fun deserialize(decoder: Decoder) = setValueBySerializer(Long.serializer().deserialize(decoder))
    override fun toString(): String = _value?.toString() ?: "LongValue.value not yet initialized."
    override fun equals(other: Any?): Boolean =
        other is LongValueImpl && other::class.java == this::class.java && other._value == this._value

    override fun hashCode(): Int {
        val value = _value
        return if (value == null) 1
        else value.hashCode() * 31
    }
}

internal abstract class FloatValueImpl : FloatValue, SerializerAwareValue<Float>, KSerializer<Unit>,
    AbstractValueImpl<Float> {
    constructor()
    constructor(default: Float) {
        _value = default
    }

    private var _value: Float? = null

    final override var value: Float
        get() = _value ?: error("FloatValue.value should be initialized before get.")
        set(v) {
            if (v != this._value) {
                if (this._value == null) {
                    this._value = v
                } else {
                    this._value = v
                    onChanged()
                }
            }
        }

    protected abstract fun onChanged()

    final override val serializer: KSerializer<Unit> get() = this
    final override val descriptor: SerialDescriptor get() = BuiltInSerializerConstants.FloatSerializerDescriptor
    final override fun serialize(encoder: Encoder, value: Unit) = Float.serializer().serialize(encoder, this.value)
    final override fun deserialize(decoder: Decoder) = setValueBySerializer(Float.serializer().deserialize(decoder))
    override fun toString(): String = _value?.toString() ?: "FloatValue.value not yet initialized."
    override fun equals(other: Any?): Boolean =
        other is FloatValueImpl && other::class.java == this::class.java && other._value == this._value

    override fun hashCode(): Int {
        val value = _value
        return if (value == null) 1
        else value.hashCode() * 31
    }
}

internal abstract class DoubleValueImpl : DoubleValue, SerializerAwareValue<Double>, KSerializer<Unit>,
    AbstractValueImpl<Double> {
    constructor()
    constructor(default: Double) {
        _value = default
    }

    private var _value: Double? = null

    final override var value: Double
        get() = _value ?: error("DoubleValue.value should be initialized before get.")
        set(v) {
            if (v != this._value) {
                if (this._value == null) {
                    this._value = v
                } else {
                    this._value = v
                    onChanged()
                }
            }
        }

    protected abstract fun onChanged()

    final override val serializer: KSerializer<Unit> get() = this
    final override val descriptor: SerialDescriptor get() = BuiltInSerializerConstants.DoubleSerializerDescriptor
    final override fun serialize(encoder: Encoder, value: Unit) = Double.serializer().serialize(encoder, this.value)
    final override fun deserialize(decoder: Decoder) = setValueBySerializer(Double.serializer().deserialize(decoder))
    override fun toString(): String = _value?.toString() ?: "DoubleValue.value not yet initialized."
    override fun equals(other: Any?): Boolean =
        other is DoubleValueImpl && other::class.java == this::class.java && other._value == this._value

    override fun hashCode(): Int {
        val value = _value
        return if (value == null) 1
        else value.hashCode() * 31
    }
}

internal abstract class CharValueImpl : CharValue, SerializerAwareValue<Char>, KSerializer<Unit>,
    AbstractValueImpl<Char> {
    constructor()
    constructor(default: Char) {
        _value = default
    }

    private var _value: Char? = null

    final override var value: Char
        get() = _value ?: error("CharValue.value should be initialized before get.")
        set(v) {
            if (v != this._value) {
                if (this._value == null) {
                    this._value = v
                } else {
                    this._value = v
                    onChanged()
                }
            }
        }

    protected abstract fun onChanged()

    final override val serializer: KSerializer<Unit> get() = this
    final override val descriptor: SerialDescriptor get() = BuiltInSerializerConstants.CharSerializerDescriptor
    final override fun serialize(encoder: Encoder, value: Unit) = Char.serializer().serialize(encoder, this.value)
    final override fun deserialize(decoder: Decoder) = setValueBySerializer(Char.serializer().deserialize(decoder))
    override fun toString(): String = _value?.toString() ?: "CharValue.value not yet initialized."
    override fun equals(other: Any?): Boolean =
        other is CharValueImpl && other::class.java == this::class.java && other._value == this._value

    override fun hashCode(): Int {
        val value = _value
        return if (value == null) 1
        else value.hashCode() * 31
    }
}

internal abstract class BooleanValueImpl : BooleanValue, SerializerAwareValue<Boolean>, KSerializer<Unit>,
    AbstractValueImpl<Boolean> {
    constructor()
    constructor(default: Boolean) {
        _value = default
    }

    private var _value: Boolean? = null

    final override var value: Boolean
        get() = _value ?: error("BooleanValue.value should be initialized before get.")
        set(v) {
            if (v != this._value) {
                if (this._value == null) {
                    this._value = v
                } else {
                    this._value = v
                    onChanged()
                }
            }
        }

    protected abstract fun onChanged()

    final override val serializer: KSerializer<Unit> get() = this
    final override val descriptor: SerialDescriptor get() = BuiltInSerializerConstants.BooleanSerializerDescriptor
    final override fun serialize(encoder: Encoder, value: Unit) = Boolean.serializer().serialize(encoder, this.value)
    final override fun deserialize(decoder: Decoder) = setValueBySerializer(Boolean.serializer().deserialize(decoder))
    override fun toString(): String = _value?.toString() ?: "BooleanValue.value not yet initialized."
    override fun equals(other: Any?): Boolean =
        other is BooleanValueImpl && other::class.java == this::class.java && other._value == this._value

    override fun hashCode(): Int {
        val value = _value
        return if (value == null) 1
        else value.hashCode() * 31
    }
}

internal abstract class StringValueImpl : StringValue, SerializerAwareValue<String>, KSerializer<Unit>,
    AbstractValueImpl<String> {
    constructor()
    constructor(default: String) {
        _value = default
    }

    private var _value: String? = null

    final override var value: String
        get() = _value ?: error("StringValue.value should be initialized before get.")
        set(v) {
            if (v != this._value) {
                if (this._value == null) {
                    this._value = v
                } else {
                    this._value = v
                    onChanged()
                }
            }
        }

    protected abstract fun onChanged()

    final override val serializer: KSerializer<Unit> get() = this
    final override val descriptor: SerialDescriptor get() = BuiltInSerializerConstants.StringSerializerDescriptor
    final override fun serialize(encoder: Encoder, value: Unit) = String.serializer().serialize(encoder, this.value)
    final override fun deserialize(decoder: Decoder) = setValueBySerializer(String.serializer().deserialize(decoder))
    override fun toString(): String = _value ?: "StringValue.value not yet initialized."
    override fun equals(other: Any?): Boolean =
        other is StringValueImpl && other::class.java == this::class.java && other._value == this._value

    override fun hashCode(): Int {
        val value = _value
        return if (value == null) 1
        else value.hashCode() * 31
    }
}

//// endregion PrimitiveValuesImpl CODEGEN ////
