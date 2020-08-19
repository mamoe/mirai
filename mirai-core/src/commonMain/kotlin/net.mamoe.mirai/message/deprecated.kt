/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress(
    "EXPERIMENTAL_UNSIGNED_LITERALS",
    "EXPERIMENTAL_API_USAGE",
    "unused",
    "DECLARATION_CANT_BE_INLINED", "UNCHECKED_CAST", "NOTHING_TO_INLINE"
)

@file:JvmMultifileClass
@file:JvmName("MessageEventKt")

package net.mamoe.mirai.message

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.qqandroid.network.Packet
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName


/**
 * 已废弃, 请使用 [MessageEvent]
 */
@Deprecated(
    message = "use MessageEvent",
    replaceWith = ReplaceWith("MessageEvent", "net.mamoe.mirai.message.MessageEvent"),
    level = DeprecationLevel.HIDDEN
)
public abstract class MessagePacketBase<out TSender : User, out TSubject : Contact> : Packet, BotEvent,
    AbstractEvent() {
    abstract override val bot: Bot
    public abstract val sender: User
    public abstract val subject: Contact
    public abstract val message: MessageChain
    public abstract val time: Int
    public abstract val source: OnlineMessageSource.Incoming
    public abstract val senderName: String
}

@Deprecated(
    message = "Ambiguous name. Use MessageEvent instead",
    replaceWith = ReplaceWith("MessageEvent", "net.mamoe.mirai.message.MessageEvent"),
    level = DeprecationLevel.HIDDEN
)
@Suppress("DEPRECATION_ERROR")
public abstract class MessagePacket : MessagePacketBase<User, Contact>(),
    BotEvent, MessageEventExtensions<User, Contact> {
    abstract override val bot: Bot
    abstract override val sender: User
    abstract override val subject: Contact
    abstract override val message: MessageChain
    abstract override val time: Int
    abstract override val source: OnlineMessageSource.Incoming
    abstract override val senderName: String
}

@Deprecated(
    message = "Ambiguous name. Use MessageEvent instead",
    replaceWith = ReplaceWith("MessageEvent", "net.mamoe.mirai.message.MessageEvent"),
    level = DeprecationLevel.HIDDEN
)
@Suppress("DEPRECATION_ERROR")
public abstract class ContactMessage : MessagePacket(),
    BotEvent, MessageEventExtensions<User, Contact> {
    abstract override val bot: Bot
    abstract override val sender: User
    abstract override val subject: Contact
    abstract override val message: MessageChain
    abstract override val time: Int
    abstract override val source: OnlineMessageSource.Incoming
    abstract override val senderName: String
}

@Deprecated(
    message = "Ambiguous name. Use FriendMessageEvent instead",
    replaceWith = ReplaceWith("FriendMessageEvent", "net.mamoe.mirai.message.FriendMessageEvent"),
    level = DeprecationLevel.HIDDEN
)
@Suppress("DEPRECATION_ERROR")
public abstract class FriendMessage : MessageEvent() {
    abstract override val bot: Bot
    abstract override val sender: Friend
    abstract override val subject: Friend
    abstract override val message: MessageChain
    abstract override val time: Int
    abstract override val source: OnlineMessageSource.Incoming.FromFriend
    abstract override val senderName: String
}

@Deprecated(
    message = "Ambiguous name. Use GroupMessageEvent instead",
    replaceWith = ReplaceWith("GroupMessageEvent", "net.mamoe.mirai.message.GroupMessageEvent"),
    level = DeprecationLevel.HIDDEN
)
@Suppress("DEPRECATION_ERROR")
public abstract class GroupMessage : MessageEvent() {
    public abstract val group: Group
    abstract override val bot: Bot
    abstract override val sender: Member
    abstract override val subject: Group
    abstract override val message: MessageChain
    abstract override val time: Int
    abstract override val source: OnlineMessageSource.Incoming.FromGroup
    abstract override val senderName: String
}

@Deprecated(
    message = "Ambiguous name. Use TempMessageEvent instead",
    replaceWith = ReplaceWith("TempMessageEvent", "net.mamoe.mirai.message.TempMessageEvent"),
    level = DeprecationLevel.HIDDEN
)
public abstract class TempMessage : MessageEvent() {
    abstract override val bot: Bot
    abstract override val sender: Member
    abstract override val subject: Member
    abstract override val message: MessageChain
    abstract override val time: Int
    abstract override val source: OnlineMessageSource.Incoming.FromTemp
    public abstract val group: Group
    abstract override val senderName: String
}