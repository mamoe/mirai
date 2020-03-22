/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageUtils")

@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.LowLevelAPI
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.nameCardOrNick
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic


/**
 * At 一个人. 只能发送给一个群.
 *
 * @see AtAll 全体成员
 */
class At
private constructor(val target: Long, val display: String) : Message, MessageContent {

    /**
     * 构造一个 [At] 实例. 这是唯一的公开的构造方式.
     */
    constructor(member: Member) : this(member.id, "@${member.nameCardOrNick}")

    override fun toString(): String = display

    companion object Key : Message.Key<At> {
        /**
         * 构造一个 [At], 仅供内部使用, 否则可能造成消息无法发出的问题.
         */
        @Suppress("FunctionName")
        @JvmStatic
        @LowLevelAPI
        fun _lowLevelConstructAtInstance(target: Long, display: String): At = At(target, display)
    }

    // 自动为消息补充 " "

    override fun followedBy(tail: Message): CombinedMessage {
        if (tail is PlainText && tail.stringValue.startsWith(' ')) {
            return super<MessageContent>.followedBy(tail)
        }
        return super<MessageContent>.followedBy(PlainText(" ")) + tail
    }
}

/**
 * At 这个成员
 */
@JvmSynthetic
@Suppress("NOTHING_TO_INLINE")
inline fun Member.at(): At = At(this)