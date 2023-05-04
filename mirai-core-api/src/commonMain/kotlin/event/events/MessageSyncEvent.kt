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
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.message.data.source
import net.mamoe.mirai.utils.DeprecatedSinceMirai
import net.mamoe.mirai.utils.MiraiInternalApi
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName


/**
 * 机器人在其他客户端发送消息同步到这个客户端的事件.
 *
 * 本事件发生于**机器人账号**在另一个客户端向一个群或一个好友主动发送消息, 这条消息同步到机器人这个客户端上.
 *
 * @see MessageEvent
 */
public interface MessageSyncEvent : MessageEvent, OtherClientEvent {
    public override val client: OtherClient
    override val bot: Bot get() = sender.bot // don't rely on `client`, old version does not have client.
}

/**
 * 机器人在其他客户端发送群临时会话消息同步到这个客户端的事件
 *
 * @see MessageSyncEvent
 */
@OptIn(MiraiInternalApi::class)
public class GroupTempMessageSyncEvent private constructor(
    private val _client: OtherClient?,
    public override val sender: NormalMember,
    public override val message: MessageChain,
    public override val time: Int,
    @Suppress("UNUSED_PARAMETER", "LocalVariableName") _primaryConstructorMark: Any?
) : AbstractMessageEvent(), GroupAwareMessageEvent, MessageSyncEvent {
    /**
     * @since 2.13
     */
    public override val client: OtherClient
        get() = _client ?: error("client is not set. Please use the new constructor.")

    /**
     * @since 2.13
     */
    public constructor(
        client: OtherClient,
        sender: NormalMember,
        message: MessageChain,
        time: Int
    ) : this(client, sender, message, time, null)

    @Deprecated(
        "Please use the new constructor.",
        replaceWith = ReplaceWith("GroupTempMessageSyncEvent(client, sender, message, time)"),
        level = DeprecationLevel.WARNING
    )
    @DeprecatedSinceMirai(warningSince = "2.13")
    public constructor(
        sender: NormalMember,
        message: MessageChain,
        time: Int
    ) : this(null, sender, message, time, null)


    init {
        val source = message[MessageSource] ?: error("Cannot find MessageSource from message")
        check(source is OnlineMessageSource.Incoming.FromTemp) { "source provided to a GroupTempMessageSyncEvent must be an instance of OnlineMessageSource.Incoming.FromTemp" }
    }

    public override val bot: Bot get() = sender.bot
    public override val subject: NormalMember get() = sender
    public override val group: Group get() = sender.group
    public override val senderName: String get() = sender.nameCardOrNick
    public override val source: OnlineMessageSource.Incoming.FromTemp get() = message.source as OnlineMessageSource.Incoming.FromTemp
}

/**
 * 机器人在其他客户端发送好友消息同步到这个客户端的事件
 *
 * @see MessageSyncEvent
 */
@OptIn(MiraiInternalApi::class)
public class FriendMessageSyncEvent private constructor(
    private val _client: OtherClient?,
    public override val sender: Friend,
    public override val message: MessageChain,
    public override val time: Int,
    @Suppress("UNUSED_PARAMETER", "LocalVariableName") _primaryConstructorMark: Any?
) : AbstractMessageEvent(), FriendEvent, MessageSyncEvent {
    /**
     * @since 2.13
     */
    public override val client: OtherClient
        get() = _client ?: error("client is not set. Please use the new constructor.")

    /**
     * @since 2.13
     */
    public constructor(
        client: OtherClient,
        sender: Friend,
        message: MessageChain,
        time: Int
    ) : this(client, sender, message, time, null)

    @Deprecated(
        "Please use the new constructor.",
        replaceWith = ReplaceWith("FriendMessageSyncEvent(client, sender, message, time)"),
        level = DeprecationLevel.WARNING
    )
    @DeprecatedSinceMirai(warningSince = "2.13")
    public constructor(
        sender: Friend,
        message: MessageChain,
        time: Int
    ) : this(null, sender, message, time, null)


    init {
        val source =
            message[MessageSource] ?: throw IllegalArgumentException("Cannot find MessageSource from message")
        check(source is OnlineMessageSource.Incoming.FromFriend) { "source provided to a FriendMessageSyncEvent must be an instance of OnlineMessageSource.Incoming.FromFriend" }
    }

    public override val friend: Friend get() = sender
    public override val bot: Bot get() = sender.bot
    public override val subject: Friend get() = sender
    public override val senderName: String get() = sender.nick
    public override val source: OnlineMessageSource.Incoming.FromFriend get() = message.source as OnlineMessageSource.Incoming.FromFriend
}

/**
 * 机器人在其他客户端发送陌生人消息同步到这个客户端的事件
 *
 * @see MessageSyncEvent
 */
@OptIn(MiraiInternalApi::class)
public class StrangerMessageSyncEvent private constructor(
    private val _client: OtherClient?,
    public override val sender: Stranger,
    public override val message: MessageChain,
    public override val time: Int,
    @Suppress("UNUSED_PARAMETER", "LocalVariableName") _primaryConstructorMark: Any?,
) : AbstractMessageEvent(), StrangerEvent, MessageSyncEvent {
    /**
     * @since 2.13
     */
    public override val client: OtherClient
        get() = _client ?: error("client is not set. Please use the new constructor.")

    /**
     * @since 2.13
     */
    public constructor(
        client: OtherClient,
        sender: Stranger,
        message: MessageChain,
        time: Int
    ) : this(client, sender, message, time, null)

    @Deprecated(
        "Please use the new constructor.",
        replaceWith = ReplaceWith("StrangerMessageSyncEvent(client, sender, message, time)"),
        level = DeprecationLevel.WARNING
    )
    @DeprecatedSinceMirai(warningSince = "2.13")
    public constructor(
        sender: Stranger,
        message: MessageChain,
        time: Int
    ) : this(null, sender, message, time, null)


    init {
        val source =
            message[MessageSource] ?: throw IllegalArgumentException("Cannot find MessageSource from message")
        check(source is OnlineMessageSource.Incoming.FromStranger) { "source provided to a StrangerMessageSyncEvent must be an instance of OnlineMessageSource.Incoming.FromStranger" }
    }

    public override val stranger: Stranger get() = sender
    public override val bot: Bot get() = sender.bot
    public override val subject: Stranger get() = sender
    public override val senderName: String get() = sender.nick
    public override val source: OnlineMessageSource.Incoming.FromStranger get() = message.source as OnlineMessageSource.Incoming.FromStranger
}

/**
 * 机器人在其他客户端发送群消息同步到这个客户端的事件
 *
 * @see MessageSyncEvent
 */
@OptIn(MiraiInternalApi::class)
public class GroupMessageSyncEvent private constructor(
    private val _client: OtherClient?,
    public override val group: Group,
    public override val message: MessageChain,
    public override val sender: Member,
    public override val senderName: String,
    public override val time: Int,
    @Suppress("UNUSED_PARAMETER", "LocalVariableName") _primaryConstructorMark: Any?,
) : AbstractMessageEvent(), GroupAwareMessageEvent, MessageSyncEvent {
    /**
     * @since 2.13
     */
    public override val client: OtherClient
        get() = _client ?: error("client is not set. Please use the new constructor.")

    /**
     * @since 2.13
     */
    public constructor(
        client: OtherClient,
        group: Group,
        message: MessageChain,
        sender: Member,
        senderName: String,
        time: Int
    ) : this(client, group, message, sender, senderName, time, null)

    @Deprecated(
        "Please use the new constructor.",
        replaceWith = ReplaceWith("GroupMessageSyncEvent(client, group, message, sender, senderName, time)"),
        level = DeprecationLevel.WARNING
    )
    @DeprecatedSinceMirai(warningSince = "2.13")
    public constructor(
        group: Group,
        message: MessageChain,
        sender: Member,
        senderName: String,
        time: Int
    ) : this(null, group, message, sender, senderName, time, null)

    init {
        val source = message[MessageSource] ?: error("Cannot find MessageSource from message")
        check(source is OnlineMessageSource.Incoming.FromGroup) { "source provided to a GroupMessageSyncEvent must be an instance of OnlineMessageSource.Incoming.FromGroup" }
    }

    override val bot: Bot get() = sender.bot
    override val subject: Group get() = group
    override val source: OnlineMessageSource.Incoming.FromGroup get() = message.source as OnlineMessageSource.Incoming.FromGroup

    public override fun toString(): String =
        "GroupMessageSyncEvent(group=${group.id}, senderName=$senderName, sender=${sender.id}, message=$message)"
}
