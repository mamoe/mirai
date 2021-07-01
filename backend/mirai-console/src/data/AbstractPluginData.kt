/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER", "EXPOSED_SUPER_CLASS", "NOTHING_TO_INLINE", "unused")

package net.mamoe.mirai.console.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import net.mamoe.mirai.console.internal.data.PluginDataImpl
import net.mamoe.mirai.console.internal.data.getAnnotationListForValueSerialization
import net.mamoe.mirai.console.internal.data.valueName
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import kotlin.reflect.KProperty

/**
 * [PluginData] 的默认实现. 支持使用 `by value()` 等委托方法创建 [Value] 并跟踪其改动.
 *
 * ### 实现注意
 * 此类型处于实验性阶段. 使用其中定义的属性和函数是安全的, 但将来可能会新增成员抽象函数.
 *
 * @see PluginData
 */
public abstract class AbstractPluginData : PluginData, PluginDataImpl() {
    /**
     * 这个 [PluginData] 保存时使用的名称.
     */
    public abstract override val saveName: String

    /**
     * 添加了追踪的 [ValueNode] 列表, 即通过 `by value` 初始化的属性列表.
     *
     * 它们的修改会被跟踪, 并触发 [onValueChanged].
     *
     * @see provideDelegate
     */
    @ConsoleExperimentalApi
    public val valueNodes: MutableList<ValueNode<*>> = mutableListOf()

    /**
     * 供手动实现时值跟踪使用 (如 Java 用户). 一般 Kotlin 用户需使用 [provideDelegate]
     */
    @ConsoleExperimentalApi
    public open fun <T : SerializerAwareValue<*>> track(
        value: T,
        /**
         * 值名称.
         *
         * 如果属性带有 [ValueName], 则使用 [ValueName.value],
         * 否则使用 [属性名称][KProperty.name]
         *
         * @see [ValueNode.value]
         */
        valueName: String,
        annotations: List<Annotation>,
    ): T =
        value.apply { this@AbstractPluginData.valueNodes.add(ValueNode(valueName, this, annotations, this.serializer)) }

    /**
     * 使用 `by value()` 时自动调用此方法, 添加对 [Value] 的值修改的跟踪, 并创建 [ValueNode] 加入 [valueNodes]
     */
    public operator fun <T : SerializerAwareValue<*>> T.provideDelegate(
        thisRef: Any?,
        property: KProperty<*>,
    ): T = track(this, property.valueName, property.getAnnotationListForValueSerialization())

    /**
     * 所有 [valueNodes] 更新和保存序列化器.
     */
    @ConsoleExperimentalApi
    public final override val updaterSerializer: KSerializer<Unit>
        get() = super.updaterSerializer

    @ConsoleExperimentalApi
    public override val serializersModule: SerializersModule = EmptySerializersModule

    /**
     * 当所属于这个 [PluginData] 的 [Value] 的 [值][Value.value] 被修改时被调用.
     */
    @ConsoleExperimentalApi
    public override fun onValueChanged(value: Value<*>) {
        // no-op by default
    }

    /**
     * 当这个 [PluginData] 被放入一个 [PluginDataStorage] 时调用
     */
    @ConsoleExperimentalApi
    public override fun onInit(owner: PluginDataHolder, storage: PluginDataStorage) {
        // no-op by default
    }

    /**
     * 由 [track] 创建, 来自一个通过 `by value` 初始化的属性节点.
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
        val updaterSerializer: KSerializer<Unit>,
    )
}

/**
 * 获取这个 [KProperty] 委托的 [Value]
 *
 * 示例:
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
@ConsoleExperimentalApi
public fun <T> AbstractPluginData.findBackingFieldValue(property: KProperty<T>): Value<out T>? =
    findBackingFieldValue(property.valueName)

/**
 * 获取这个 [KProperty] 委托的 [Value]
 *
 * 示例:
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
@ConsoleExperimentalApi
public fun <T> AbstractPluginData.findBackingFieldValue(propertyValueName: String): Value<out T>? {
    @Suppress("UNCHECKED_CAST")
    return this.valueNodes.find { it.valueName == propertyValueName }?.value as Value<out T>?
}

/**
 * 获取这个 [KProperty] 委托的 [Value]
 *
 * 示例:
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
@ConsoleExperimentalApi
public fun <T> AbstractPluginData.findBackingFieldValueNode(property: KProperty<T>): AbstractPluginData.ValueNode<out T>? {
    @Suppress("UNCHECKED_CAST")
    return this.valueNodes.find { it == property } as AbstractPluginData.ValueNode<out T>?
}