/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("DEPRECATION_ERROR", "unused", "NOTHING_TO_INLINE")

package net.mamoe.mirai.message

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.BroadcastControllable
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.message.data.source

/**
 * 机器人收到的群临时会话消息的事件
 *
 * @see MessageEvent
 */
class TempMessageEvent(
    override val sender: Member,
    override val message: MessageChain,
    override val time: Int
) : TempMessage(), BroadcastControllable {
    init {
        val source = message[MessageSource] ?: error("Cannot find MessageSource from message")
        check(source is OnlineMessageSource.Incoming.FromTemp) { "source provided to a TempMessage must be an instance of OnlineMessageSource.Incoming.FromTemp" }
    }

    override val bot: Bot get() = sender.bot
    override val subject: Member get() = sender
    override val group: Group get() = sender.group
    override val senderName: String get() = sender.nameCardOrNick
    override val source: OnlineMessageSource.Incoming.FromTemp get() = message.source as OnlineMessageSource.Incoming.FromTemp

    override fun toString(): String =
        "TempMessageEvent(sender=${sender.id} from group(${sender.group.id}), message=$message)"
}