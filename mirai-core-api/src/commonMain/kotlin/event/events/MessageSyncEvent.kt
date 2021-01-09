/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("BotEventsKt")

package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.message.data.source


/**
 * 机器人在其他客户端发送消息同步到这个客户端的事件.
 *
 * 本事件发生于**机器人账号**在另一个客户端向一个群或一个好友主动发送消息, 这条消息同步到机器人这个客户端上.
 *
 * @see MessageEvent
 */
public interface MessageSyncEvent : MessageEvent


/**
 * 机器人在其他客户端发送群消息同步到这个客户端的事件
 *
 * @see MessageSyncEvent
 */
public class GroupMessageSyncEvent(
    override val group: Group,
    override val message: MessageChain,
    override val sender: Member,
    override val senderName: String,
    override val time: Int
) : AbstractMessageEvent(), GroupAwareMessageEvent, MessageSyncEvent {
    init {
        val source = message[MessageSource] ?: error("Cannot find MessageSource from message")
        check(source is OnlineMessageSource.Incoming.FromGroup) { "source provided to a GroupMessageSyncEvent must be an instance of OnlineMessageSource.Incoming.FromGroup" }
    }

    override val bot: Bot get() = group.bot
    override val subject: Group get() = group
    override val source: OnlineMessageSource.Incoming.FromGroup get() = message.source as OnlineMessageSource.Incoming.FromGroup

    public override fun toString(): String =
        "GroupMessageSyncEvent(group=${group.id}, senderName=$senderName, sender=${sender.id}, message=$message)"
}
