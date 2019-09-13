package net.mamoe.mirai.network.handler

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.event.events.qq.FriendMessageEvent
import net.mamoe.mirai.event.hookWhile
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.defaults.MessageChain
import net.mamoe.mirai.network.LoginSession
import net.mamoe.mirai.network.packet.*
import net.mamoe.mirai.network.packet.action.ClientSendFriendMessagePacket
import net.mamoe.mirai.network.packet.action.ServerSendFriendMessageResponsePacket
import net.mamoe.mirai.network.packet.action.ServerSendGroupMessageResponsePacket

/**
 * 处理消息事件, 承担消息发送任务.
 *
 * @author Him188moe
 */
@Suppress("EXPERIMENTAL_API_USAGE")
class MessagePacketHandler(session: LoginSession) : PacketHandler(session) {
    internal var ignoreMessage: Boolean = true

    init {
        //todo for test
        FriendMessageEvent::class.hookWhile {
            if (session.socket.isClosed()) {
                return@hookWhile false
            }
            if (it.message() valueEquals "你好") {
                it.qq.sendMessage("你好!")
            } else if (it.message().toString().startsWith("复读")) {
                it.qq.sendMessage(it.message())
            }

            return@hookWhile true
        }
    }

    override fun onPacketReceived(packet: ServerPacket) {
        when (packet) {
            is ServerGroupUploadFileEventPacket -> {
                //todo
            }

            is ServerFriendMessageEventPacket -> {
                if (ignoreMessage) {
                    return
                }

                FriendMessageEvent(session.bot, session.bot.contacts.getQQ(packet.qq), packet.message).broadcast()
            }

            is ServerGroupMessageEventPacket -> {
                //todo message chain
                //GroupMessageEvent(this.bot, bot.contacts.getGroupByNumber(packet.groupNumber), bot.contacts.getQQ(packet.qq), packet.message)
            }

            is UnknownServerEventPacket -> {
                //todo
            }

            is ServerSendFriendMessageResponsePacket,
            is ServerSendGroupMessageResponsePacket -> {
                //ignored
            }
            else -> {
                //ignored
            }
        }
    }

    fun sendFriendMessage(qq: QQ, message: MessageChain) {
        session.socket.sendPacket(ClientSendFriendMessagePacket(session.bot.account.qqNumber, qq.number, session.sessionKey, message))
    }

    fun sendGroupMessage(group: Group, message: Message) {
        TODO()
        //sendPacket(ClientSendGroupMessagePacket(group.groupId, bot.account.qqNumber, sessionKey, message))
    }
}