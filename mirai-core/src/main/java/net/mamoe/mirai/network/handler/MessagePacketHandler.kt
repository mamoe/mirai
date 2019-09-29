package net.mamoe.mirai.network.handler

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.event.events.group.GroupMessageEvent
import net.mamoe.mirai.event.events.qq.FriendMessageEvent
import net.mamoe.mirai.getGroupByNumber
import net.mamoe.mirai.getQQ
import net.mamoe.mirai.message.defaults.MessageChain
import net.mamoe.mirai.network.LoginSession
import net.mamoe.mirai.network.packet.ServerFriendMessageEventPacket
import net.mamoe.mirai.network.packet.ServerGroupMessageEventPacket
import net.mamoe.mirai.network.packet.ServerGroupUploadFileEventPacket
import net.mamoe.mirai.network.packet.ServerPacket
import net.mamoe.mirai.network.packet.action.ClientSendFriendMessagePacket
import net.mamoe.mirai.network.packet.action.ClientSendGroupMessagePacket
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

    override suspend fun onPacketReceived(packet: ServerPacket) {
        when (packet) {
            is ServerGroupUploadFileEventPacket -> {
                //todo
            }

            is ServerFriendMessageEventPacket -> {
                if (ignoreMessage) return

                FriendMessageEvent(session.bot, session.bot.getQQ(packet.qq), packet.message).broadcast()
            }

            is ServerGroupMessageEventPacket -> {
                if (ignoreMessage) return

                if (packet.qq == session.bot.account.qqNumber) return

                GroupMessageEvent(session.bot, session.bot.getGroupByNumber(packet.groupNumber), session.bot.getQQ(packet.qq), packet.message).broadcast()
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

    suspend fun sendFriendMessage(qq: QQ, message: MessageChain) {
        session.socket.sendPacket(ClientSendFriendMessagePacket(session.bot.account.qqNumber, qq.number, session.sessionKey, message))
    }

    suspend fun sendGroupMessage(group: Group, message: MessageChain) {
        session.socket.sendPacket(ClientSendGroupMessagePacket(session.bot.account.qqNumber, group.groupId, session.sessionKey, message))
    }
}