/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER", "EXPOSED_SUPER_CLASS")

package net.mamoe.mirai.console.setting

import kotlinx.serialization.KSerializer
import net.mamoe.mirai.console.setting.internal.*
import kotlin.internal.LowPriorityInOverloadResolution
import kotlin.reflect.KProperty
import kotlin.reflect.KType


/**
 * 序列化之后的名称.
 *
 * 例:
 * ```
 * class MySetting : Setting() {
 *
 * }
 * ```
 */
// TODO: 2020/6/26 document
typealias SerialName = kotlinx.serialization.SerialName

// TODO: 2020/6/26 document
abstract class AbstractSetting : Setting, SettingImpl() {
    final override operator fun <T> SerializerAwareValue<T>.provideDelegate(
        thisRef: Any?,
        property: KProperty<*>
    ): SerializerAwareValue<T> {
        val name = property.serialName
        valueNodes.add(Node(name, this, this.serializer))
        return this
    }

    final override val updaterSerializer: KSerializer<Unit> get() = super.updaterSerializer
}

// TODO: 2020/6/26 document
interface Setting {
    // TODO: 2020/6/26 document
    operator fun <T> SerializerAwareValue<T>.provideDelegate(
        thisRef: Any?,
        property: KProperty<*>
    ): SerializerAwareValue<T>

    // TODO: 2020/6/26 document
    val updaterSerializer: KSerializer<Unit>
}

//// region Setting_value_primitives CODEGEN ////

fun Setting.value(default: Byte): SerializerAwareValue<Byte> = valueImpl(default)
fun Setting.value(default: Short): SerializerAwareValue<Short> = valueImpl(default)
fun Setting.value(default: Int): SerializerAwareValue<Int> = valueImpl(default)
fun Setting.value(default: Long): SerializerAwareValue<Long> = valueImpl(default)
fun Setting.value(default: Float): SerializerAwareValue<Float> = valueImpl(default)
fun Setting.value(default: Double): SerializerAwareValue<Double> = valueImpl(default)
fun Setting.value(default: Char): SerializerAwareValue<Char> = valueImpl(default)
fun Setting.value(default: Boolean): SerializerAwareValue<Boolean> = valueImpl(default)
fun Setting.value(default: String): SerializerAwareValue<String> = valueImpl(default)

//// endregion Setting_value_primitives CODEGEN ////


/**
 * Creates a [Value] with reified type, and set default value.
 *
 * @param T reified param type T.
 * Supports only primitives, Kotlin built-in collections,
 * and classes that are serializable with Kotlinx.serialization
 * (typically annotated with [kotlinx.serialization.Serializable])
 */
@Suppress("UNCHECKED_CAST")
@LowPriorityInOverloadResolution
inline fun <reified T> Setting.value(default: T): SerializerAwareValue<T> = valueFromKType(typeOf0<T>(), default)

/**
 * Creates a [Value] with reified type, and set default value by reflection to its no-arg public constructor.
 *
 * @param T reified param type T.
 * Supports only primitives, Kotlin built-in collections,
 * and classes that are serializable with Kotlinx.serialization
 * (typically annotated with [kotlinx.serialization.Serializable])
 */
@LowPriorityInOverloadResolution
inline fun <reified T> Setting.value(): SerializerAwareValue<T> = value(T::class.createInstance() as T)

/**
 * Creates a [Value] with specified [KType], and set default value.
 */
@Suppress("UNCHECKED_CAST")
fun <T> Setting.valueFromKType(type: KType, default: T): SerializerAwareValue<T> =
    (valueFromKTypeImpl(type) as SerializerAwareValue<Any?>).apply { this.value = default } as SerializerAwareValue<T>

// TODO: 2020/6/24 Introduce class TypeToken for compound types for Java.