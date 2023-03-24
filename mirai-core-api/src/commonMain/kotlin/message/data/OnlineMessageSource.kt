/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageUtils")
@file:Suppress("unused", "INAPPLICABLE_JVM_NAME", "DEPRECATION_ERROR", "UnUsedImport")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.NotStableForInheritance
import net.mamoe.mirai.utils.safeCast
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

/**
 * 从服务器接收的在线消息的 [MessageSource].
 *
 * 对比 [OfflineMessageSource], [OnlineMessageSource] 拥有完整的信息:
 * - 可获取 [sender] 和 [target] 的 [ContactOrBot] 对象
 * - 可获取有关 [bot] 对象.
 *
 * 此消息源一定 "指向" 一条存在于服务器上的消息, 但由于服务器消息可能已经被撤回, 对此消息源执行[撤回][MessageSource.recall] 仍然可能会失败.
 *
 * ### 来源
 * - 当 bot 主动发送消息时, 产生 (由协议模块主动构造) [OnlineMessageSource.Outgoing]
 * - 当 bot 接收消息时, 产生 (由协议模块根据服务器的提供的信息构造) [OnlineMessageSource.Incoming]
 *
 * #### 机器人主动发送消息
 * 当机器人 [主动发出消息][Member.sendMessage], 将会得到一个 [消息回执][MessageReceipt].
 * 此回执的 [消息源][MessageReceipt.source] 即为一个 [外向消息源][OnlineMessageSource.Outgoing], 代表着刚刚发出的那条消息的来源.
 *
 * #### 机器人接受消息
 * 当机器人接收一条消息 [MessageEvent], 这条消息包含一个 [内向消息源][OnlineMessageSource.Incoming], 代表着接收到的这条消息的来源.
 *
 *
 * ### 实现
 * 此类的所有子类都有协议模块实现. 不要自行实现它们, 否则将无法发送
 *
 * @see OnlineMessageSource.toOffline 转为 [OfflineMessageSource]
 */
public sealed class OnlineMessageSource : MessageSource() {
    public companion object Key : AbstractMessageKey<OnlineMessageSource>({ it.safeCast() })

    /**
     * @see botId
     */
    public abstract val bot: Bot
    final override val botId: Long get() = bot.id

    /**
     * 消息发送人. 可能为 [机器人][Bot] 或 [好友][Friend] 或 [群员][Member].
     * 即类型必定为 [Bot], [Friend] 或 [Member]
     */
    public abstract val sender: ContactOrBot

    /**
     * 消息发送目标. 可能为 [机器人][Bot] 或 [好友][Friend] 或 [群][Group].
     * 即类型必定为 [Bot], [Friend] 或 [Group]
     */
    public abstract val target: ContactOrBot

    /**
     * 消息主体. 群消息时为 [Group]. 好友消息时为 [Friend], 临时消息为 [Member]
     * 不论是机器人接收的消息还是发送的消息, 此属性都指向机器人能进行回复的目标.
     */
    public abstract val subject: Contact

    /*
     * 以下子类型仅是覆盖了 [target], [subject], [sender] 等的类型
     */

    /**
     * 由 [机器人主动发送消息][Contact.sendMessage] 产生的 [MessageSource], 可通过 [MessageReceipt] 获得.
     */
    public sealed class Outgoing : OnlineMessageSource() {
        public companion object Key :
            AbstractPolymorphicMessageKey<OnlineMessageSource, Outgoing>(OnlineMessageSource, { it.safeCast() })

        public abstract override val sender: Bot
        public abstract override val target: Contact

        public final override val fromId: Long get() = sender.id
        public final override val targetId: Long get() = target.id

        @NotStableForInheritance
        public abstract class ToFriend @MiraiInternalApi constructor() : Outgoing() {
            public companion object Key : AbstractPolymorphicMessageKey<Outgoing, ToFriend>(Outgoing, { it.safeCast() })

            public abstract override val target: Friend
            public final override val subject: Friend get() = target

            final override val kind: MessageSourceKind get() = MessageSourceKind.FRIEND

            final override fun toString(): String {
                return "[mirai:source:ids=${ids.contentToString()}, internalIds=${internalIds.contentToString()}, from $fromId to friend $targetId at $time]"
            }
        }

        @NotStableForInheritance
        public abstract class ToStranger @MiraiInternalApi constructor() : Outgoing() {
            public companion object Key :
                AbstractPolymorphicMessageKey<Outgoing, ToStranger>(Outgoing, { it.safeCast() })

            public abstract override val target: Stranger
            public final override val subject: Stranger get() = target

            final override val kind: MessageSourceKind get() = MessageSourceKind.STRANGER

            final override fun toString(): String {
                return "[mirai:source:ids=${ids.contentToString()}, internalIds=${internalIds.contentToString()}, from $fromId to stranger $targetId at $time]"
            }
        }

        @NotStableForInheritance
        public abstract class ToTemp @MiraiInternalApi constructor() : Outgoing() {
            public companion object Key : AbstractPolymorphicMessageKey<Outgoing, ToTemp>(Outgoing, { it.safeCast() })

            public abstract override val target: Member
            public val group: Group get() = target.group
            public final override val subject: Member get() = target

            final override val kind: MessageSourceKind get() = MessageSourceKind.TEMP

            final override fun toString(): String {
                return "[mirai:source:ids=${ids.contentToString()}, internalIds=${internalIds.contentToString()}, from $fromId to group temp $targetId at $time]"
            }
        }

        @NotStableForInheritance
        public abstract class ToGroup @MiraiInternalApi constructor() : Outgoing() {
            public companion object Key : AbstractPolymorphicMessageKey<Outgoing, ToGroup>(Outgoing, { it.safeCast() })

            public abstract override val target: Group
            public final override val subject: Group get() = target

            final override val kind: MessageSourceKind get() = MessageSourceKind.GROUP

            final override fun toString(): String {
                return "[mirai:source:ids=${ids.contentToString()}, internalIds=${internalIds.contentToString()}, from $fromId to group $targetId at $time]"
            }
        }
    }

    /**
     * 接收到的一条消息的 [MessageSource]
     */
    public sealed class Incoming : OnlineMessageSource() {
        /**
         * 当 [sender] 为 [bot] 自身时为 bot 的对应表示 (如: [Bot.asFriend], [Bot.asStranger], [Group.botAsMember])
         */
        public abstract override val sender: User

        /// NOTE: DONT use final to avoid contact not available
        public override val fromId: Long get() = sender.id
        public override val targetId: Long get() = target.id

        @NotStableForInheritance
        public abstract class FromFriend @MiraiInternalApi constructor() : Incoming() {
            public companion object Key :
                AbstractPolymorphicMessageKey<Incoming, FromFriend>(Incoming, { it.safeCast() })

            public abstract override val subject: Friend

            /**
             * 当 [sender] 为 [bot] 自身时为 [Bot.asFriend]
             */
            public abstract override val sender: Friend
            public abstract override val target: ContactOrBot

            @JvmName("getTarget")
            @Deprecated("For ABI compatibility", level = DeprecationLevel.HIDDEN)
            public fun getTargetLegacy(): Bot {
                if (targetId == bot.id) return subject.bot

                error("Message target isn't bot; $this")
            }

            final override val kind: MessageSourceKind get() = MessageSourceKind.FRIEND

            final override fun toString(): String {
                return "[mirai:source:ids=${ids.contentToString()}, internalIds=${internalIds.contentToString()}, from friend $fromId to $targetId at $time]"
            }
        }

        @NotStableForInheritance
        public abstract class FromTemp @MiraiInternalApi constructor() : Incoming() {
            public companion object Key :
                AbstractPolymorphicMessageKey<Incoming, FromTemp>(Incoming, { it.safeCast() })

            /**
             * 当 [sender] 为 [bot] 自身时为 [Group.botAsMember]
             */
            public abstract override val sender: Member
            public abstract override val subject: Member
            public abstract override val target: ContactOrBot

            public inline val group: Group get() = subject.group

            @JvmName("getTarget")
            @Deprecated("For ABI compatibility", level = DeprecationLevel.HIDDEN)
            public fun getTargetLegacy(): Bot {
                if (targetId == bot.id) return subject.bot

                error("Message target isn't bot; $this")
            }

            final override val kind: MessageSourceKind get() = MessageSourceKind.TEMP

            final override fun toString(): String {
                return "[mirai:source:ids=${ids.contentToString()}, internalIds=${internalIds.contentToString()}, from group temp $fromId to $targetId at $time]"
            }
        }

        @NotStableForInheritance
        public abstract class FromStranger @MiraiInternalApi constructor() : Incoming() {
            public companion object Key :
                AbstractPolymorphicMessageKey<Incoming, FromStranger>(Incoming, { it.safeCast() })

            /**
             * 当 [sender] 为 [bot] 自身时为 [Bot.asStranger]
             */
            public abstract override val sender: Stranger

            public abstract override val subject: Stranger
            public abstract override val target: ContactOrBot

            @JvmName("getTarget")
            @Deprecated("For ABI compatibility", level = DeprecationLevel.HIDDEN)
            public fun getTargetLegacy(): Bot {
                if (targetId == bot.id) return subject.bot

                error("Message target isn't bot; $this")
            }

            final override val kind: MessageSourceKind get() = MessageSourceKind.STRANGER

            final override fun toString(): String {
                return "[mirai:source:ids=${ids.contentToString()}, internalIds=${internalIds.contentToString()}, from stranger $fromId to $targetId at $time]"
            }
        }

        @NotStableForInheritance
        public abstract class FromGroup @MiraiInternalApi constructor() : Incoming() {
            public companion object Key :
                AbstractPolymorphicMessageKey<Incoming, FromGroup>(Incoming, { it.safeCast() })

            /**
             * 当 [sender] 为 [bot] 自身时为 [Group.botAsMember]
             */
            public abstract override val sender: Member
            public override val subject: Group get() = sender.group
            public final override val target: Group get() = subject
            public inline val group: Group get() = subject

            final override val kind: MessageSourceKind get() = MessageSourceKind.GROUP

            final override fun toString(): String {
                return "[mirai:source:ids=${ids.contentToString()}, internalIds=${internalIds.contentToString()}, from group $fromId to $targetId at $time]"
            }
        }

        public companion object Key :
            AbstractPolymorphicMessageKey<OnlineMessageSource, FromTemp>(OnlineMessageSource, { it.safeCast() })
    }
}
