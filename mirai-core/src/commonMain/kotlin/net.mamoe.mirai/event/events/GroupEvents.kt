package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.network.protocol.tim.packet.event.SenderPermission


abstract class GroupEvent(bot: Bot, val group: Group) : BotEvent(bot)


@Suppress("unused")
class GroupMessageEvent(
    bot: Bot,
    group: Group,
    val sender: QQ,
    val message: MessageChain,
    val senderPermission: SenderPermission,
    val senderName: String//若他有群名片就是群名片, 没有就是昵称
) : GroupEvent(bot, group) {
    suspend inline fun reply(message: Message) = group.sendMessage(message)

    suspend inline fun reply(message: String) = group.sendMessage(message)

    suspend inline fun reply(message: MessageChain) = group.sendMessage(message)
}