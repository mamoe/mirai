package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.Cancellable
import net.mamoe.mirai.network.protocol.tim.packet.OutgoingPacket
import net.mamoe.mirai.network.protocol.tim.packet.Packet
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket

/* Abstract */


sealed class PacketEvent<out P : Packet>(bot: Bot, open val packet: P) : BotEvent(bot)


/* Client to Server */

sealed class ClientPacketEvent(bot: Bot, packet: OutgoingPacket) : PacketEvent<OutgoingPacket>(bot, packet)

/**
 * 包已发送. 不可被取消
 */
class PacketSentEvent(bot: Bot, packet: OutgoingPacket) : ClientPacketEvent(bot, packet)

/**
 * 包发送前. 可被取消
 */
class BeforePacketSendEvent(bot: Bot, packet: OutgoingPacket) : ClientPacketEvent(bot, packet), Cancellable


/* Server to Client */

sealed class ServerPacketEvent<out P : ServerPacket>(bot: Bot, packet: P) : PacketEvent<P>(bot, packet)

class ServerPacketReceivedEvent(bot: Bot, packet: ServerPacket) : ServerPacketEvent<ServerPacket>(bot, packet)