package net.mamoe.mirai.message

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.event.BroadcastControllable
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.MiraiInternalAPI

class FriendMessage(
    bot: Bot,
    override val sender: QQ,
    override val message: MessageChain
) : MessagePacket<QQ, QQ>(bot), BroadcastControllable {
    override val subject: QQ get() = sender

    override fun toString(): String = "FriendMessage(sender=${sender.id}, message=$message)"
}
