package net.mamoe.mirai.message

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.event.BroadcastControllable
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.MiraiInternalAPI

class FriendMessage(
    bot: Bot,
    /**
     * 是否是在这次登录之前的消息, 即消息记录
     */
    val previous: Boolean,
    override val sender: QQ,
    override val message: MessageChain
) : MessagePacket<QQ, QQ>(bot), BroadcastControllable {
    /**
     * 是否应被自动广播. 此为内部 API
     */
    @MiraiInternalAPI
    override val shouldBroadcast: Boolean
        get() = !previous

    override val subject: QQ get() = sender

    override fun toString(): String = "FriendMessage(sender=${sender.id}, message=$message)"
}
