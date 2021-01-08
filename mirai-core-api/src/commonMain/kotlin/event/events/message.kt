/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("BotEventsKt")
@file:Suppress("unused", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "RESULT_CLASS_IN_RETURN_TYPE")

package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.ImageUploadEvent.Failed
import net.mamoe.mirai.event.events.ImageUploadEvent.Succeed
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.isContextIdenticalWith
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.MiraiInternalApi
import kotlin.internal.InlineOnly


// region MessagePreSendEvent

/**
 * 在发送消息前广播的事件. 可被 [取消][CancellableEvent.cancel].
 *
 * 此事件总是在 [MessagePostSendEvent] 之前广播.
 *
 * 当 [MessagePreSendEvent] 被 [取消][CancellableEvent.cancel] 后:
 * - [MessagePostSendEvent] 不会广播
 * - 消息不会发送.
 * - [Contact.sendMessage] 会抛出异常 [EventCancelledException]
 *
 * @see Contact.sendMessage 发送消息. 为广播这个事件的唯一途径
 */
public sealed class MessagePreSendEvent : BotEvent, BotActiveEvent, AbstractEvent(), CancellableEvent {
    /** 发信目标. */
    public abstract val target: Contact
    public final override val bot: Bot get() = target.bot

    /** 待发送的消息. 修改后将会同时应用于发送. */
    public abstract var message: Message
}

/**
 * 在发送群消息前广播的事件.
 * @see MessagePreSendEvent
 */
public data class GroupMessagePreSendEvent @MiraiInternalApi constructor(
    /** 发信目标. */
    public override val target: Group,
    /** 待发送的消息. 修改后将会同时应用于发送. */
    public override var message: Message
) : MessagePreSendEvent()

/**
 * 在发送好友或群临时会话消息前广播的事件.
 * @see MessagePreSendEvent
 */
public sealed class UserMessagePreSendEvent : MessagePreSendEvent() {
    /** 发信目标. */
    public abstract override val target: User
}

/**
 * 在发送好友消息前广播的事件.
 * @see MessagePreSendEvent
 */
public data class FriendMessagePreSendEvent @MiraiInternalApi constructor(
    /** 发信目标. */
    public override val target: Friend,
    /** 待发送的消息. 修改后将会同时应用于发送. */
    public override var message: Message
) : UserMessagePreSendEvent()

/**
 * 在发送群临时会话消息前广播的事件.
 * @see MessagePreSendEvent
 */
public data class TempMessagePreSendEvent @MiraiInternalApi constructor(
    /** 发信目标. */
    public override val target: Member,
    /** 待发送的消息. 修改后将会同时应用于发送. */
    public override var message: Message
) : UserMessagePreSendEvent() {
    public val group: Group get() = target.group
}

/**
 * 在发送陌生人消息前广播的事件.
 * @see MessagePreSendEvent
 */
public data class StrangerMessagePreSendEvent @MiraiInternalApi constructor(
    /** 发信目标. */
    public override val target: Stranger,
    /** 待发送的消息. 修改后将会同时应用于发送. */
    public override var message: Message
) : UserMessagePreSendEvent()

// endregion

// region MessagePostSendEvent

/**
 * 在发送消息后广播的事件, 总是在 [MessagePreSendEvent] 之后广播.
 *
 * 只要 [MessagePreSendEvent] 未被 [取消][CancellableEvent.cancel], [MessagePostSendEvent] 就一定会被广播, 并携带 [发送时产生的异常][MessagePostSendEvent.exception] (如果有).
 *
 * 在此事件广播前, 消息一定已经发送成功, 或产生一个异常.
 *
 * @see Contact.sendMessage 发送消息. 为广播这个事件的唯一途径
 * @see MessagePreSendEvent
 */
public sealed class MessagePostSendEvent<C : Contact> : BotEvent, BotActiveEvent, AbstractEvent() {
    /** 发信目标. */
    public abstract val target: C
    public final override val bot: Bot get() = target.bot

    /** 待发送的消息. 此为 [MessagePreSendEvent.message] 的最终值. */
    public abstract val message: MessageChain

    /**
     * 发送消息时抛出的异常. `null` 表示消息成功发送.
     * @see result
     */
    public abstract val exception: Throwable?

    /**
     * 发送消息成功时的回执. `null` 表示消息发送失败.
     * @see result
     */
    public abstract val receipt: MessageReceipt<C>?
}

/**
 * 获取指代这条已经发送的消息的 [MessageSource]. 若消息发送失败, 返回 `null`
 * @see MessagePostSendEvent.sourceResult
 */
@get:JvmSynthetic
public inline val MessagePostSendEvent<*>.source: MessageSource?
    get() = receipt?.source

/**
 * 获取指代这条已经发送的消息的 [MessageSource], 并包装为 [kotlin.Result]
 * @see MessagePostSendEvent.result
 */
@get:JvmSynthetic
public inline val MessagePostSendEvent<*>.sourceResult: Result<MessageSource>
    get() = result.map { it.source }

/**
 * 在此消息发送成功时返回 `true`.
 * @see MessagePostSendEvent.exception
 * @see MessagePostSendEvent.result
 */
@get:JvmSynthetic
public inline val MessagePostSendEvent<*>.isSuccess: Boolean
    get() = exception == null

/**
 * 在此消息发送失败时返回 `true`.
 * @see MessagePostSendEvent.exception
 * @see MessagePostSendEvent.result
 */
@get:JvmSynthetic
public inline val MessagePostSendEvent<*>.isFailure: Boolean
    get() = exception != null

/**
 * 将 [MessagePostSendEvent.exception] 与 [MessagePostSendEvent.receipt] 表示为 [Result]
 */
@InlineOnly
public inline val <C : Contact> MessagePostSendEvent<C>.result: Result<MessageReceipt<C>>
    get() = exception.let { exception -> if (exception != null) Result.failure(exception) else Result.success(receipt!!) }

/**
 * 在群消息发送后广播的事件.
 * @see MessagePostSendEvent
 */
public data class GroupMessagePostSendEvent @MiraiInternalApi constructor(
    /** 发信目标. */
    public override val target: Group,
    /** 待发送的消息. 此为 [MessagePreSendEvent.message] 的最终值. */
    public override val message: MessageChain,
    /**
     * 发送消息时抛出的异常. `null` 表示消息成功发送.
     * @see result
     */
    public override val exception: Throwable?,
    /**
     * 发送消息成功时的回执. `null` 表示消息发送失败.
     * @see result
     */
    public override val receipt: MessageReceipt<Group>?
) : MessagePostSendEvent<Group>()

/**
 * 在好友或群临时会话消息发送后广播的事件.
 * @see MessagePostSendEvent
 */
public sealed class UserMessagePostSendEvent<C : User> : MessagePostSendEvent<C>()

/**
 * 在好友消息发送后广播的事件.
 * @see MessagePostSendEvent
 */
public data class FriendMessagePostSendEvent @MiraiInternalApi constructor(
    /** 发信目标. */
    public override val target: Friend,
    /** 待发送的消息. 此为 [MessagePreSendEvent.message] 的最终值. */
    public override val message: MessageChain,
    /**
     * 发送消息时抛出的异常. `null` 表示消息成功发送.
     * @see result
     */
    public override val exception: Throwable?,
    /**
     * 发送消息成功时的回执. `null` 表示消息发送失败.
     * @see result
     */
    public override val receipt: MessageReceipt<Friend>?
) : UserMessagePostSendEvent<Friend>()

/**
 * 在群临时会话消息发送后广播的事件.
 * @see MessagePostSendEvent
 */
public data class TempMessagePostSendEvent @MiraiInternalApi constructor(
    /** 发信目标. */
    public override val target: Member,
    /** 待发送的消息. 此为 [MessagePreSendEvent.message] 的最终值. */
    public override val message: MessageChain,
    /**
     * 发送消息时抛出的异常. `null` 表示消息成功发送.
     * @see result
     */
    public override val exception: Throwable?,
    /**
     * 发送消息成功时的回执. `null` 表示消息发送失败.
     * @see result
     */
    public override val receipt: MessageReceipt<Member>?
) : UserMessagePostSendEvent<Member>() {
    public val group: Group get() = target.group
}

/**
 * 在陌生人消息发送后广播的事件.
 * @see MessagePostSendEvent
 */
public data class StrangerMessagePostSendEvent @MiraiInternalApi constructor(
    /** 发信目标. */
    public override val target: Stranger,
    /** 待发送的消息. 此为 [MessagePreSendEvent.message] 的最终值. */
    public override val message: MessageChain,
    /**
     * 发送消息时抛出的异常. `null` 表示消息成功发送.
     * @see result
     */
    public override val exception: Throwable?,
    /**
     * 发送消息成功时的回执. `null` 表示消息发送失败.
     * @see result
     */
    public override val receipt: MessageReceipt<Stranger>?
) : UserMessagePostSendEvent<Stranger>()

// endregion

// region MessageRecallEvent

/**
 * 消息撤回事件. 可是任意消息被任意人撤回.
 *
 * @see Contact.recallMessage 撤回消息. 为广播这个事件的唯一途径
 */
public sealed class MessageRecallEvent : BotEvent, AbstractEvent() {
    /**
     * 消息原发送人
     */
    public abstract val authorId: Long

    /**
     * 消息原发送人, 为 `null` 表示原消息发送人已经不是 bot 的好友或已经被移出群。
     */
    public abstract val author: UserOrBot?

    /**
     * 消息 ids.
     * @see MessageSource.ids
     */
    public abstract val messageIds: IntArray

    /**
     * 消息内部 ids.
     * @see MessageSource.ids
     */
    public abstract val messageInternalIds: IntArray

    /**
     * 原发送时间
     */
    public abstract val messageTime: Int // seconds

    /**
     * 好友消息撤回事件
     */
    public data class FriendRecall @MiraiInternalApi public constructor(
        public override val bot: Bot,
        public override val messageIds: IntArray,
        public override val messageInternalIds: IntArray,
        public override val messageTime: Int,
        /**
         * 撤回操作人, 好友的 [User.id]
         */
        public val operatorId: Long
    ) : MessageRecallEvent(), Packet {
        @Deprecated("Binary compatibility.", level = DeprecationLevel.HIDDEN)
        public fun getOperator(): Long = operatorId

        /**
         * 撤回操作人. 为 `null` 表示该用户已经不是 bot 的好友
         */
        public val operator: Friend? get() = bot.getFriend(operatorId)

        /**
         * 消息原发送人, 等于 [operator]
         */
        override val author: Friend? get() = operator

        public override val authorId: Long
            get() = operatorId

        @Suppress("DuplicatedCode")
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FriendRecall

            if (bot != other.bot) return false
            if (!messageIds.contentEquals(other.messageIds)) return false
            if (!messageInternalIds.contentEquals(other.messageInternalIds)) return false
            if (messageTime != other.messageTime) return false
            if (operatorId != other.operatorId) return false

            return true
        }

        override fun hashCode(): Int {
            var result = bot.hashCode()
            result = 31 * result + messageIds.contentHashCode()
            result = 31 * result + messageInternalIds.contentHashCode()
            result = 31 * result + messageTime
            result = 31 * result + operatorId.hashCode()
            return result
        }
    }

    /**
     * 群消息撤回事件.
     */
    public data class GroupRecall @MiraiInternalApi constructor(
        public override val bot: Bot,
        public override val authorId: Long,
        public override val messageIds: IntArray,
        public override val messageInternalIds: IntArray,
        public override val messageTime: Int,
        /**
         * 操作人. 为 null 时则为 [Bot] 操作.
         */
        public override val operator: Member?,
        public override val group: Group
    ) : MessageRecallEvent(), GroupOperableEvent, Packet {
        override val author: NormalMember? get() = group[authorId]

        @Suppress("DuplicatedCode")
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GroupRecall

            if (bot != other.bot) return false
            if (authorId != other.authorId) return false
            if (!messageIds.contentEquals(other.messageIds)) return false
            if (!messageInternalIds.contentEquals(other.messageInternalIds)) return false
            if (messageTime != other.messageTime) return false
            if (operator != other.operator) return false
            if (group != other.group) return false

            return true
        }

        override fun hashCode(): Int {
            var result = bot.hashCode()
            result = 31 * result + authorId.hashCode()
            result = 31 * result + messageIds.contentHashCode()
            result = 31 * result + messageInternalIds.contentHashCode()
            result = 31 * result + messageTime
            result = 31 * result + (operator?.hashCode() ?: 0)
            result = 31 * result + group.hashCode()
            return result
        }
    }
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
@Deprecated("Binary compatibility", level = DeprecationLevel.HIDDEN)
public val MessageRecallEvent.GroupRecall.author: Member
    get() = if (authorId == bot.id) group.botAsMember else group.getOrFail(authorId)

public val MessageRecallEvent.FriendRecall.isByBot: Boolean get() = this.operatorId == bot.id
// val MessageRecallEvent.GroupRecall.isByBot: Boolean get() = (this as GroupOperableEvent).isByBot
// no need

public val MessageRecallEvent.isByBot: Boolean
    get() = when (this) {
        is MessageRecallEvent.FriendRecall -> this.isByBot
        is MessageRecallEvent.GroupRecall -> (this as GroupOperableEvent).isByBot
    }

// endregion

// region ImageUploadEvent

/**
 * 图片上传前. 可以阻止上传.
 *
 * 此事件总是在 [ImageUploadEvent] 之前广播.
 * 若此事件被取消, [ImageUploadEvent] 不会广播.
 *
 * @see Contact.uploadImage 上传图片. 为广播这个事件的唯一途径
 */
public data class BeforeImageUploadEvent @MiraiInternalApi constructor(
    public val target: Contact,
    public val source: ExternalResource
) : BotEvent, BotActiveEvent, AbstractEvent(), CancellableEvent {
    public override val bot: Bot
        get() = target.bot
}

/**
 * 图片上传完成.
 *
 * 此事件总是在 [BeforeImageUploadEvent] 之后广播.
 * 若 [BeforeImageUploadEvent] 被取消, 此事件不会广播.
 *
 * @see Contact.uploadImage 上传图片. 为广播这个事件的唯一途径
 *
 * @see Succeed
 * @see Failed
 */
public sealed class ImageUploadEvent : BotEvent, BotActiveEvent, AbstractEvent() {
    public abstract val target: Contact
    public abstract val source: ExternalResource
    public override val bot: Bot
        get() = target.bot

    public data class Succeed @MiraiInternalApi constructor(
        override val target: Contact,
        override val source: ExternalResource,
        val image: Image
    ) : ImageUploadEvent()

    public data class Failed @MiraiInternalApi constructor(
        override val target: Contact,
        override val source: ExternalResource,
        val errno: Int,
        val message: String
    ) : ImageUploadEvent()
}

// endregion

// region MessageEvent

/**
 * 一个 (收到的) 消息事件.
 *
 * 它是一个 [BotEvent], 因此可以被 [监听][EventChannel.subscribe]
 *
 * @see isContextIdenticalWith 判断语境相同
 */
public interface MessageEvent : Event, Packet, BotPassiveEvent {
    /**
     * 与这个消息事件相关的 [Bot]
     */
    public override val bot: Bot

    /**
     * 消息事件主体.
     *
     * - 对于好友消息, 这个属性为 [Friend] 的实例, 与 [sender] 引用相同;
     * - 对于临时会话消息, 这个属性为 [Member] 的实例, 与 [sender] 引用相同;
     * - 对于陌生人消息, 这个属性为 [Stranger] 的实例, 与 [sender] 引用相同
     * - 对于群消息, 这个属性为 [Group] 的实例, 与 [GroupMessageEvent.group] 引用相同
     * - 对于其他客户端消息, 这个属性为 [OtherClient] 的实例, 与 [OtherClientMessageEvent.client] 引用相同
     *
     * 在回复消息时, 可通过 [subject] 作为回复对象
     */
    public val subject: Contact

    /**
     * 发送人.
     *
     * 在私聊消息时为相关 [User] 的实例, 在群消息时为 [Member] 的实例, 在其他客户端消息时为 [Bot.asFriend]
     */
    public val sender: User

    /**
     * 发送人名称
     */
    public val senderName: String

    /**
     * 消息内容.
     *
     * 第一个元素一定为 [MessageSource], 存储此消息的发送人, 发送时间, 收信人, 消息 ids 等数据.
     * 随后的元素为拥有顺序的真实消息内容.
     */
    public val message: MessageChain

    /** 消息发送时间 (由服务器提供, 可能与本地有时差) */
    public val time: Int

    /**
     * 消息源. 来自 [message] 的第一个元素,
     */
    public val source: OnlineMessageSource.Incoming get() = message.source as OnlineMessageSource.Incoming
}

/**
 * 来自 [User] 的消息
 *
 * @see FriendMessageEvent
 * @see TempMessageEvent
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
 * 机器人收到的好友消息的事件
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
    public override val source: OnlineMessageSource.Incoming.FromFriend get() = message.source as OnlineMessageSource.Incoming.FromFriend

    public override fun toString(): String = "OtherClientMessageEvent(client=${client.platform}, message=$message)"
}

/**
 * 来自一个可以知道其 [Group] 的用户消息
 *
 * @see FriendMessageEvent
 * @see TempMessageEvent
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
public class TempMessageEvent(
    public override val sender: Member,
    public override val message: MessageChain,
    public override val time: Int
) : AbstractMessageEvent(), GroupAwareMessageEvent, UserMessageEvent {
    init {
        val source = message[MessageSource] ?: error("Cannot find MessageSource from message")
        check(source is OnlineMessageSource.Incoming.FromTemp) { "source provided to a TempMessageEvent must be an instance of OnlineMessageSource.Incoming.FromTemp" }
    }

    public override val bot: Bot get() = sender.bot
    public override val subject: Member get() = sender
    public override val group: Group get() = sender.group
    public override val senderName: String get() = sender.nameCardOrNick
    public override val source: OnlineMessageSource.Incoming.FromTemp get() = message.source as OnlineMessageSource.Incoming.FromTemp

    public override fun toString(): String =
        "TempMessageEvent(sender=${sender.id} from group(${sender.group.id}), message=$message)"
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

@MiraiInternalApi
public abstract class AbstractMessageEvent : MessageEvent, AbstractEvent()

// endregion

// region MessageSyncEvent

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

// endregion