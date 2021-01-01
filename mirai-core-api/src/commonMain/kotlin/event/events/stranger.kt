package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Stranger
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.message.action.Nudge
import net.mamoe.mirai.utils.MiraiExperimentalApi

/**
 * 新增陌生人的事件
 *
 */
public data class StrangerAddEvent internal constructor(
    /**
     * 新的陌生人. 已经添加到 [Bot.strangers]
     */
    public override val stranger: Stranger
) : StrangerEvent, Packet, AbstractEvent()


/**
 * 陌生人关系改变事件
 *
 */
public abstract class StrangerRelationChangeEvent(
    public override val stranger: Stranger
) : StrangerEvent, Packet, AbstractEvent() {
    /**
     * 主动删除陌生人或陌生人被删除的事件
     *
     * 除主动删除外，此事件为惰性广播，无法确保实时性
     * 目前被动删除仅会在陌生人二次添加时才会进行广播
     */
    public class Deleted(
        /**
         * 被删除的陌生人
         */
        stranger: Stranger
    ) : StrangerRelationChangeEvent(stranger)

    /**
     * 与陌生人成为好友
     */
    public class Friended(
        /**
         * 成为好友的陌生人
         *
         * 成为好友后该陌生人会从陌生人列表中删除
         */
        public override val stranger: Stranger,
        /**
         * 成为好友后的实例
         *
         * 已经添加到Bot的好友列表中
         */
        public val friend: Friend
    ) : StrangerRelationChangeEvent(stranger)

}

/**
 * 在 [Stranger] 与 [Bot] 的对话中, [Stranger] 被 [戳][Nudge] 事件
 *
 * 注: 此事件仅可能在私聊中发生
 */
@MiraiExperimentalApi
public sealed class StrangerNudgedEvent : AbstractEvent(), StrangerEvent, Packet {
    /**
     * 戳一戳的发起人, 为 [Bot] 的某一好友, 或是 [Bot.asFriend]
     */
    public abstract val from: Stranger

    /**
     * 戳一戳的动作名称
     */
    public abstract val action: String

    /**
     * 戳一戳中设置的自定义后缀
     */
    public abstract val suffix: String

    /** 在 [Bot] 与 [Stranger] 的对话中 [Stranger] 戳了自己事件 */
    @MiraiExperimentalApi
    public data class NudgedByHimself internal constructor(
        override val stranger: Stranger,
        override val action: String,
        override val suffix: String
    ) : StrangerNudgedEvent() {
        override fun toString(): String {
            return "StrangerNudgedEvent.NudgedByHimself(stranger=$stranger, action=$action, suffix=$suffix)"
        }

        override val from: Stranger
            get() = stranger
    }

    /** [Bot] 戳了 [Stranger] */
    @MiraiExperimentalApi
    public data class NudgedByBot internal constructor(
        override val stranger: Stranger,
        override val action: String,
        override val suffix: String
    ) : StrangerNudgedEvent() {
        override fun toString(): String {
            return "StrangerNudgedEvent.NudgedByBot(stranger=$stranger, action=$action, suffix=$suffix)"
        }

        override val from: Stranger
            get() = bot.asStranger
    }
}