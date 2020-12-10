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
 * 除 [MessageChain] 外, 每个 [Message] 类型都拥有一个伴生对象 (companion object) 来持有一个 Key
 *
 * 在 [MessageChain.get] 时将会使用到这个 Key 进行判断类型.
 *
 * #### 用例
 * [MessageChain.get][MessageChain.get]: 允许使用数组访问操作符获取指定类型的消息元素
 * ```
 * val image: Image = chain[Image]
 * ```
 *
 * @param M 指代持有这个 Key 的消息类型
 */
@ExperimentalMessageKey
public interface MessageKey<out M : SingleMessage> {
    public val safeCast: (SingleMessage) -> M?
}

@ExperimentalMessageKey
public fun MessageKey<*>.isInstance(message: SingleMessage): Boolean = this.safeCast(message) != null

@ExperimentalMessageKey
public tailrec fun <A : SingleMessage, B : SingleMessage> MessageKey<A>.isSubKeyOf(baseKey: MessageKey<B>): Boolean {
    return when {
        this === baseKey -> true
        this is AbstractPolymorphicMessageKey<*, *> -> {
            this.baseKey.isSubKeyOf(baseKey)
        }
        else -> false
    }
}

@ExperimentalMessageKey
public val <A : SingleMessage> MessageKey<A>.topmostKey: MessageKey<*>
    get() = when (this) {
        is AbstractPolymorphicMessageKey<*, *> -> {
            this.baseKey.topmostKey
        }
        else -> this
    }