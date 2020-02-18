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

import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.groupCardOrNick
import net.mamoe.mirai.utils.MiraiInternalAPI
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName


/**
 * At 一个人. 只能发送给一个群.
 *
 * @see AtAll 全体成员
 */
class At @MiraiInternalAPI constructor(val target: Long, val display: String) : Message {
    @UseExperimental(MiraiInternalAPI::class)
    constructor(member: Member) : this(member.id, "@${member.groupCardOrNick}")

    override fun toString(): String = display

    companion object Key : Message.Key<At>

    override fun eq(other: Message): Boolean {
        return other is At && other.target == this.target
    }

    // 自动为消息补充 " "

    override fun followedBy(tail: Message): MessageChain {
        if(tail is PlainText && tail.stringValue.startsWith(' ')){
            return super.followedBy(tail)
        }
        return super.followedBy(PlainText(" ")) + tail
    }
}

/**
 * At 这个成员
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Member.at(): At = At(this)