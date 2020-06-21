/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER", "unused")

package net.mamoe.mirai.console.setting

import kotlinx.serialization.KSerializer
import net.mamoe.mirai.utils.MiraiExperimentalAPI

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
interface Value<T> {
    var value: T
}

/**
 * The serializer for a specific kind of [Value].
 */
typealias ValueSerializer<T> = KSerializer<Value<T>>

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
interface PrimitiveValue<T> : Value<T>


//// region PrimitiveValues CODEGEN ////

/**
 * Represents a non-null [Byte] value.
 */
interface ByteValue : PrimitiveValue<Byte>

/**
 * Represents a non-null [Short] value.
 */
interface ShortValue : PrimitiveValue<Short>

/**
 * Represents a non-null [Int] value.
 */
interface IntValue : PrimitiveValue<Int>

/**
 * Represents a non-null [Long] value.
 */
interface LongValue : PrimitiveValue<Long>

/**
 * Represents a non-null [Float] value.
 */
interface FloatValue : PrimitiveValue<Float>

/**
 * Represents a non-null [Double] value.
 */
interface DoubleValue : PrimitiveValue<Double>

/**
 * Represents a non-null [Char] value.
 */
interface CharValue : PrimitiveValue<Char>

/**
 * Represents a non-null [Boolean] value.
 */
interface BooleanValue : PrimitiveValue<Boolean>

/**
 * Represents a non-null [String] value.
 */
interface StringValue : PrimitiveValue<String>


//// endregion PrimitiveValues CODEGEN ////


@MiraiExperimentalAPI
interface CompositeValue<T> : Value<T>


/**
 * Superclass of [CompositeListValue], [PrimitiveListValue].
 */
interface ListValue<E> : CompositeValue<List<E>>

/**
 * Elements can by anything, wrapped as [Value].
 * @param E is not primitive types.
 */
interface CompositeListValue<E> : ListValue<E>

/**
 * Elements can only be primitives, not wrapped.
 * @param E is not primitive types.
 */
interface PrimitiveListValue<E> : ListValue<E>


//// region PrimitiveListValue CODEGEN ////

interface PrimitiveIntListValue : PrimitiveListValue<Int>
interface PrimitiveLongListValue : PrimitiveListValue<Long>
// TODO + codegen

//// endregion PrimitiveListValue CODEGEN ////


/**
 * Superclass of [CompositeSetValue], [PrimitiveSetValue].
 */
interface SetValue<E> : CompositeValue<Set<E>>

/**
 * Elements can by anything, wrapped as [Value].
 * @param E is not primitive types.
 */
interface CompositeSetValue<E> : SetValue<E>

/**
 * Elements can only be primitives, not wrapped.
 * @param E is not primitive types.
 */
interface PrimitiveSetValue<E> : SetValue<E>


//// region PrimitiveSetValue CODEGEN ////

interface PrimitiveIntSetValue : PrimitiveSetValue<Int>
interface PrimitiveLongSetValue : PrimitiveSetValue<Long>
// TODO + codegen

//// endregion PrimitiveSetValue CODEGEN ////


/**
 * Superclass of [CompositeMapValue], [PrimitiveMapValue].
 */
interface MapValue<K, V> : CompositeValue<Map<K, V>>

interface CompositeMapValue<K, V> : MapValue<K, V>

interface PrimitiveMapValue<K, V> : MapValue<K, V>


//// region PrimitiveMapValue CODEGEN ////

interface PrimitiveIntIntMapValue : PrimitiveMapValue<Int, Int>
interface PrimitiveIntLongMapValue : PrimitiveMapValue<Int, Long>
// TODO + codegen

//// endregion PrimitiveSetValue CODEGEN ////









