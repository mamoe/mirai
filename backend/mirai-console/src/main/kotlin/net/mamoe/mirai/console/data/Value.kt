/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER", "unused", "NOTHING_TO_INLINE", "INAPPLICABLE_JVM_NAME")

package net.mamoe.mirai.console.data

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat
import net.mamoe.mirai.console.internal.data.map
import net.mamoe.mirai.console.internal.data.setValueBySerializer
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * 表示一个值代理.
 *
 * [Value.value] 可以像 Kotlin 的 `var` 一样被修改, 然而它也可能被用户修改, 如通过 UI 前端, 或通过自动重载.
 *
 * 一些常用的基础类型实现由代码生成创建特性的优化.
 *
 * @see PluginData 容纳 [Value] 的数据对象
 *
 * @see PrimitiveValue 基础数据类型实现
 * @see CompositeValue 复合数据类型实现
 */
public interface Value<T> : ReadWriteProperty<Any?, T> {
    @get:JvmName("get")
    @set:JvmName("set")
    public var value: T

    @JvmSynthetic // avoid ambiguity with property `value`
    public override operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    @JvmSynthetic
    public override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

/**
 * 可被序列化的 [Value].
 */
public class SerializableValue<T>(
    @JvmField internal val delegate: Value<T>,
    /**
     * 用于更新和保存 [delegate] 的序列化器
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
                serializer.map(serializer = {
                    this.value
                }, deserializer = { this.setValueBySerializer(it) })
            )
        }
    }
}

/**
 * 带有显式 [序列化器][serializer] 的 [Value].
 *
 * @see SerializableValue 简单实现
 * @see PluginData.value 创建一个这样的 [SerializerAwareValue]
 */
public interface SerializerAwareValue<T> : Value<T> {
    /**
     * 用于更新 [value] 的序列化器. 在反序列化时不会创建新的 [T] 对象实例.
     *
     * - 序列化: `val text: String = Yaml.default.encodeToString(serializer, Unit)`
     * - 反序列化 (本质上是更新 [value], 不创建新的 [T] 实例): `Yaml.default.decodeFromString(serializer, text)`
     */
    public val serializer: KSerializer<Unit>

    public companion object {
        /**
         * 使用 [指定格式格式][format] 序列化一个 [SerializerAwareValue]
         */
        @JvmStatic
        public fun <T> SerializerAwareValue<T>.serialize(format: StringFormat): String {
            return format.encodeToString(this.serializer, Unit)
        }

        /**
         * 使用 [指定格式格式][format] 序列化一个 [SerializerAwareValue]
         */
        @JvmStatic
        public fun <T> SerializerAwareValue<T>.serialize(format: BinaryFormat): ByteArray {
            return format.encodeToByteArray(this.serializer, Unit)
        }

        /**
         * 使用 [指定格式格式][format] 反序列化 (更新) 一个 [SerializerAwareValue]
         */
        @JvmStatic
        public fun <T> SerializerAwareValue<T>.deserialize(format: StringFormat, string: String) {
            format.decodeFromString(this.serializer, string)
        }

        /**
         * 使用 [指定格式格式][format] 反序列化 (更新) 一个 [SerializerAwareValue]
         */
        @JvmStatic
        public fun <T> SerializerAwareValue<T>.deserialize(format: BinaryFormat, bytes: ByteArray) {
            format.decodeFromByteArray(this.serializer, bytes)
        }
    }
}

/**
 * 基础数据类型 [Value]
 *
 * 9 个被认为是 *基础类型* 的类型:
 * - 整数: [Byte], [Short], [Int], [Long]
 * - 浮点: [Float], [Double]
 * - [Boolean]
 * - [Char], [String]
 *
 * 注意: 目前这些类型都会被装箱, 由于泛型 T. 在将来可能会有优化处理.
 * *Primitive* 仅表示一个类型是上面 9 种类型之一.
 */
public interface PrimitiveValue<T> : Value<T>


//// region PrimitiveValues CODEGEN ////

/**
 * 表示一个不可空 [Byte] [Value].
 */
public interface ByteValue : PrimitiveValue<Byte>

/**
 * 表示一个不可空 [Short] [Value].
 */
public interface ShortValue : PrimitiveValue<Short>

/**
 * 表示一个不可空 [Int] [Value].
 */
public interface IntValue : PrimitiveValue<Int>

/**
 * 表示一个不可空 [Long] [Value].
 */
public interface LongValue : PrimitiveValue<Long>

/**
 * 表示一个不可空 [Float] [Value].
 */
public interface FloatValue : PrimitiveValue<Float>

/**
 * 表示一个不可空 [Double] [Value].
 */
public interface DoubleValue : PrimitiveValue<Double>

/**
 * 表示一个不可空 [Char] [Value].
 */
public interface CharValue : PrimitiveValue<Char>

/**
 * 表示一个不可空 [Boolean] [Value].
 */
public interface BooleanValue : PrimitiveValue<Boolean>

/**
 * 表示一个不可空 [String] [Value].
 */
public interface StringValue : PrimitiveValue<String>

//// endregion PrimitiveValues CODEGEN ////


/**
 * 复合数据类型实现
 */
@ConsoleExperimentalAPI
public interface CompositeValue<T> : Value<T>


/**
 * @see [CompositeListValue]
 * @see [PrimitiveListValue]
 */
public interface ListValue<E> : CompositeValue<List<E>>

/**
 * 复合数据类型的 [List]
 *
 * @param E 不是基础数据类型
 */
@ConsoleExperimentalAPI
public interface CompositeListValue<E> : ListValue<E>

/**
 * 针对基础类型优化的 [List]
 *
 * @param E 是基础类型
 */
@ConsoleExperimentalAPI
public interface PrimitiveListValue<E> : ListValue<E>


//// region PrimitiveListValue CODEGEN ////

@ConsoleExperimentalAPI
public interface PrimitiveIntListValue : PrimitiveListValue<Int>

@ConsoleExperimentalAPI
public interface PrimitiveLongListValue : PrimitiveListValue<Long>
// TODO + codegen

//// endregion PrimitiveListValue CODEGEN ////


/**
 * @see [CompositeSetValue]
 * @see [PrimitiveSetValue]
 */
@ConsoleExperimentalAPI
public interface SetValue<E> : CompositeValue<Set<E>>

/**
 * 复合数据类型 [Set]
 * @param E 是基础数据类型
 */
@ConsoleExperimentalAPI
public interface CompositeSetValue<E> : SetValue<E>

/**
 * 基础数据类型 [Set]
 * @param E 是基础数据类型
 */
@ConsoleExperimentalAPI
public interface PrimitiveSetValue<E> : SetValue<E>


//// region PrimitiveSetValue CODEGEN ////

@ConsoleExperimentalAPI
public interface PrimitiveIntSetValue : PrimitiveSetValue<Int>

@ConsoleExperimentalAPI
public interface PrimitiveLongSetValue : PrimitiveSetValue<Long>
// TODO + codegen

//// endregion PrimitiveSetValue CODEGEN ////


/**
 * @see [CompositeMapValue]
 * @see [PrimitiveMapValue]
 */
@ConsoleExperimentalAPI
public interface MapValue<K, V> : CompositeValue<Map<K, V>>

@ConsoleExperimentalAPI
public interface CompositeMapValue<K, V> : MapValue<K, V>

@ConsoleExperimentalAPI
public interface PrimitiveMapValue<K, V> : MapValue<K, V>


//// region PrimitiveMapValue CODEGEN ////

@ConsoleExperimentalAPI
public interface PrimitiveIntIntMapValue : PrimitiveMapValue<Int, Int>

@ConsoleExperimentalAPI
public interface PrimitiveIntLongMapValue : PrimitiveMapValue<Int, Long>
// TODO + codegen

//// endregion PrimitiveSetValue CODEGEN ////


@ConsoleExperimentalAPI
public interface ReferenceValue<T> : Value<T>