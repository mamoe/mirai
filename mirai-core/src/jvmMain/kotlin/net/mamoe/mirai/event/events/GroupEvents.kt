package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.defaults.MessageChain
import kotlin.reflect.KClass

/**
 * @author Him188moe
 */
abstract class GroupEvent(bot: Bot, val group: Group) : BotEvent(bot)

/**
 * @author Him188moe
 */
class GroupMessageEvent(bot: Bot, group: Group, val sender: QQ, val message: MessageChain) : GroupEvent(bot, group) {
    companion object : KClass<GroupMessageEvent> by GroupMessageEvent::class

    @JvmSynthetic
    suspend inline fun reply(message: Message) = group.sendMessage(message)

    @JvmSynthetic
    suspend inline fun reply(message: String) = group.sendMessage(message)

    @JvmSynthetic
    suspend inline fun reply(message: List<Message>) = group.sendMessage(message)

    @JvmSynthetic
    suspend inline fun reply(message: MessageChain) = group.sendMessage(message)
}