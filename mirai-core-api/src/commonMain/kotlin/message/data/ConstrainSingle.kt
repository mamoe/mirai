/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress(
    "MemberVisibilityCanBePrivate", "unused", "EXPERIMENTAL_API_USAGE",
    "NOTHING_TO_INLINE", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE",
    "INAPPLICABLE_JVM_NAME"
)
@file:JvmMultifileClass
@file:JvmName("MessageUtils")

package net.mamoe.mirai.message.data

/**
 * 约束一个 [MessageChain] 中只存在这一种类型的元素. 新元素将会替换旧元素, 保持原顺序.
 *
 * 实现此接口的元素将会在连接时自动处理替换.
 *
 * - 要获取有关键的信息, 查看 [MessageKey].
 * - 要获取有关约束的处理方式, 查看 [AbstractPolymorphicMessageKey].
 */
public interface ConstrainSingle : SingleMessage {
    /**
     * 用于判断是否为同一种元素的 [MessageKey]. 使用多态类型 [MessageKey] 最上层的 [MessageKey].
     * @see MessageKey 查看更多信息
     */
    public val key: MessageKey<*>
}