@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet.event

import kotlinx.io.core.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.network.sessionKey
import net.mamoe.mirai.qqAccount
import net.mamoe.mirai.utils.io.readIoBuffer

/**
 * 事件的识别 ID. 在 ACK 时使用
 */
class EventPacketIdentity(
    val from: UInt,//对于好友消息, 这个是发送人
    val to: UInt,//对于好友消息, 这个是bot
    internal val uniqueId: IoBuffer//8
) {
    override fun toString(): String = "($from->$to)"
}

fun BytePacketBuilder.writeEventPacketIdentity(identity: EventPacketIdentity) = with(identity) {
    writeUInt(from)
    writeUInt(to)
    writeFully(uniqueId)
}


@Suppress("FunctionName")
fun matchEventPacketFactory(value: UShort): EventParserAndHandler<*> =
    KnownEventParserAndHandler.firstOrNull { it.id == value } ?: IgnoredEventIds.firstOrNull { it.id == value } ?: UnknownEventParserAndHandler(value)

/**
 * 事件包, 它将会分析事件 ID 并解析事件为 [Packet]
 */
@NoLog
@Suppress("FunctionName")
object EventPacketFactory : PacketFactory<Packet, SessionKey>(SessionKey) {
    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler<*>): Packet {
        val eventIdentity = EventPacketIdentity(
            from = readUInt(),
            to = readUInt(),
            uniqueId = readIoBuffer(8)
        )
        handler.sendPacket(EventPacketFactory(id, sequenceId, handler.bot.qqAccount, handler.sessionKey, eventIdentity))
        discardExact(2) // 1F 40

        return with(matchEventPacketFactory(readUShort())) { parse(handler.bot, eventIdentity) }.also {
            if (it is MessagePacket<*, *>) {
                it.botVar = handler.bot
            }

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
        bot: UInt,
        sessionKey: SessionKey,
        identity: EventPacketIdentity
    ): OutgoingPacket = buildSessionPacket(name = "EventPacket", id = id, sequenceId = sequenceId, bot = bot, sessionKey = sessionKey) {
        writeEventPacketIdentity(identity)
    }
}

interface EventParserAndHandler<TPacket : Packet> {
    val id: UShort

    suspend fun ByteReadPacket.parse(bot: Bot, identity: EventPacketIdentity): TPacket

    /**
     * 在 [BotNetworkHandler] 下处理这个包. 广播事件等.
     */
    suspend fun BotNetworkHandler<*>.handlePacket(packet: TPacket) {}
}

abstract class KnownEventParserAndHandler<TPacket : Packet>(override val id: UShort) : EventParserAndHandler<TPacket> {
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