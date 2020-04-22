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
@file:Suppress("NOTHING_TO_INLINE", "unused", "INAPPLICABLE_JVM_NAME", "INVISIBLE_MEMBER")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.PlannedRemoval
import net.mamoe.mirai.utils.SinceMirai
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic


/**
 * 在线消息的 [MessageSource].
 * 拥有对象化的 [sender], [target], 也可以直接 [recall] 和 [quote]
 *
 * ### 来源
 * **必定是一个发出去的消息或接收到的消息的 [MessageChain] 中的一个元数据 [MessageMetadata].**
 *
 * #### 机器人主动发送消息
 * 当机器人 [主动发出消息][Member.sendMessage], 将会得到一个 [消息回执][MessageReceipt].
 * 此回执的 [消息源][MessageReceipt.source] 即为一个 [外向消息源][OnlineMessageSource.Outgoing], 代表着刚刚发出的那条消息的来源.
 *
 * #### 机器人接受消息
 * 当机器人接收一条消息 [ContactMessage], 这条消息包含一个 [内向消息源][OnlineMessageSource.Incoming], 代表着接收到的这条消息的来源.
 */
@SinceMirai("0.33.0")
@OptIn(MiraiExperimentalAPI::class)
sealed class OnlineMessageSource : MessageSource() {
    companion object Key : Message.Key<OnlineMessageSource> {
        override val typeName: String get() = "OnlineMessageSource"
    }

    /**
     * 消息发送人. 可能为 [机器人][Bot] 或 [好友][QQ] 或 [群员][Member].
     * 即类型必定为 [Bot], [QQ] 或 [Member]
     */
    abstract val sender: ContactOrBot

    /**
     * 消息发送目标. 可能为 [机器人][Bot] 或 [好友][QQ] 或 [群][Group].
     * 即类型必定为 [Bot], [QQ] 或 [Group]
     */
    abstract val target: ContactOrBot

    /**
     * 消息主体. 群消息时为 [Group]. 好友消息时为 [QQ], 临时消息为 [Member]
     * 不论是机器人接收的消息还是发送的消息, 此属性都指向机器人能进行回复的目标.
     */
    abstract val subject: Contact

    /**
     * 由 [机器人主动发送消息][Contact.sendMessage] 产生的 [MessageSource]
     */
    sealed class Outgoing : OnlineMessageSource() {
        companion object Key : Message.Key<Outgoing> {
            override val typeName: String get() = "OnlineMessageSource.Outgoing"
        }

        abstract override val sender: Bot
        abstract override val target: Contact

        final override val fromId: Long get() = sender.id
        final override val targetId: Long get() = target.id

        abstract class ToFriend : Outgoing() {
            companion object Key : Message.Key<ToFriend> {
                override val typeName: String get() = "OnlineMessageSource.Outgoing.ToFriend"
            }

            abstract override val target: Friend
            final override val subject: Friend get() = target
            //  final override fun toString(): String = "OnlineMessageSource.ToFriend(target=${target.id})"
        }

        abstract class ToTemp : Outgoing() {
            companion object Key : Message.Key<ToTemp> {
                override val typeName: String get() = "OnlineMessageSource.Outgoing.ToTemp"
            }

            abstract override val target: Member
            val group: Group get() = target.group
            final override val subject: Member get() = target
        }

        abstract class ToGroup : Outgoing() {
            companion object Key : Message.Key<ToGroup> {
                override val typeName: String get() = "OnlineMessageSource.Outgoing.ToGroup"
            }

            abstract override val target: Group
            final override val subject: Group get() = target
            //  final override fun toString(): String = "OnlineMessageSource.ToGroup(group=${target.id})"
        }
    }

    /**
     * 接收到的一条消息的 [MessageSource]
     */
    sealed class Incoming : OnlineMessageSource() {
        companion object Key : Message.Key<Incoming> {
            override val typeName: String get() = "OnlineMessageSource.Incoming"
        }

        abstract override val sender: User

        final override val fromId: Long get() = sender.id
        final override val targetId: Long get() = target.id

        abstract class FromFriend : Incoming() {
            companion object Key : Message.Key<FromFriend> {
                override val typeName: String get() = "OnlineMessageSource.Incoming.FromFriend"
            }

            abstract override val sender: Friend
            final override val subject: Friend get() = sender
            final override val target: Bot get() = sender.bot
            // final override fun toString(): String = "OnlineMessageSource.FromFriend(from=${sender.id})"
        }

        abstract class FromTemp : Incoming() {
            companion object Key : Message.Key<FromTemp> {
                override val typeName: String get() = "OnlineMessageSource.Incoming.FromTemp"
            }

            abstract override val sender: Member
            inline val group: Group get() = sender.group
            final override val subject: Member get() = sender
            final override val target: Bot get() = sender.bot
        }

        abstract class FromGroup : Incoming() {
            companion object Key : Message.Key<FromGroup> {
                override val typeName: String get() = "OnlineMessageSource.Incoming.FromGroup"
            }

            abstract override val sender: Member
            final override val subject: Group get() = sender.group
            final override val target: Group get() = group
            inline val group: Group get() = sender.group
        }


        //////////////////////////////////
        //// FOR BINARY COMPATIBILITY ////
        //////////////////////////////////


        @PlannedRemoval("1.0.0")
        @Deprecated("for binary compatibility until 1.0.0", level = DeprecationLevel.HIDDEN)
        @get:JvmName("target")
        @get:JvmSynthetic
        final override val target2: Any
            get() = target
    }

    @PlannedRemoval("1.0.0")
    @Deprecated("for binary compatibility until 1.0.0", level = DeprecationLevel.HIDDEN)
    @get:JvmName("target")
    @get:JvmSynthetic
    open val target2: Any
        get() = target

    @PlannedRemoval("1.0.0")
    @Deprecated("for binary compatibility until 1.0.0", level = DeprecationLevel.HIDDEN)
    @get:JvmName("sender")
    @get:JvmSynthetic
    open val sender2: Any
        get() = sender
}

@SinceMirai("0.39.0")
@JvmName("toOfflineMessageSource")
fun OnlineMessageSource.toOffline(): OfflineMessageSource =
    OfflineMessageSourceByOnline(this)

internal class OfflineMessageSourceByOnline(
    private val onlineMessageSource: OnlineMessageSource
) : OfflineMessageSource() {
    override val kind: Kind
        get() = when {
            onlineMessageSource.isAboutGroup() -> Kind.GROUP
            onlineMessageSource.isAboutFriend() -> Kind.FRIEND
            onlineMessageSource.isAboutTemp() -> Kind.TEMP
            else -> error("stub")
        }
    override val bot: Bot get() = onlineMessageSource.bot
    override val id: Int get() = onlineMessageSource.id
    override val internalId: Int get() = onlineMessageSource.internalId
    override val time: Int get() = onlineMessageSource.time
    override val fromId: Long get() = onlineMessageSource.fromId
    override val targetId: Long get() = onlineMessageSource.targetId
    override val originalMessage: MessageChain get() = onlineMessageSource.originalMessage
}