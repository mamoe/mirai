package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.Cancellable
import net.mamoe.mirai.network.protocol.tim.packet.OutgoingPacket
import net.mamoe.mirai.network.protocol.tim.packet.Packet

/* Abstract */

/**
 * 数据包相关事件
 *
 * @param P 指代数据包的类型. 这个类型是 **invariant(不变的)**
 */
sealed class PacketEvent<P : Packet>(bot: Bot, open val packet: P) : BotEvent(bot)


/* Client to Server */

/**
 * 发送给服务器的数据包的相关事件
 */
sealed class OutgoingPacketEvent(bot: Bot, packet: OutgoingPacket) : PacketEvent<OutgoingPacket>(bot, packet)

/**
 * 包已发送, 此时包数据已完全发送至服务器, 且包已被关闭.
 *
 * 不可被取消
 */
class PacketSentEvent(bot: Bot, packet: OutgoingPacket) : OutgoingPacketEvent(bot, packet)

/**
 * 包发送前, 此时包数据已经编码完成.
 *
 * 可被取消
 */
class BeforePacketSendEvent(bot: Bot, packet: OutgoingPacket) : OutgoingPacketEvent(bot, packet), Cancellable


/* Server to Client */

/**
 * 来自服务器的数据包的相关事件
 */
sealed class ServerPacketEvent<P : Packet>(bot: Bot, packet: P) : PacketEvent<P>(bot, packet)

/**
 * 服务器数据包接收事件. 此时包已经解密完成.
 */
class ServerPacketReceivedEvent<P : Packet>(bot: Bot, packet: P) : ServerPacketEvent<P>(bot, packet),
    Cancellable