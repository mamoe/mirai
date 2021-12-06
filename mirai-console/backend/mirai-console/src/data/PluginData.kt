/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
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
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.serializersModuleOf
import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.RESTRICTED_NO_ARG_CONSTRUCTOR
import net.mamoe.mirai.console.data.java.JAutoSavePluginData
import net.mamoe.mirai.console.internal.data.createInstanceSmart
import net.mamoe.mirai.console.internal.data.typeOf0
import net.mamoe.mirai.console.internal.data.valueFromKTypeImpl
import net.mamoe.mirai.console.internal.data.valueImpl
import net.mamoe.mirai.console.plugin.jvm.AbstractJvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.reloadPluginData
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import kotlin.internal.LowPriorityInOverloadResolution
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * 一个插件内部的, 对用户隐藏的数据对象. 可包含对多个 [Value] 的值变更的跟踪. 典型的实现为 [AbstractPluginData].
 *
 * [AbstractPluginData] 不涉及有关数据的存储, 而是只维护数据结构: [属性节点列表][AbstractPluginData.valueNodes].
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
 * 但也注意, 不要存储 `AccountPluginData.list`. 它可能受不到值跟踪. 若必要存储, 请使用 [AbstractPluginData.findBackingFieldValue]
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
 * newList.add(1) // 不会添加到 MyPluginData.nestedMap 中, 因为 `mutableListOf` 创建的 MutableList 被非引用 (浅拷贝) 地添加进了 MyPluginData.nestedMap
 * ```
 *
 * 一个解决方案是对 [SerializerAwareValue] 做映射或相关修改. 如 [PluginDataExtensions].
 *
 * 要查看详细的解释，请查看 [docs/PluginData.md](https://github.com/mamoe/mirai-console/blob/master/docs/PluginData.md)
 *
 * ## 实现注意
 * 此类型处于实验性阶段. 使用其中定义的属性和函数是安全的, 但将来可能会新增成员抽象函数.
 *
 * @see AbstractJvmPlugin.reloadPluginData 通过 [JvmPlugin] 获取指定 [PluginData] 实例.
 * @see PluginDataStorage [PluginData] 存储仓库
 * @see PluginDataExtensions 相关 [SerializerAwareValue] 映射函数
 */
public interface PluginData {
    /**
     * 这个 [PluginData] 保存时使用的名称.
     */
    @ConsoleExperimentalApi
    public val saveName: String

    @ConsoleExperimentalApi
    public val updaterSerializer: KSerializer<Unit>

    /**
     * 当所属于这个 [PluginData] 的 [Value] 的 [值][Value.value] 被修改时被调用.
     * 调用者为 [Value] 的实现.
     */
    @ConsoleExperimentalApi
    public fun onValueChanged(value: Value<*>)

    /**
     * 用于支持多态序列化.
     *
     * @see SerializersModule
     * @see serializersModuleOf
     */
    @ConsoleExperimentalApi
    public val serializersModule: SerializersModule

    /**
     * 当这个 [PluginData] 被放入一个 [PluginDataStorage] 时调用
     */
    @ConsoleExperimentalApi
    public fun onInit(owner: PluginDataHolder, storage: PluginDataStorage)
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
    crossinline apply: T.() -> Unit = {},
): SerializerAwareValue<T> =
    valueFromKType(typeOf0<T>(), default).also { it.value.apply() }

/**
 * 通过具体化类型创建一个 [SerializerAwareValue].
 * @see valueFromKType 查看更多实现信息
 */
@ResolveContext(RESTRICTED_NO_ARG_CONSTRUCTOR)
@LowPriorityInOverloadResolution
public inline fun <@ResolveContext(RESTRICTED_NO_ARG_CONSTRUCTOR) reified T>
        PluginData.value(apply: T.() -> Unit = {}): SerializerAwareValue<T> =
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
