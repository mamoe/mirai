package net.mamoe.mirai.message

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.BroadcastControllable
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.message.data.source
import net.mamoe.mirai.utils.getValue
import net.mamoe.mirai.utils.unsafeWeakRef

class TempMessage(
    sender: Member,
    override val message: MessageChain
) : ContactMessage(), BroadcastControllable {
    override val sender: Member by sender.unsafeWeakRef()
    override val bot: Bot get() = sender.bot
    override val subject: Member get() = sender
    override val source: OnlineMessageSource.Incoming.FromTemp get() = message.source as OnlineMessageSource.Incoming.FromTemp

    override fun toString(): String = "TempMessage(sender=${sender.id} from group(${sender.group.id}), message=$message)"
}