package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.Cancellable
import net.mamoe.mirai.network.protocol.tim.packet.ClientPacket
import net.mamoe.mirai.network.protocol.tim.packet.Packet
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import kotlin.reflect.KClass

/* Abstract */

/**
 * @author Him188moe
 */
sealed class PacketEvent<out P : Packet>(bot: Bot, open val packet: P) : BotEvent(bot)


/* Client to Server */

sealed class ClientPacketEvent<out P : ClientPacket>(bot: Bot, packet: P) : PacketEvent<P>(bot, packet)

/**
 * 包已发送. 不可被取消
 */
class PacketSentEvent<P : ClientPacket>(bot: Bot, packet: P) : ClientPacketEvent<P>(bot, packet) {
    companion object : KClass<PacketSentEvent<*>> by PacketSentEvent::class
}

/**
 * 包发送前. 可被取消
 */
class BeforePacketSendEvent<P : ClientPacket>(bot: Bot, packet: P) : ClientPacketEvent<P>(bot, packet), Cancellable {
    companion object : KClass<BeforePacketSendEvent<*>> by BeforePacketSendEvent::class
}


/* Server to Client */

sealed class ServerPacketEvent<out P : ServerPacket>(bot: Bot, packet: P) : PacketEvent<P>(bot, packet)

class ServerPacketReceivedEvent(bot: Bot, packet: ServerPacket) : ServerPacketEvent<ServerPacket>(bot, packet) {
    companion object : KClass<ServerPacketReceivedEvent> by ServerPacketReceivedEvent::class
}