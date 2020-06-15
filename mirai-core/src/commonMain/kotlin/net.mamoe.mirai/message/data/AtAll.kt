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

package net.mamoe.mirai.message.data

import net.mamoe.mirai.message.code.CodableMessage
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

private const val displayA = "@全体成员"

/**
 * "@全体成员".
 *
 * 非会员每天只能发送 10 次 [AtAll]. 超出部分会被以普通文字看待.
 *
 * ## mirai 码支持
 * 格式: &#91;mirai:atall&#93;
 *
 * @see At at 单个群成员
 */
object AtAll :
    Message.Key<AtAll>,
    MessageContent, CodableMessage {
    const val display = displayA
    override val typeName: String
        get() = "AtAll"

    @Suppress("SpellCheckingInspection")
    override fun toString(): String = "[mirai:atall]"
    override fun contentToString(): String = display
    override fun equals(other: Any?): Boolean {
        return other === this
    }

    override fun hashCode(): Int {
        return display.hashCode()
    }

    // 自动为消息补充 " "
    override fun followedBy(tail: Message): MessageChain {
        if (tail is PlainText && tail.content.startsWith(' ')) {
            return super<MessageContent>.followedBy(tail)
        }
        return super<MessageContent>.followedBy(PlainText(" ")) + tail
    }
}