package net.mamoe.mirai.event.events.network

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.Cancellable
import net.mamoe.mirai.network.protocol.tim.packet.ClientPacket
import kotlin.reflect.KClass

abstract class ClientPacketEvent<out P : ClientPacket>(bot: Bot, packet: P) : PacketEvent<P>(bot, packet)

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