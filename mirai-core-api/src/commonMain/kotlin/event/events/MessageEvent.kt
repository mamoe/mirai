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
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.message.data.source
import net.mamoe.mirai.message.isContextIdenticalWith
import net.mamoe.mirai.utils.MiraiInternalApi


/**
 * 一个消息事件.
 *
 * @see isContextIdenticalWith 判断语境相同
 */
public interface MessageEvent : Event, Packet, BotPassiveEvent { // TODO: 2021/1/11 Make sealed interface in Kotlin 1.5
    /**
     * 与这个消息事件相关的 [Bot]
     */
    public override val bot: Bot

    /**
     * 消息事件主体.
     *
     * - 对于私聊会话, 这个属性与 [sender] 相同;
     * - 对于群消息, 这个属性为 [Group] 的实例, 与 [GroupMessageEvent.group] 相同.
     *
     * 如果在 [GroupMessageEvent] 对 [sender] 发送消息, 将会通过私聊发送给群员, 而不会发送在群内.
     * 使用 [subject] 作为消息目标则可以确保消息发送到用户所在的场景.
     *
     * 在回复消息时, 可通过 [subject] 作为回复对象.
     */
    public val subject: Contact

    /**
     * 发送人.
     *
     * 在私聊消息时为相关 [User] 的实例, 在群消息时为 [Member] 的实例, 在其他客户端消息时为 [Bot.asFriend]
     */
    public val sender: User

    /**
     * 发送人名称. 由群员发送时为群员名片, 由好友发送时为好友昵称. 使用 [User.nameCardOrNick] 也能得到相同的结果.
     */
    public val senderName: String

    /**
     * 消息内容.
     *
     * 返回的消息链中一定包含 [MessageSource], 存储此消息的发送人, 发送时间, 收信人, 消息 ids 等数据. 随后的元素为拥有顺序的真实消息内容.
     *
     * 详细查看 [MessageChain]
     */
    public val message: MessageChain

    /** 消息发送时间戳, 单位为秒. 由服务器提供, 可能与本地有时差. */
    public val time: Int

    /**
     * 消息源. 来自 [message]. 相当于对 [message] 以 [MessageSource] 参数调用 [MessageChain.get].
     */
    public val source: OnlineMessageSource.Incoming get() = message.source as OnlineMessageSource.Incoming
}

/**
 * 来自 [User] 的消息
 *
 * @see FriendMessageEvent
 * @see GroupTempMessageEvent
 */
public interface UserMessageEvent : MessageEvent {
    public override val subject: User
}

/**
 * 机器人收到的好友消息的事件
 *
 * @see MessageEvent
 */
public class FriendMessageEvent constructor(
    public override val sender: Friend,
    public override val message: MessageChain,
    public override val time: Int
) : AbstractMessageEvent(), UserMessageEvent, FriendEvent {
    init {
        val source =
            message[MessageSource] ?: throw IllegalArgumentException("Cannot find MessageSource from message")
        check(source is OnlineMessageSource.Incoming.FromFriend) { "source provided to a FriendMessageEvent must be an instance of OnlineMessageSource.Incoming.FromFriend" }
    }

    public override val friend: Friend get() = sender
    public override val bot: Bot get() = super.bot
    public override val subject: Friend get() = sender
    public override val senderName: String get() = sender.nick
    public override val source: OnlineMessageSource.Incoming.FromFriend get() = message.source as OnlineMessageSource.Incoming.FromFriend

    public override fun toString(): String = "FriendMessageEvent(sender=${sender.id}, message=$message)"
}

/**
 * 机器人收到的其他客户端消息的事件
 *
 * @see MessageEvent
 */
public class OtherClientMessageEvent constructor(
    public override val client: OtherClient,
    public override val message: MessageChain,
    public override val time: Int
) : AbstractMessageEvent(), MessageEvent, OtherClientEvent {
    init {
        val source =
            message[MessageSource] ?: throw IllegalArgumentException("Cannot find MessageSource from message")
        check(source is OnlineMessageSource.Incoming.FromFriend) { "source provided to a OtherClientMessageEvent must be an instance of OnlineMessageSource.Incoming.FromFriend" }
    }

    public override val sender: User get() = client.bot.asFriend
    public override val bot: Bot get() = super.bot
    public override val subject: OtherClient get() = client
    public override val senderName: String get() = sender.nick

    /**
     * 为简化处理, 其他客户端消息的 [MessageSource] 被作为 [OnlineMessageSource.Incoming.FromFriend].
     */
    public override val source: OnlineMessageSource.Incoming.FromFriend get() = message.source as OnlineMessageSource.Incoming.FromFriend

    public override fun toString(): String = "OtherClientMessageEvent(client=${client.platform}, message=$message)"
}

/**
 * 来自一个可以知道其 [Group] 的用户消息
 *
 * @see FriendMessageEvent
 * @see GroupTempMessageEvent
 */
public interface GroupAwareMessageEvent : MessageEvent {
    public val group: Group
}

/**
 * 机器人收到的群消息的事件
 *
 * @see MessageEvent
 */
public class GroupMessageEvent(
    public override val senderName: String,
    /**
     * 发送方权限.
     */
    public val permission: MemberPermission,
    /**
     * 发送人. 可能是 [NormalMember] 或 [AnonymousMember]
     */
    public override val sender: Member,
    public override val message: MessageChain,
    public override val time: Int
) : AbstractMessageEvent(), GroupAwareMessageEvent, MessageEvent, GroupEvent {
    init {
        val source = message[MessageSource] ?: error("Cannot find MessageSource from message")
        check(source is OnlineMessageSource.Incoming.FromGroup) { "source provided to a GroupMessageEvent must be an instance of OnlineMessageSource.Incoming.FromGroup" }
    }

    public override val group: Group get() = sender.group
    public override val bot: Bot get() = sender.bot
    public override val subject: Group get() = group
    public override val source: OnlineMessageSource.Incoming.FromGroup get() = message.source as OnlineMessageSource.Incoming.FromGroup

    public override fun toString(): String =
        "GroupMessageEvent(group=${group.id}, senderName=$senderName, sender=${sender.id}, permission=${permission.name}, message=$message)"
}

/**
 * 机器人收到的群临时会话消息的事件
 *
 * @see MessageEvent
 */
@Deprecated(
    "mirai 正计划支持其他渠道发起的临时会话, 届时此事件会变动. 原 TempMessageEvent 已更改为 GroupTempMessageEvent",
    replaceWith = ReplaceWith("GroupTempMessageEvent", "net.mamoe.mirai.event.events.GroupTempMessageEvent"),
    DeprecationLevel.ERROR
)
public sealed class TempMessageEvent constructor(
    public override val sender: NormalMember,
    public override val message: MessageChain,
    public override val time: Int
) : AbstractMessageEvent(), GroupAwareMessageEvent, UserMessageEvent

// support by mirai 2.1
//
//public class UserTempMessageEvent(
//    sender: TempUser,
//    message: MessageChain,
//    time: Int
//) : @Suppress("DEPRECATION_ERROR") TempMessageEvent(sender, message, time), GroupAwareMessageEvent, UserMessageEvent {
//}

/**
 * 群临时会话消息
 */
public class GroupTempMessageEvent(
    public override val sender: NormalMember,
    public override val message: MessageChain,
    public override val time: Int
) : @Suppress("DEPRECATION_ERROR") TempMessageEvent(sender, message, time), GroupAwareMessageEvent, UserMessageEvent {
    init {
        val source = message[MessageSource] ?: error("Cannot find MessageSource from message")
        check(source is OnlineMessageSource.Incoming.FromTemp) { "source provided to a GroupTempMessageEvent must be an instance of OnlineMessageSource.Incoming.FromTemp" }
    }

    public override val bot: Bot get() = sender.bot
    public override val subject: NormalMember get() = sender
    public override val group: Group get() = sender.group
    public override val senderName: String get() = sender.nameCardOrNick
    public override val source: OnlineMessageSource.Incoming.FromTemp get() = message.source as OnlineMessageSource.Incoming.FromTemp

    public override fun toString(): String =
        "GroupTempMessageEvent(sender=${sender.id} from group(${sender.group.id}), message=$message)"
}

/**
 * 机器人收到的陌生人消息的事件
 *
 * @see MessageEvent
 */
public class StrangerMessageEvent constructor(
    public override val sender: Stranger,
    public override val message: MessageChain,
    public override val time: Int
) : AbstractMessageEvent(), UserMessageEvent, StrangerEvent {
    init {
        val source =
            message[MessageSource] ?: throw IllegalArgumentException("Cannot find MessageSource from message")
        check(source is OnlineMessageSource.Incoming.FromStranger) { "source provided to a StrangerMessageEvent must be an instance of OnlineMessageSource.Incoming.FromStranger" }
    }

    public override val stranger: Stranger get() = sender
    public override val bot: Bot get() = super.bot
    public override val subject: Stranger get() = sender
    public override val senderName: String get() = sender.nick
    public override val source: OnlineMessageSource.Incoming.FromStranger get() = message.source as OnlineMessageSource.Incoming.FromStranger

    public override fun toString(): String = "StrangerMessageEvent(sender=${sender.id}, message=$message)"
}

/**
 * 消息事件的公共抽象父类, 保留将来使用. 这是内部 API, 请不要使用.
 */
@MiraiInternalApi
public abstract class AbstractMessageEvent : MessageEvent, AbstractEvent()
