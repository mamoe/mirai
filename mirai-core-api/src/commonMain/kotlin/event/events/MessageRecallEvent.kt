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
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.utils.MiraiInternalApi


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
     * 消息原发送人.
     */
    public abstract val author: UserOrBot

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
     * 原发送时间戳, 单位为秒.
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
        public val operatorId: Long,
        public val operator: Friend,
    ) : MessageRecallEvent(), Packet {
        /**
         * 消息原发送人, 等于 [operator]
         */
        override val author: Friend get() = operator

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
            if (operator != other.operator) return false

            return true
        }

        override fun hashCode(): Int {
            var result = bot.hashCode()
            result = 31 * result + messageIds.contentHashCode()
            result = 31 * result + messageInternalIds.contentHashCode()
            result = 31 * result + messageTime
            result = 31 * result + operatorId.hashCode()
            result = 31 * result + operator.hashCode()
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
        public override val group: Group,
        public override val author: NormalMember,
    ) : MessageRecallEvent(), GroupOperableEvent, Packet {

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
            if (author != other.author) return false

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
            result = 31 * result + author.hashCode()
            return result
        }
    }
}

public val MessageRecallEvent.FriendRecall.isByBot: Boolean get() = this.operatorId == bot.id
// val MessageRecallEvent.GroupRecall.isByBot: Boolean get() = (this as GroupOperableEvent).isByBot
// no need

public val MessageRecallEvent.isByBot: Boolean
    get() = when (this) {
        is MessageRecallEvent.FriendRecall -> this.isByBot
        is MessageRecallEvent.GroupRecall -> (this as GroupOperableEvent).isByBot
    }
