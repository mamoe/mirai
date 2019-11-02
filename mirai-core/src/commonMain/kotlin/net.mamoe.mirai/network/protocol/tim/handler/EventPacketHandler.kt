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
import net.mamoe.mirai.network.distributePacket
import net.mamoe.mirai.network.protocol.tim.packet.FriendOnlineStatusChangedPacket
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import net.mamoe.mirai.network.protocol.tim.packet.action.FriendImageIdRequestPacket
import net.mamoe.mirai.network.protocol.tim.packet.action.SendFriendMessagePacket
import net.mamoe.mirai.network.protocol.tim.packet.action.SendGroupMessagePacket
import net.mamoe.mirai.network.protocol.tim.packet.event.*
import net.mamoe.mirai.network.qqAccount

/**
 * 处理消息事件, 承担消息发送任务.
 *
 * @author Him188moe
 */
@Suppress("EXPERIMENTAL_API_USAGE")
class EventPacketHandler(session: BotSession) : PacketHandler(session) {
    companion object Key : PacketHandler.Key<EventPacketHandler>

    override suspend fun onPacketReceived(packet: ServerPacket): Unit = with(session) {
        when (packet) {
            is ServerGroupUploadFileEventPacket -> {
                //todo
            }

            is FriendMessageEventPacket -> {
                if (!packet.isPrevious) FriendMessageEvent(bot, bot.getQQ(packet.qq), packet.message).broadcast()
            }

            is GroupMessageEventPacket -> {
                if (packet.qq == bot.account.id) return

                GroupMessageEvent(
                    bot, bot.getGroup(GroupId(packet.groupNumber)), bot.getQQ(packet.qq), packet.message, packet.senderPermission, packet.senderName
                ).broadcast()
            }

            is FriendConversationInitializedEventPacket -> FriendConversationInitializedEvent(bot, bot.getQQ(packet.qq)).broadcast()
            is FriendOnlineStatusChangedPacket -> FriendOnlineStatusChangedEvent(bot, bot.getQQ(packet.qq), packet.status).broadcast()
            is FriendImageIdRequestPacket.Response -> packet.imageId?.let { FriendImageIdObtainedEvent(bot, it) }

            is GroupMemberPermissionChangedEventPacket -> MemberPermissionChangedEvent(
                bot, bot.getGroup(packet.groupId.groupId()), bot.getQQ(packet.qq), packet.kind
            ).broadcast()


            is FriendOnlineStatusChangedPacket.Encrypted -> distributePacket(packet.decrypt(sessionKey))
        }
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