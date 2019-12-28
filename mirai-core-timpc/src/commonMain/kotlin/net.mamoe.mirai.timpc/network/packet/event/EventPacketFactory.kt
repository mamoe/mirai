@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.timpc.network.packet.event

import kotlinx.io.core.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.utils.NoLog
import net.mamoe.mirai.timpc.network.TIMPCBotNetworkHandler
import net.mamoe.mirai.qqAccount
import net.mamoe.mirai.timpc.network.packet.*
import net.mamoe.mirai.utils.io.readIoBuffer

/**
 * 事件的识别 ID. 在 ACK 时使用
 */
internal class EventPacketIdentity(
    val from: Long,//对于好友消息, 这个是发送人
    val to: Long,//对于好友消息, 这个是bot
    internal val uniqueId: IoBuffer//8
) {
    override fun toString(): String = "($from->$to)"
}

internal fun BytePacketBuilder.writeEventPacketIdentity(identity: EventPacketIdentity) = with(identity) {
    writeUInt(from.toUInt())
    writeUInt(to.toUInt())
    writeFully(uniqueId)
}


@Suppress("FunctionName")
internal fun matchEventPacketFactory(value: UShort): EventParserAndHandler<*> =
    KnownEventParserAndHandler.firstOrNull { it.id == value } ?: IgnoredEventIds.firstOrNull { it.id == value } ?: UnknownEventParserAndHandler(value)

/**
 * 事件包, 它将会分析事件 ID 并解析事件为 [Packet]
 */
@NoLog
@Suppress("FunctionName")
internal object EventPacketFactory : PacketFactory<Packet, SessionKey>(SessionKey) {
    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler): Packet {
        val eventIdentity = EventPacketIdentity(
            from = readUInt().toLong(),  // clear semantic, don't readQQ() or readGroup()
            to = readUInt().toLong(), // clear semantic
            uniqueId = readIoBuffer(8)
        )
        (handler as TIMPCBotNetworkHandler).socket.sendPacket(EventPacketFactory(id, sequenceId, handler.bot.qqAccount, handler.sessionKey, eventIdentity))
        discardExact(2) // 1F 40

        return with(matchEventPacketFactory(readUShort())) { parse(handler.bot, eventIdentity) }.also {
            if (it is EventParserAndHandler<*>) {
                @Suppress("UNCHECKED_CAST")
                with(it as EventParserAndHandler<in Packet>) {
                    with(handler) {
                        handlePacket(it)
                    }
                }

            }
        }
    }

    operator fun invoke(
        id: PacketId,
        sequenceId: UShort,
        bot: Long,
        sessionKey: SessionKey,
        identity: EventPacketIdentity
    ): OutgoingPacket = buildSessionPacket(name = "EventPacket", id = id, sequenceId = sequenceId, bot = bot, sessionKey = sessionKey) {
        writeEventPacketIdentity(identity)
    }
}

internal interface EventParserAndHandler<TPacket : Packet> {
    val id: UShort

    suspend fun ByteReadPacket.parse(bot: Bot, identity: EventPacketIdentity): TPacket

    /**
     * 在 [BotNetworkHandler] 下处理这个包. 广播事件等.
     */
    suspend fun BotNetworkHandler.handlePacket(packet: TPacket) {}
}

internal abstract class KnownEventParserAndHandler<TPacket : Packet>(override val id: UShort) : EventParserAndHandler<TPacket> {
    companion object FactoryList : MutableList<KnownEventParserAndHandler<*>> by mutableListOf(
        AndroidDeviceOnlineStatusChangedEventFactory,
        FriendConversationInitializedEventParserAndHandler,
        GroupFileUploadEventFactory,
        GroupMemberPermissionChangedEventFactory,
        GroupMessageEventParserAndHandler,
        FriendMessageEventParserAndHandler,
        FriendAddRequestEventPacket,
        MemberGoneEventPacketHandler,
        ConnectionOccupiedPacketHandler,
        MemberJoinPacketHandler,
        MemberMuteEventPacketParserAndHandler
    )
}