/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message.data

/**
 * 类型 Key. 由伴生对象实现, 表示一个 [Message] 对象的类型.
 *
 * 每个 [ConstrainSingle] 类型都拥有一个伴生对象来持有 [MessageKey] 以允许 `val source = chain[MessageSource]` 的用法.
 *
 * #### 用例
 * [MessageChain.get][MessageChain.get]: 允许使用数组访问操作符获取指定类型的消息元素
 * ```
 * val source: MessageSource = chain[MessageSource]
 * ```
 *
 * @param M 指代持有这个 Key 的消息类型
 */
@ExperimentalMessageKey
public interface MessageKey<out M : SingleMessage> {
    /**
     * 将一个 [SingleMessage] 强转为 [M] 类型. 在类型不符合时返回 `null`
     */
    public val safeCast: (SingleMessage) -> M?
}

@ExperimentalMessageKey
public fun MessageKey<*>.isInstance(message: SingleMessage): Boolean = this.safeCast(message) != null

/**
 * 获取最上层 [MessageKey].
 *
 * 当 [this][MessageKey] 为 [AbstractPolymorphicMessageKey]
 *
 * 如 [FlashImage], 其 [MessageKey]
 */
@ExperimentalMessageKey
public val <A : SingleMessage> MessageKey<A>.topmostKey: MessageKey<*>
    get() = when (this) {
        is AbstractPolymorphicMessageKey<*, *> -> {
            this.topmostKey
        }
        else -> this
    }