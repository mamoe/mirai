/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress(
    "INVISIBLE_REFERENCE",
    "INVISIBLE_MEMBER",
    "EXPOSED_SUPER_CLASS",
    "NOTHING_TO_INLINE", "unused", "UNCHECKED_CAST"
)
@file:JvmName("PluginDataKt")

package net.mamoe.mirai.console.data

import kotlinx.serialization.KSerializer
import net.mamoe.mirai.console.data.java.JAutoSavePluginData
import net.mamoe.mirai.console.internal.data.*
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import kotlin.internal.LowPriorityInOverloadResolution
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation

/**
 * 一个插件内部的, 对用户隐藏的数据对象. 可包含对多个 [Value] 的值变更的跟踪.
 *
 * [PluginData] 不涉及有关数据的存储, 而是只维护数据结构: [属性节点列表][valueNodes].
 *
 * 有关存储方案, 请查看 [PluginDataStorage].
 *
 * **注意**: [PluginData] 总应该是单例的.
 *
 * ## [JvmPlugin] 的实现方案
 *
 * 要修改保存时的名称, 请参考 [ValueName]
 *
 * ### 使用 Kotlin
 *
 * 在 [JvmPlugin] 的典型实现方式:
 * ```
 * object PluginMain : KotlinPlugin()
 *
 * object MyPluginData : AutoSavePluginData() {
 *    var list: MutableList<String> by value(mutableListOf("a", "b")) // mutableListOf("a", "b") 是初始值, 可以省略
 *    val custom: Map<Long, CustomData> by value() // 使用 kotlinx-serialization 序列化的类型.
 *    var long: Long by value(0) // 允许 var
 *    var int by value(0) // 可以使用类型推断, 但更推荐使用 `var long: Long by value(0)` 这种定义方式.
 *
 *    // 将 MutableMap<Long, Long> 映射到 MutableMap<Bot, Long>.
 *    val botToLongMap: MutableMap<Bot, Long> by value<MutableMap<Long, Long>>().mapKeys(Bot::getInstance, Bot::id)
 * }
 *
 * @Serializable // kotlinx.serialization: https://github.com/Kotlin/kotlinx.serialization
 * data class CustomData(
 *     // ...
 * )
 * ```
 *
 * 使用时, 可以方便地直接调用, 如:
 * ```
 * val theList: MutableList<String> = AccountPluginData.list
 * ```
 *
 * 但也注意, 不要存储 `AccountPluginData.list`. 它可能受不到值跟踪. 若必要存储, 请使用 [PluginData.findBackingFieldValue]
 *
 * ### 使用 Java
 *
 * 参考 [JAutoSavePluginData]
 *
 * ## 非引用赋值
 *
 * 由于实现特殊, 赋值时不会写其引用. 即:
 * ```
 * val list = ArrayList<String>("A")
 * MyPluginData.list = list // 赋值给 PluginData 的委托属性是非引用的
 * println(MyPluginData.list) // "[A]"
 *
 * list.add("B")
 * println(list) // "[A, B]"
 * println(MyPluginData.list) // "[A]"  // !! 由于 `list` 的引用并未赋值给 `MyPluginData.list`.
 * ```
 *
 * 另一个更容易出错的示例:
 * ```
 * // MyPluginData.nestedMap: MutableMap<Long, List<Long>> by value()
 * val newList = MyPluginData.map.getOrPut(1, ::mutableListOf)
 * newList.add(1) // 不会添加到 MyPluginData.nestedMap 中, 因为 `mutableListOf` 创建的 MutableList 被非引用地添加进了 MyPluginData.nestedMap
 * ```
 *
 * 一个解决方案是对 [SerializerAwareValue] 做映射或相关修改. 如 [PluginDataExtensions].
 *
 * 要查看详细的解释，请查看 [docs/PluginData.md](https://github.com/mamoe/mirai-console/blob/master/docs/PluginData.md)
 *
 * @see JvmPlugin.reloadPluginData 通过 [JvmPlugin] 获取指定 [PluginData] 实例.
 * @see PluginDataStorage [PluginData] 存储仓库
 * @see PluginDataExtensions 相关 [SerializerAwareValue] 映射函数
 */
public interface PluginData {
    /**
     * 添加了追踪的 [ValueNode] 列表 (即使用 `by value()` 委托的属性), 即通过 `by value` 初始化的属性列表.
     *
     * 他们的修改会被跟踪, 并触发 [onValueChanged].
     *
     * @see provideDelegate
     * @see track
     */
    public val valueNodes: MutableList<ValueNode<*>>

    /**
     * 这个 [PluginData] 保存时使用的名称. 默认通过 [ValueName] 获取, 否则使用 [类全名][KClass.qualifiedName] (即 [Class.getCanonicalName])
     */
    @ConsoleExperimentalApi
    public val saveName: String
        get() {
            val clazz = this::class
            return clazz.findAnnotation<ValueName>()?.value
                ?: clazz.qualifiedName
                ?: throw IllegalArgumentException("Cannot find a serial name for ${this::class}")
        }

    /**
     * 由 [provideDelegate] 创建, 来自一个通过 `by value` 初始化的属性节点.
     */
    @ConsoleExperimentalApi
    public data class ValueNode<T>(
        /**
         * 节点名称.
         *
         * 如果属性带有 [ValueName], 则使用 [ValueName.value],
         * 否则使用 [属性名称][KProperty.name]
         */
        val valueName: String,
        /**
         * 属性值代理
         */
        val value: Value<out T>,
        /**
         * 注解列表
         */
        val annotations: List<Annotation>,
        /**
         * 属性值更新器
         */
        val updaterSerializer: KSerializer<Unit>
    )

    /**
     * 使用 `by value()` 时自动调用此方法, 添加对 [Value] 的值修改的跟踪, 并创建 [ValueNode] 加入 [valueNodes]
     */
    public operator fun <T : SerializerAwareValue<*>> T.provideDelegate(
        thisRef: Any?,
        property: KProperty<*>
    ): T = track(property.valueName, property.getAnnotationListForValueSerialization())

    /**
     * 供手动实现时值跟踪使用 (如 Java 用户). 一般 Kotlin 用户需使用 [provideDelegate]
     */
    public fun <T : SerializerAwareValue<*>> T.track(
        /**
         * 值名称.
         *
         * 如果属性带有 [ValueName], 则使用 [ValueName.value],
         * 否则使用 [属性名称][KProperty.name]
         *
         * @see [ValueNode.value]
         */
        valueName: String,
        annotations: List<Annotation>
    ): T

    /**
     * 所有 [valueNodes] 更新和保存序列化器. 仅供内部使用
     */
    public val updaterSerializer: KSerializer<Unit>

    /**
     * 当所属于这个 [PluginData] 的 [Value] 的 [值][Value.value] 被修改时被调用.
     */
    public fun onValueChanged(value: Value<*>)

    /**
     * 当这个 [PluginData] 被放入一个 [PluginDataStorage] 时调用
     */
    @ConsoleExperimentalApi
    public fun onInit(owner: PluginDataHolder, storage: PluginDataStorage)
}

/**
 * 获取这个 [KProperty] 委托的 [Value]
 *
 * 如, 对于
 * ```
 * object MyData : AutoSavePluginData(PluginMain) {
 *     val list: List<String> by value()
 * }
 *
 * val value: Value<List<String>> = MyData.findBackingFieldValue(MyData::list)
 * ```
 *
 * @see PluginData
 */
public fun <T> PluginData.findBackingFieldValue(property: KProperty<T>): Value<out T>? =
    findBackingFieldValue(property.valueName)

/**
 * 获取这个 [KProperty] 委托的 [Value]
 *
 * 如, 对于
 * ```
 * object MyData : AutoSavePluginData(PluginMain) {
 *     @ValueName("theList")
 *     val list: List<String> by value()
 *     val int: Int by value()
 * }
 *
 * val value: Value<List<String>> = MyData.findBackingFieldValue("theList") // 需使用 @ValueName 标注的名称
 * val intValue: Value<Int> = MyData.findBackingFieldValue("int")
 * ```
 *
 * @see PluginData
 */
public fun <T> PluginData.findBackingFieldValue(propertyValueName: String): Value<out T>? {
    return this.valueNodes.find { it.valueName == propertyValueName }?.value as Value<T>
}


/**
 * 获取这个 [KProperty] 委托的 [Value]
 *
 * 如, 对于
 * ```
 * object MyData : AutoSavePluginData(PluginMain) {
 *     val list: List<String> by value()
 * }
 *
 * val value: PluginData.ValueNode<List<String>> = MyData.findBackingFieldValueNode(MyData::list)
 * ```
 *
 * @see PluginData
 */
public fun <T> PluginData.findBackingFieldValueNode(property: KProperty<T>): PluginData.ValueNode<out T>? {
    return this.valueNodes.find { it == property } as PluginData.ValueNode<out T>?
}

// don't default = 0, cause ambiguity
//// region PluginData_value_primitives CODEGEN ////

/**
 * 创建一个 [Byte] 类型的 [Value], 并设置初始值为 [default]
 */
public fun PluginData.value(default: Byte): SerializerAwareValue<Byte> = valueImpl(default)

/**
 * 创建一个 [Short] 类型的 [Value], 并设置初始值为 [default]
 */
public fun PluginData.value(default: Short): SerializerAwareValue<Short> = valueImpl(default)

/**
 * 创建一个 [Int] 类型的 [Value], 并设置初始值为 [default]
 */
public fun PluginData.value(default: Int): SerializerAwareValue<Int> = valueImpl(default)

/**
 * 创建一个 [Long] 类型的 [Value], 并设置初始值为 [default]
 */
public fun PluginData.value(default: Long): SerializerAwareValue<Long> = valueImpl(default)

/**
 * 创建一个 [Float] 类型的 [Value], 并设置初始值为 [default]
 */
public fun PluginData.value(default: Float): SerializerAwareValue<Float> = valueImpl(default)

/**
 * 创建一个 [Double] 类型的 [Value], 并设置初始值为 [default]
 */
public fun PluginData.value(default: Double): SerializerAwareValue<Double> = valueImpl(default)

/**
 * 创建一个 [Char] 类型的 [Value], 并设置初始值为 [default]
 */
public fun PluginData.value(default: Char): SerializerAwareValue<Char> = valueImpl(default)

/**
 * 创建一个 [Boolean] 类型的 [Value], 并设置初始值为 [default]
 */
public fun PluginData.value(default: Boolean): SerializerAwareValue<Boolean> = valueImpl(default)

/**
 * 创建一个 [String] 类型的 [Value], 并设置初始值为 [default]
 */
public fun PluginData.value(default: String): SerializerAwareValue<String> = valueImpl(default)

//// endregion PluginData_value_primitives CODEGEN ////


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
public inline fun <reified T> PluginData.value(
    default: T,
    crossinline apply: T.() -> Unit = {}
): SerializerAwareValue<T> =
    valueFromKType(typeOf0<T>(), default).also { it.value.apply() }

/**
 * 通过具体化类型创建一个 [SerializerAwareValue].
 * @see valueFromKType 查看更多实现信息
 */
@LowPriorityInOverloadResolution
public inline fun <reified T> PluginData.value(apply: T.() -> Unit = {}): SerializerAwareValue<T> =
    valueImpl<T>(typeOf0<T>(), T::class).also { it.value.apply() }

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal fun <T> PluginData.valueImpl(type: KType, classifier: KClass<*>): SerializerAwareValue<T> =
    valueFromKType(type, classifier.run { objectInstance ?: createInstanceSmart() } as T)

/**
 * 通过一个特定的 [KType] 创建 [Value], 并设置初始值.
 *
 * 对于 [Map], [Set], [List] 等标准库类型, 这个函数会尝试构造 [LinkedHashMap], [LinkedHashSet], [ArrayList] 等相关类型.
 * 而对于自定义数据类型, 本函数只会反射获取 [objectInstance][KClass.objectInstance] 或使用*无参构造器*构造实例.
 *
 * @param T 具体化参数类型 T. 仅支持:
 * - 基础数据类型, [String]
 * - 标准库集合类型 ([List], [Map], [Set])
 * - 标准库数据类型 ([Map.Entry], [Pair], [Triple])
 * - 使用 [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) 的 [Serializable] 标记的类
 */
@Suppress("UNCHECKED_CAST")
@ConsoleExperimentalApi
public fun <T> PluginData.valueFromKType(type: KType, default: T): SerializerAwareValue<T> =
    (valueFromKTypeImpl(type) as SerializerAwareValue<Any?>).apply { this.value = default } as SerializerAwareValue<T>
