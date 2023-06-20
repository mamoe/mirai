/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress(
    "INVISIBLE_REFERENCE",
    "INVISIBLE_MEMBER",
)
@file:JvmName("PluginDataKt")

package net.mamoe.mirai.console.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.RESTRICTED_NO_ARG_CONSTRUCTOR
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginData
import net.mamoe.mirai.console.internal.data.createInstanceSmart
import net.mamoe.mirai.console.internal.data.valueFromKTypeImpl
import net.mamoe.mirai.console.internal.data.valueImpl
import net.mamoe.mirai.console.plugin.jvm.AbstractJvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.reloadPluginData
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.NotStableForInheritance
import kotlin.internal.LowPriorityInOverloadResolution
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

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
 * 参考 [JavaAutoSavePluginData]
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
 * 要查看详细的解释，请查看 [docs/PluginData.md](/mirai-console/docs/PluginData.md)
 *
 * ## 实现注意
 * 此类型处于实验性阶段. 使用其中定义的属性和函数是安全的, 但将来可能会新增成员抽象函数.
 *
 * 继承 [AbstractPluginData] 比继承 [PluginData] 更安全, 尽管 [AbstractPluginData] 也不稳定.
 *
 * @see AbstractJvmPlugin.reloadPluginData 通过 [JvmPlugin] 获取指定 [PluginData] 实例.
 * @see PluginDataStorage [PluginData] 存储仓库
 * @see PluginDataExtensions 相关 [SerializerAwareValue] 映射函数
 */
@NotStableForInheritance
public interface PluginData {
    /**
     * 这个 [PluginData] 保存时使用的名称.
     */
    @ConsoleExperimentalApi
    public val saveName: String

    /**
     * [PluginData] 序列化时使用的格式的枚举.
     */
    @ConsoleExperimentalApi
    public enum class SaveType(@ConsoleExperimentalApi public val extension: String) {
        YAML("yml"), JSON("json")
    }

    /**
     * 决定这个 [PluginData] 序列化时使用的格式, 默认为 YAML.
     * 具体实现格式由 [PluginDataStorage] 决定.
     */
    @ConsoleExperimentalApi
    public val saveType: SaveType get() = SaveType.YAML

    @ConsoleExperimentalApi
    public val updaterSerializer: KSerializer<Unit>

    /**
     * 当所属于这个 [PluginData] 的 [Value] 的 [值][Value.value] 被修改时被调用.
     * 调用者为 [Value] 的实现.
     */
    @ConsoleExperimentalApi
    public fun onValueChanged(value: Value<*>)

    /**
     * 序列化本对象数据时使用的 [SerializersModule]. 用于支持多态序列化等.
     * 在序列化时会先使用 [PluginData.serializersModule], 再对无法找到 serializer 的类型使用 [MessageSerializers.serializersModule].
     *
     * ### 使用示例
     *
     * 假设你编写了一个类型 `ChatHistory` 用来存储一个群的消息记录:
     * ```
     * data class ChatHistory(
     *     val groupId: Long,
     *     val chain: List<MessageChain>,
     * )
     * ```
     *
     * 要在 [PluginData] 中支持它, 需要首先为 `ChatHistory` 编写 [KSerializer].
     *
     * 一种方式是为其添加 [kotlinx.serialization.Serializable]:
     *
     * ```
     * @Serializable
     * data class ChatHistory(
     *     val groupId: Long,
     *     val chain: List<MessageChain>,
     * )
     * ```
     *
     * 编译器将会自动生成一个 [KSerializer], 可通过 `ChatHistory.Companion.serializer()` 获取.
     *
     * 然后在 [PluginData] 定义中添加该 [KSerializer]:
     * ```
     * object MyData : AutoSavePluginData("save") {
     *     // 注意, serializersModule 需要早于其他属性定义初始化
     *     override val serializersModule = SerializersModule {
     *         contextual(ChatHistory::class, ChatHistory.serializers()) // 为 ChatHistory 指定 KSerializer
     *     }
     *
     *     val histories: Map<Long, ChatHistory> by value()
     * }
     * ```
     *
     * 然而, 即使不覆盖 `serializersModule` 提供 [KSerializer], mirai 也会通过反射尝试获取.
     *
     * 但对于不是使用 `@Serializable` 注解方式, 或者是 `interface`, `abstract class` 等的抽象类型, 则必须覆盖 `serializersModule` 并提供其 [KSerializer].
     *
     *
     *
     * @see SerializersModule
     *
     * @since 2.11
     */
    public val serializersModule: SerializersModule // 该属性在 2.0 增加, 但在 2.11 才正式支持并删除 @MiraiExperimentalApi

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
 * 2.11 起, 本函数会优先根据返回值推断类型. 如下示例:
 * ```
 * var singleMessage: SingleMessage by value(PlainText("str")) // value 的类型为 SerializerAwareValue<SingleMessage>
 * ```
 * 这符合正常的类型定义逻辑.
 *
 * @param T 具体化参数类型 T. 在 2.11 以前, 支持:
 * - 基础数据类型
 * - 标准库集合类型 ([List], [Map], [Set])
 * - 标准库数据类型 ([Map.Entry], [Pair], [Triple])
 * - 使用 [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) 的 [Serializable] 标记的, 可以通过反射获取 [KSerializer] 的类型
 *
 * 2.11 起, 还支持:
 * - [MessageSerializers] 支持的所有类型, 如 [MessageChain].
 * - 在 [PluginData.serializersModule] 自定义支持的类型
 */
@OptIn(ConsoleExperimentalApi::class)
@LowPriorityInOverloadResolution
public inline fun <reified T> PluginData.value(
    default: T,
    crossinline apply: T.() -> Unit = {},
): SerializerAwareValue<@kotlin.internal.Exact T> {
    /*
     * 使用 `@Exact` 的 trick (自 2.11.0-RC)
     *
     * 使用前:
     *
     * ```
     * var singleMessage: SingleMessage by value(PlainText("str"))
     * ```
     *
     * `value` 的 reified [T] 根据其参数推断为 [PlainText], 则会使用 `PlainText.serializer()`.
     *
     * 可以通过序列化后的 YAML 文本直观地感受问题:
     * ```yaml
     * singleMessage:
     *   content: str
     * ```
     * 那么将来若 `singleMessage` 的值变更为非 [PlainText] 类型, 将会无法序列化.
     *
     * 附使用 `@Exact` 时的正确结果 (使用 [SingleMessage] 的序列化器 (来自 [MessageSerializers.serializersModule])):
     *
     * ```yaml
     * singleMessage:
     *   type: PlainText
     *   value:
     *     content: str
     * ```
     *
     * 相关测试: [net.mamoe.mirai.console.data.PluginDataTest.supports message chain]
     */
    return valueFromKType(typeOf<T>(), default).also { it.value.apply() }
}

/**
 * 通过具体化类型创建一个 [SerializerAwareValue].
 */
@ResolveContext(RESTRICTED_NO_ARG_CONSTRUCTOR)
@LowPriorityInOverloadResolution
public inline fun <@ResolveContext(RESTRICTED_NO_ARG_CONSTRUCTOR) reified T>
        PluginData.value(apply: T.() -> Unit = {}): SerializerAwareValue<@kotlin.internal.Exact T> =
    valueImpl<T>(typeOf<T>(), T::class).also { it.value.apply() }

@OptIn(ConsoleExperimentalApi::class)
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
