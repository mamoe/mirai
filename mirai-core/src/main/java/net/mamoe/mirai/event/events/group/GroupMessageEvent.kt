package net.mamoe.mirai.event.events.group

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.defaults.MessageChain

/**
 * @author Him188moe
 */
class GroupMessageEvent(bot: Bot, group: Group, val sender: QQ, val message: MessageChain) : GroupEvent(bot, group) {

    @JvmSynthetic
    suspend fun reply(message: Message) = group.sendMessage(message)

    @JvmSynthetic
    suspend fun reply(message: String) = group.sendMessage(message)

    @JvmSynthetic
    suspend fun reply(message: List<Message>) = group.sendMessage(message)

    @JvmSynthetic
    suspend fun reply(message: MessageChain) = group.sendMessage(message)
}
