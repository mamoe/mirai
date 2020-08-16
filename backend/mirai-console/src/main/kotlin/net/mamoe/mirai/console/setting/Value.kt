/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER", "unused", "NOTHING_TO_INLINE")

package net.mamoe.mirai.console.setting

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat
import net.mamoe.mirai.console.internal.setting.map
import net.mamoe.mirai.console.internal.setting.setValueBySerializer
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import kotlin.reflect.KProperty

/**
 * Represents a observable, immutable value wrapping.
 *
 * The value can be modified by delegation just like Kotlin's `var`, however it can also be done by the user, e.g. changing using the UI frontend.
 *
 * Some frequently used types are specially treated with performance enhancement by codegen.
 *
 * @see PrimitiveValue
 * @see CompositeValue
 */
public interface Value<T> {
    public var value: T
}

/**
 * Typically returned by [Setting.value] functions.
 */
public class SerializableValue<T>(
    private val delegate: Value<T>,
    /**
     * The serializer used to update and dump [delegate]
     */
    public override val serializer: KSerializer<Unit>
) : Value<T> by delegate, SerializerAwareValue<T> {
    public override fun toString(): String = delegate.toString()

    public companion object {
        @JvmStatic
        @JvmName("create")
        public fun <T> Value<T>.serializableValueWith(
            serializer: KSerializer<T>
        ): SerializableValue<T> {
            return SerializableValue(
                this,
                serializer.map(serializer = { this.value }, deserializer = { this.setValueBySerializer(it) })
            )
        }
    }
}

/**
 * @see SerializableValue
 */
public interface SerializerAwareValue<T> : Value<T> {
    public val serializer: KSerializer<Unit>

    public companion object {
        @JvmStatic
        @ConsoleExperimentalAPI("will be changed due to reconstruction of kotlinx.serialization")
        public fun <T> SerializerAwareValue<T>.serialize(format: StringFormat): String {
            return format.encodeToString(this.serializer, Unit)
        }

        @JvmStatic
        @ConsoleExperimentalAPI("will be changed due to reconstruction of kotlinx.serialization")
        public fun <T> SerializerAwareValue<T>.serialize(format: BinaryFormat): ByteArray {
            return format.encodeToByteArray(this.serializer, Unit)
        }

        @JvmStatic
        @ConsoleExperimentalAPI("will be changed due to reconstruction of kotlinx.serialization")
        public fun <T> SerializerAwareValue<T>.deserialize(format: StringFormat, value: String) {
            format.decodeFromString(this.serializer, value)
        }

        @JvmStatic
        @ConsoleExperimentalAPI("will be changed due to reconstruction of kotlinx.serialization")
        public fun <T> SerializerAwareValue<T>.deserialize(format: BinaryFormat, value: ByteArray) {
            format.decodeFromByteArray(this.serializer, value)
        }
    }
}

@JvmSynthetic
public inline operator fun <T> Value<T>.getValue(mySetting: Any?, property: KProperty<*>): T = value

@JvmSynthetic
public inline operator fun <T> Value<T>.setValue(mySetting: Any?, property: KProperty<*>, value: T) {
    this.value = value
}

/**
 * The serializer for a specific kind of [Value].
 */
public typealias ValueSerializer<T> = KSerializer<Value<T>>

/**
 * Represents a observable *primitive* value wrapping.
 *
 * 9 types that are considered *primitive*:
 * - Integers: [Byte], [Short], [Int], [Long]
 * - Floating: [Float], [Double]
 * - [Boolean]
 * - [Char], [String]
 *
 * Note: The values are actually *boxed* because of the generic type T.
 * *Primitive* indicates only it is one of the 9 types mentioned above.
 */
public interface PrimitiveValue<T> : Value<T>


//// region PrimitiveValues CODEGEN ////

/**
 * Represents a non-null [Byte] value.
 */
public interface ByteValue : PrimitiveValue<Byte>

/**
 * Represents a non-null [Short] value.
 */
public interface ShortValue : PrimitiveValue<Short>

/**
 * Represents a non-null [Int] value.
 */
public interface IntValue : PrimitiveValue<Int>

/**
 * Represents a non-null [Long] value.
 */
public interface LongValue : PrimitiveValue<Long>

/**
 * Represents a non-null [Float] value.
 */
public interface FloatValue : PrimitiveValue<Float>

/**
 * Represents a non-null [Double] value.
 */
public interface DoubleValue : PrimitiveValue<Double>

/**
 * Represents a non-null [Char] value.
 */
public interface CharValue : PrimitiveValue<Char>

/**
 * Represents a non-null [Boolean] value.
 */
public interface BooleanValue : PrimitiveValue<Boolean>

/**
 * Represents a non-null [String] value.
 */
public interface StringValue : PrimitiveValue<String>

//// endregion PrimitiveValues CODEGEN ////


@ConsoleExperimentalAPI
public interface CompositeValue<T> : Value<T>


/**
 * Superclass of [CompositeListValue], [PrimitiveListValue].
 */
public interface ListValue<E> : CompositeValue<List<E>>

/**
 * Elements can by anything, wrapped as [Value].
 * @param E is not primitive types.
 */
public interface CompositeListValue<E> : ListValue<E>

/**
 * Elements can only be primitives, not wrapped.
 * @param E is not primitive types.
 */
public interface PrimitiveListValue<E> : ListValue<E>


//// region PrimitiveListValue CODEGEN ////

public interface PrimitiveIntListValue : PrimitiveListValue<Int>
public interface PrimitiveLongListValue : PrimitiveListValue<Long>
// TODO + codegen

//// endregion PrimitiveListValue CODEGEN ////


/**
 * Superclass of [CompositeSetValue], [PrimitiveSetValue].
 */
public interface SetValue<E> : CompositeValue<Set<E>>

/**
 * Elements can by anything, wrapped as [Value].
 * @param E is not primitive types.
 */
public interface CompositeSetValue<E> : SetValue<E>

/**
 * Elements can only be primitives, not wrapped.
 * @param E is not primitive types.
 */
public interface PrimitiveSetValue<E> : SetValue<E>


//// region PrimitiveSetValue CODEGEN ////

public interface PrimitiveIntSetValue : PrimitiveSetValue<Int>
public interface PrimitiveLongSetValue : PrimitiveSetValue<Long>
// TODO + codegen

//// endregion PrimitiveSetValue CODEGEN ////


/**
 * Superclass of [CompositeMapValue], [PrimitiveMapValue].
 */
public interface MapValue<K, V> : CompositeValue<Map<K, V>>

public interface CompositeMapValue<K, V> : MapValue<K, V>

public interface PrimitiveMapValue<K, V> : MapValue<K, V>


//// region PrimitiveMapValue CODEGEN ////

public interface PrimitiveIntIntMapValue : PrimitiveMapValue<Int, Int>
public interface PrimitiveIntLongMapValue : PrimitiveMapValue<Int, Long>
// TODO + codegen

//// endregion PrimitiveSetValue CODEGEN ////









