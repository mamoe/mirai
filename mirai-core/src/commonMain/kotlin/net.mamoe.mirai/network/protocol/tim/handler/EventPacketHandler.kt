package net.mamoe.mirai.network.protocol.tim.handler

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.GroupId
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.contact.groupId
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.getGroup
import net.mamoe.mirai.getQQ
import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.network.BotSession
import net.mamoe.mirai.network.protocol.tim.packet.EventPacket
import net.mamoe.mirai.network.protocol.tim.packet.FriendStatusChanged
import net.mamoe.mirai.network.protocol.tim.packet.Packet
import net.mamoe.mirai.network.protocol.tim.packet.action.FriendImageIdRequestPacket
import net.mamoe.mirai.network.protocol.tim.packet.action.SendFriendMessagePacket
import net.mamoe.mirai.network.protocol.tim.packet.action.SendGroupMessagePacket
import net.mamoe.mirai.network.qqAccount

/**
 * 处理消息事件, 承担消息发送任务.
 *
 * @author Him188moe
 */
@Suppress("EXPERIMENTAL_API_USAGE")
class EventPacketHandler(session: BotSession) : PacketHandler(session) {
    companion object Key : PacketHandler.Key<EventPacketHandler>

    override suspend fun onPacketReceived(packet: Packet): Unit = with(session) {
        when (packet) {
            is EventPacket.FriendMessage -> {
                if (!packet.isPrevious) FriendMessageEvent(bot, bot.getQQ(packet.qq), packet.message) else null
            }

            is EventPacket.GroupMessage -> {
                if (packet.qq == bot.account.id) return

                GroupMessageEvent(
                    bot, bot.getGroup(GroupId(packet.groupNumber)), bot.getQQ(packet.qq), packet.message, packet.senderPermission, packet.senderName
                )
            }

            is EventPacket.FriendConversationInitialize -> FriendConversationInitializedEvent(bot, bot.getQQ(packet.qq))
            is FriendStatusChanged -> FriendOnlineStatusChangedEvent(bot, bot.getQQ(packet.qq), packet.status)
            is FriendImageIdRequestPacket.Response -> packet.imageId?.let { FriendImageIdObtainedEvent(bot, it) }

            is EventPacket.MemberPermissionChange ->
                MemberPermissionChangedEvent(bot, bot.getGroup(packet.groupId.groupId()), bot.getQQ(packet.qq), packet.kind)

            else -> null
        }?.broadcast()
    }

    suspend fun sendFriendMessage(qq: QQ, message: MessageChain) {
        session.socket.sendPacket(SendFriendMessagePacket(session.qqAccount, qq.id, session.sessionKey, message))
    }

    suspend fun sendGroupMessage(group: Group, message: MessageChain) {
        session.socket.sendPacket(
            SendGroupMessagePacket(
                session.qqAccount,
                group.internalId,
                session.sessionKey,
                message
            )
        )
    }
}