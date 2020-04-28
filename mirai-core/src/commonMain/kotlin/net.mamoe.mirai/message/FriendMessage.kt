/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("DEPRECATION_ERROR")

package net.mamoe.mirai.message

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.event.BroadcastControllable
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.message.data.source
import net.mamoe.mirai.utils.PlannedRemoval
import net.mamoe.mirai.utils.currentTimeSeconds
import net.mamoe.mirai.utils.getValue
import net.mamoe.mirai.utils.unsafeWeakRef

/**
 * 好友消息
 */
class FriendMessage constructor(
    sender: Friend,
    override val message: MessageChain,
    override val time: Int
) : ContactMessage(), BroadcastControllable {
    @PlannedRemoval("1.0.0")
    @Deprecated("", level = DeprecationLevel.HIDDEN)
    constructor(sender: QQ, message: MessageChain) : this(sender as Friend, message, currentTimeSeconds.toInt())

    @PlannedRemoval("1.0.0")
    @Deprecated("", level = DeprecationLevel.HIDDEN)
    constructor(sender: Friend, message: MessageChain) : this(sender, message, currentTimeSeconds.toInt())

    init {
        val source = message.getOrNull(MessageSource) ?: error("Cannot find MessageSource from message")
        check(source is OnlineMessageSource.Incoming.FromFriend) { "source provided to a FriendMessage must be an instance of OnlineMessageSource.Incoming.FromFriend" }
    }

    override val sender: Friend by sender.unsafeWeakRef()
    override val bot: Bot get() = sender.bot
    override val subject: Friend get() = sender
    override val senderName: String get() = sender.nick
    override val source: OnlineMessageSource.Incoming.FromFriend get() = message.source as OnlineMessageSource.Incoming.FromFriend

    override fun toString(): String = "FriendMessage(sender=${sender.id}, message=$message)"
}
