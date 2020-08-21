/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER", "EXPOSED_SUPER_CLASS", "NOTHING_TO_INLINE")

package net.mamoe.mirai.console.data

import kotlinx.serialization.KSerializer
import net.mamoe.mirai.console.data.PluginData.ValueNode
import net.mamoe.mirai.console.internal.data.PluginDataImpl
import net.mamoe.mirai.console.internal.data.serialName
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import kotlin.reflect.KProperty

/**
 * [PluginData] 的默认实现. 支持使用 `by value()` 等委托方法创建 [Value] 并跟踪其改动.
 *
 * @see PluginData
 */
public abstract class AbstractPluginData : PluginData, PluginDataImpl() {
    /**
     * 添加了追踪的 [ValueNode] 列表, 即通过 `by value` 初始化的属性列表.
     *
     * 他们的修改会被跟踪, 并触发 [onValueChanged].
     *
     * @see provideDelegate
     */
    public override val valueNodes: MutableList<ValueNode<*>> = mutableListOf()

    /**
     * 使用 `by` 时自动调用此方法, 添加对 [Value] 的值修改的跟踪.
     *
     * 将会创建一个 [ValueNode] 并添加到 [valueNodes]
     */
    public final override operator fun <T> SerializerAwareValue<T>.provideDelegate(
        thisRef: Any?,
        property: KProperty<*>
    ): SerializerAwareValue<T> = apply { valueNodes.add(ValueNode(property.serialName, this, this.serializer)) }

    /**
     * 值更新序列化器. 仅供内部使用.
     */
    @ConsoleExperimentalAPI
    public final override val updaterSerializer: KSerializer<Unit>
        get() = super.updaterSerializer

    /**
     * 当所属于这个 [PluginData] 的 [Value] 的 [值][Value.value] 被修改时被调用.
     */
    public abstract override fun onValueChanged(value: Value<*>)
}