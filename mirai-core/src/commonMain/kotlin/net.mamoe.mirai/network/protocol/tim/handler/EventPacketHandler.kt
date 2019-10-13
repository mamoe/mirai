package net.mamoe.mirai.network.protocol.tim.handler

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.getGroupByNumber
import net.mamoe.mirai.getQQ
import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.network.LoginSession
import net.mamoe.mirai.network.distributePacket
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.network.protocol.tim.packet.action.ClientSendFriendMessagePacket
import net.mamoe.mirai.network.protocol.tim.packet.action.ClientSendGroupMessagePacket
import net.mamoe.mirai.network.protocol.tim.packet.action.ServerSendFriendMessageResponsePacket
import net.mamoe.mirai.network.protocol.tim.packet.action.ServerSendGroupMessageResponsePacket
import net.mamoe.mirai.utils.MiraiLogger

/**
 * 处理消息事件, 承担消息发送任务.
 *
 * @author Him188moe
 */
@Suppress("EXPERIMENTAL_API_USAGE")
class EventPacketHandler(session: LoginSession) : PacketHandler(session) {
    internal var ignoreMessage: Boolean = true

    override suspend fun onPacketReceived(packet: ServerPacket): Unit = with(session) {
        when (packet) {
            is ServerGroupUploadFileEventPacket -> {
                //todo
            }

            is ServerFriendMessageEventPacket -> {
                if (ignoreMessage) return

                FriendMessageEvent(bot, bot.getQQ(packet.qq), packet.message).broadcast()
            }

            is ServerGroupMessageEventPacket -> {
                if (ignoreMessage) return

                if (packet.qq.toLong() == bot.account.qqNumber) return

                GroupMessageEvent(bot, bot.getGroupByNumber(packet.groupNumber), bot.getQQ(packet.qq), packet.message).broadcast()
            }

            is ServerSendFriendMessageResponsePacket,
            is ServerSendGroupMessageResponsePacket -> {
                //ignored
            }

            is ServerFieldOnlineStatusChangedPacket.Encrypted -> distributePacket(packet.decrypt(sessionKey))
            is ServerFieldOnlineStatusChangedPacket -> {
                MiraiLogger.logInfo("${packet.qq.toLong()} 登录状态改变为 ${packet.status}")
                //TODO
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