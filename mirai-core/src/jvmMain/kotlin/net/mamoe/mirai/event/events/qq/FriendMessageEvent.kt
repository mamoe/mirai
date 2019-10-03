package net.mamoe.mirai.event.events.qq

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.defaults.MessageChain
import kotlin.reflect.KClass

/**
 * 接受好友消息事件
 *
 * @author Him188moe
 */
class FriendMessageEvent(bot: Bot, sender: QQ, val message: MessageChain) : FriendEvent(bot, sender) {
    companion object : KClass<FriendMessageEvent> by FriendMessageEvent::class

    @JvmSynthetic
    suspend inline fun reply(message: Message) = sender.sendMessage(message)

    @JvmSynthetic
    suspend inline fun reply(message: String) = sender.sendMessage(message)

    @JvmSynthetic
    suspend inline fun reply(message: List<Message>) = sender.sendMessage(message)

    @JvmSynthetic
    suspend inline fun reply(message: MessageChain) = sender.sendMessage(message)//shortcut
}
