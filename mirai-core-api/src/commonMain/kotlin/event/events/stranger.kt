package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Stranger
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.message.action.Nudge
import net.mamoe.mirai.utils.MiraiExperimentalApi

/**
 * 新增陌生人的事件
 */
public data class StrangerAddEvent internal constructor(
    /**
     * 新好友. 已经添加到 [Bot.strangers]
     */
    public override val stranger: Stranger
) : StrangerEvent, Packet, AbstractEvent()


/**
 * 删除陌生人的事件
 *
 * 除主动删除外，此事件为惰性发生，无法实时广播
 */
public data class StrangerDeleteEvent(
    public override val stranger: Stranger
) : StrangerEvent, Packet, AbstractEvent()


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
            return "FriendNudgedEvent.NudgedByHimself(stranger=$stranger, action=$action, suffix=$suffix)"
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