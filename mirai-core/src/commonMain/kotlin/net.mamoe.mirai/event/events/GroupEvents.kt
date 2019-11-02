@file:Suppress("unused")

package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.network.protocol.tim.packet.event.SenderPermission


abstract class GroupEvent(bot: Bot, val group: Group) : BotEvent(bot)

/**
 * 群消息
 */
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

/**
 * 群成员权限改变
 */
class MemberPermissionChangedEvent(
    bot: Bot,
    group: Group,
    val member: QQ,
    val kind: Kind
) : GroupEvent(bot, group) {
    enum class Kind {
        /**
         * 变成管理员
         */
        BECOME_OPERATOR,
        /**
         * 不再是管理员
         */
        NO_LONGER_OPERATOR,
    } // TODO: 2019/11/2 变成群主的情况
}