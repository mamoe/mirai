/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("DEPRECATION_ERROR")

package net.mamoe.mirai.message

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.event.BroadcastControllable
import net.mamoe.mirai.event.events.FriendEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.message.data.source
import net.mamoe.mirai.utils.PlannedRemoval
import net.mamoe.mirai.utils.SinceMirai

/**
 * 机器人收到的好友消息的事件
 *
 * @see MessageEvent
 */
public class FriendMessageEvent constructor(
    public override val sender: Friend,
    public override val message: MessageChain,
    public override val time: Int
) : @PlannedRemoval("1.2.0") FriendMessage(), BroadcastControllable, @SinceMirai("1.2.0") FriendEvent {
    init {
        val source =
            message[MessageSource] ?: throw IllegalArgumentException("Cannot find MessageSource from message")
        check(source is OnlineMessageSource.Incoming.FromFriend) { "source provided to a FriendMessage must be an instance of OnlineMessageSource.Incoming.FromFriend" }
    }

    @SinceMirai("1.2.0")
    public override val friend: Friend
        get() = sender
    public override val bot: Bot get() = super.bot
    public override val subject: Friend get() = sender
    public override val senderName: String get() = sender.nick
    public override val source: OnlineMessageSource.Incoming.FromFriend get() = message.source as OnlineMessageSource.Incoming.FromFriend

    public override fun toString(): String = "FriendMessageEvent(sender=${sender.id}, message=$message)"
}
