package net.mamoe.mirai.message

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.BroadcastControllable
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.message.data.source
import net.mamoe.mirai.utils.SinceMirai
import net.mamoe.mirai.utils.getValue
import net.mamoe.mirai.utils.unsafeWeakRef

/**
 * 临时会话消息
 */
@SinceMirai("0.35.0")
class TempMessage(
    sender: Member,
    override val message: MessageChain
) : ContactMessage(), BroadcastControllable {
    init {
        val source = message.getOrNull(MessageSource) ?: error("Cannot find MessageSource from message")
        check(source is OnlineMessageSource.Incoming.FromTemp) { "source provided to a TempMessage must be an instance of OnlineMessageSource.Incoming.FromTemp" }
    }

    override val sender: Member by sender.unsafeWeakRef()
    override val bot: Bot get() = sender.bot
    override val subject: Member get() = sender
    override val source: OnlineMessageSource.Incoming.FromTemp get() = message.source as OnlineMessageSource.Incoming.FromTemp

    override fun toString(): String =
        "TempMessage(sender=${sender.id} from group(${sender.group.id}), message=$message)"
}