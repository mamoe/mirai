package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.contact.sendMessage
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.MessageChain


abstract class FriendEvent(bot: Bot, val sender: QQ) : BotEvent(bot)

/**
 * 接受好友消息事件
 *
 * @author Him188moe
 */
class FriendMessageEvent(bot: Bot, sender: QQ, val message: MessageChain) : FriendEvent(bot, sender) {
    suspend inline fun reply(message: Message) = sender.sendMessage(message)

    suspend inline fun reply(message: String) = sender.sendMessage(message)

    suspend inline fun reply(message: MessageChain) = sender.sendMessage(message)//shortcut
}