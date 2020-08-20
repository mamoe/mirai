/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER", "EXPOSED_SUPER_CLASS")

package net.mamoe.mirai.console.setting

import kotlinx.serialization.KSerializer
import net.mamoe.mirai.console.internal.setting.*
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.loadSetting
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import kotlin.internal.LowPriorityInOverloadResolution
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType


/**
 * 序列化之后的名称.
 *
 * 例:
 * ```
 * @SerialName("accounts")
 * object AccountSettings : Setting by ... {
 *    @SerialName("info")
 *    val map: Map<String, String> by value("a" to "b")
 * }
 * ```
 *
 * 将被保存为配置 (YAML 作为示例):
 * ```yaml
 * accounts:
 *   info:
 *     a: b
 * ```
 */
public typealias SerialName = kotlinx.serialization.SerialName

/**
 * [Setting] 的默认实现. 支持使用 `by value()` 等委托方法创建 [Value] 并跟踪其改动.
 *
 * @see Setting
 */
public abstract class AbstractSetting : Setting, SettingImpl() {
    /**
     * 添加了追踪的 [ValueNode] 列表, 即通过 `by value` 初始化的属性列表.
     *
     * 他们的修改会被跟踪, 并触发 [onValueChanged].
     *
     * @see provideDelegate
     */
    public override val valueNodes: MutableList<ValueNode<*>> = mutableListOf()

    /**
     * 由 [provideDelegate] 创建, 来自一个通过 `by value` 初始化的属性.
     */
    public data class ValueNode<T>(
        val serialName: String,
        val value: Value<T>,
        @ConsoleExperimentalAPI
        val updaterSerializer: KSerializer<Unit>
    )

    /**
     * 使用 `by` 时自动调用此方法, 添加对 [Value] 的值修改的跟踪.
     *
     * 将会创建一个 [ValueNode] 并添加到 [valueNodes]
     */
    public final override operator fun <T> SerializerAwareValue<T>.provideDelegate(
        thisRef: Any?,
        property: KProperty<*>
    ): SerializerAwareValue<T> {
        val name = property.serialName
        valueNodes.add(ValueNode(name, this, this.serializer))
        return this
    }

    /**
     * 值更新序列化器. 仅供内部使用.
     */
    @ConsoleExperimentalAPI
    public final override val updaterSerializer: KSerializer<Unit>
        get() = super.updaterSerializer

    /**
     * 当所属于这个 [Setting] 的 [Value] 的 [值][Value.value] 被修改时被调用.
     */
    public abstract override fun onValueChanged(value: Value<*>)
}

/**
 * 一个配置对象. 可包含对多个 [Value] 的值变更的跟踪.
 *
 * 在 [JvmPlugin] 的典型实现方式:
 * ```
 * object PluginMain : KotlinPlugin()
 *
 * object AccountSettings : Setting by PluginMain.loadSetting() {
 *    val map: Map<String, String> by value("a" to "b")
 * }
 * ```
 *
 * @see JvmPlugin.loadSetting 通过 [JvmPlugin] 获取指定 [Setting] 实例.
 */
public interface Setting : ExperimentalSettingExtensions {
    /**
     * 使用 `by` 时自动调用此方法, 添加对 [Value] 的值修改的跟踪.
     */
    public operator fun <T> SerializerAwareValue<T>.provideDelegate(
        thisRef: Any?,
        property: KProperty<*>
    ): SerializerAwareValue<T>

    /**
     * 值更新序列化器. 仅供内部使用
     */
    @ConsoleExperimentalAPI
    public val updaterSerializer: KSerializer<Unit>

    /**
     * 当所属于这个 [Setting] 的 [Value] 的 [值][Value.value] 被修改时被调用.
     */
    public fun onValueChanged(value: Value<*>)

    /**
     * 当这个 [Setting] 被放入一个 [SettingStorage] 时调用
     */
    public fun setStorage(storage: SettingStorage)
}

@ConsoleExperimentalAPI("")
public interface ExperimentalSettingExtensions {
    public fun <E, V, K> MutableMap<E, V>.shadowMap(
        eToK: (E) -> K,
        kToE: (K) -> E
    ): MutableMap<K, V> {
        return this.shadowMap(
            kTransform = eToK,
            kTransformBack = kToE,
            vTransform = { it },
            vTransformBack = { it }
        )
    }
}

//// region Setting_value_primitives CODEGEN ////

public fun Setting.value(default: Byte): SerializerAwareValue<Byte> = valueImpl(default)
public fun Setting.value(default: Short): SerializerAwareValue<Short> = valueImpl(default)
public fun Setting.value(default: Int): SerializerAwareValue<Int> = valueImpl(default)
public fun Setting.value(default: Long): SerializerAwareValue<Long> = valueImpl(default)
public fun Setting.value(default: Float): SerializerAwareValue<Float> = valueImpl(default)
public fun Setting.value(default: Double): SerializerAwareValue<Double> = valueImpl(default)
public fun Setting.value(default: Char): SerializerAwareValue<Char> = valueImpl(default)
public fun Setting.value(default: Boolean): SerializerAwareValue<Boolean> = valueImpl(default)
public fun Setting.value(default: String): SerializerAwareValue<String> = valueImpl(default)

//// endregion Setting_value_primitives CODEGEN ////


/**
 * 通过具体化类型创建一个 [SerializerAwareValue], 并设置初始值.
 *
 * @param T 具体化参数类型 T. 仅支持:
 * - 基础数据类型
 * - 标准库集合类型 ([List], [Map], [Set])
 * - 标准库数据类型 ([Map.Entry], [Pair], [Triple])
 * - 和使用 [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) 的 [Serializable] 标记的
 */
@Suppress("UNCHECKED_CAST")
@LowPriorityInOverloadResolution
public inline fun <reified T> Setting.value(default: T): SerializerAwareValue<T> = valueFromKType(typeOf0<T>(), default)

/**
 * 通过具体化类型创建一个 [SerializerAwareValue].
 * @see valueFromKType 查看更多实现信息
 */
@LowPriorityInOverloadResolution
public inline fun <reified T> Setting.value(): SerializerAwareValue<T> =
    value(T::class.run { objectInstance ?: createInstanceSmart() } as T)

/**
 * 通过一个特定的 [KType] 创建 [Value], 并设置初始值.
 *
 * 对于 [List], [Map], [Set] 等标准库类型, 这个函数会尝试构造 [LinkedHashMap] 等相关类型.
 * 而对于自定义数据类型, 本函数只会反射获取 [objectInstance][KClass.objectInstance] 或使用无参构造器构造实例.
 *
 * @param T 具体化参数类型 T. 仅支持:
 * - 基础数据类型
 * - 标准库集合类型 ([List], [Map], [Set])
 * - 标准库数据类型 ([Map.Entry], [Pair], [Triple])
 * - 和使用 [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) 的 [Serializable] 标记的
 */
@Suppress("UNCHECKED_CAST")
@ConsoleExperimentalAPI
public fun <T> Setting.valueFromKType(type: KType, default: T): SerializerAwareValue<T> =
    (valueFromKTypeImpl(type) as SerializerAwareValue<Any?>).apply { this.value = default } as SerializerAwareValue<T>

// TODO: 2020/6/24 Introduce class TypeToken for compound types for Java.