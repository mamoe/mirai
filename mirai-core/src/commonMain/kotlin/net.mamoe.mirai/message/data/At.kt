/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
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
import net.mamoe.mirai.message.code.CodableMessage
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic


/**
 * At 一个群成员. 只能发送给一个群.
 *
 * ## mirai 码支持
 * 格式: &#91;mirai:at:*[target]*,*[display]*&#93;
 *
 * @see AtAll 全体成员
 */
data class At
@Suppress("DataClassPrivateConstructor")
private constructor(
    val target: Long,
    /**
     * "@群员名片"
     */
    val display: String
) : MessageContent, CodableMessage {

    /**
     * 构造一个 [At] 实例. 这是唯一的公开的构造方式.
     */
    constructor(member: Member) : this(member.id, "@${member.nameCardOrNick}")

    override fun equals(other: Any?): Boolean {
        return other is At && other.target == this.target && other.display == this.display
    }

    override fun toString(): String = "[mirai:at:$target,$display]"
    override fun contentToString(): String = this.display

    companion object Key : Message.Key<At> {
        override val typeName: String
            get() = "At"

        /**
         * 构造一个 [At], 仅供内部使用, 否则可能造成消息无法发出的问题.
         */
        @Suppress("FunctionName")
        @JvmStatic
        @LowLevelAPI
        fun _lowLevelConstructAtInstance(target: Long, display: String): At = At(target, display)
    }

    // 自动为消息补充 " "
    override fun followedBy(tail: Message): MessageChain {
        if (tail is PlainText && tail.content.startsWith(' ')) {
            return super<MessageContent>.followedBy(tail)
        }
        return super<MessageContent>.followedBy(PlainText(" ")) + tail
    }

    override fun hashCode(): Int {
        var result = target.hashCode()
        result = 31 * result + display.hashCode()
        return result
    }

}

/**
 * At 这个成员
 */
@JvmSynthetic
@Suppress("NOTHING_TO_INLINE")
inline fun Member.at(): At = At(this)