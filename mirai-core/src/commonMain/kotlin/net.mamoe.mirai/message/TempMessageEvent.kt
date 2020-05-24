@file:Suppress("DEPRECATION_ERROR", "unused", "NOTHING_TO_INLINE")

package net.mamoe.mirai.message

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.BroadcastControllable
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.message.data.source

/**
 * 机器人收到的群临时会话消息的事件
 *
 * @see MessageEvent
 */
class TempMessageEvent(
    override val sender: Member,
    override val message: MessageChain,
    override val time: Int
) : TempMessage(), BroadcastControllable {
    init {
        val source = message[MessageSource] ?: error("Cannot find MessageSource from message")
        check(source is OnlineMessageSource.Incoming.FromTemp) { "source provided to a TempMessage must be an instance of OnlineMessageSource.Incoming.FromTemp" }
    }

    override val bot: Bot get() = sender.bot
    override val subject: Member get() = sender
    override val group: Group get() = sender.group
    override val senderName: String get() = sender.nameCardOrNick
    override val source: OnlineMessageSource.Incoming.FromTemp get() = message.source as OnlineMessageSource.Incoming.FromTemp

    override fun toString(): String =
        "TempMessageEvent(sender=${sender.id} from group(${sender.group.id}), message=$message)"
}