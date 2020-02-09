/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message.data


inline class PlainText(val stringValue: String) : Message {
    override operator fun contains(sub: String): Boolean = sub in stringValue
    override fun toString(): String = stringValue

    companion object Key : Message.Key<PlainText>

    override fun eq(other: Message): Boolean {
        if(other is MessageChain){
            return other eq this.toString()
        }
        return other is PlainText && other.stringValue == this.stringValue
    }
}

/**
 * 构造 [PlainText]
 */
@Suppress("NOTHING_TO_INLINE")
inline fun String.toMessage(): PlainText = PlainText(this)

/**
 * 得到包含作为 [PlainText] 的 [this] 的 [MessageChain].
 *
 * @return 唯一成员且不可修改的 [SingleMessageChainImpl]
 *
 * @see SingleMessageChain
 * @see SingleMessageChainImpl
 */
@Suppress("NOTHING_TO_INLINE")
inline fun String.singleChain(): MessageChain = SingleMessageChainImpl(this.toMessage())